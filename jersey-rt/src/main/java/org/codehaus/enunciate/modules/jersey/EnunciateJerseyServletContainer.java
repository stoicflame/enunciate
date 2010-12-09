package org.codehaus.enunciate.modules.jersey;

import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

/**
 * Enunciate-specific servlet container that adds additional Enunciate-supported functionality to the Jersey JAX-RS container. This additional functionality
 * includes:
 *
 * <ul>
 *   <li>Loading known Enunciate providers</li>
 *   <li>Application of a JAXB namespace prefix mapper</li>
 *   <li>path-based resource conneg</li>
 *   <li>Automatic (and dynamic) leverage of Spring container, if found in the environment.</li>
 * </ul>
 *
 * @author Ryan Heaton
 */
public class EnunciateJerseyServletContainer extends ServletContainer {

  private static final Log LOG = LogFactory.getLog(EnunciateJerseyServletContainer.class);
  private ResourceConfig resourceConfig;
  private String resourceProviderFactory = "org.codehaus.enunciate.modules.jersey.EnunciateSpringComponentProviderFactory";
  private WebApplication wa;
  private String servletPath;
  private boolean doPathBasedConneg;

  @Override
  protected void configure(ServletConfig sc, ResourceConfig rc, WebApplication wa) {
    rc.getClasses().add(EnunciateJAXBContextResolver.class);

    InputStream stream = loadResource("/jaxrs-providers.list");
    if (stream != null) {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf-8"));
        String line = reader.readLine();
        while (line != null) {
          rc.getClasses().add(loadClass(line));
          line = reader.readLine();
        }
      }
      catch (Throwable e) {
        LOG.error("Error loading enunciate-provided provider class. Skipping...", e);
      }
    }

    stream = loadResource("/jaxrs-root-resources.list");
    if (stream != null) {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf-8"));
        String line = reader.readLine();
        while (line != null) {
          rc.getClasses().add(loadClass(line));
          line = reader.readLine();
        }
      }
      catch (Throwable e) {
        LOG.error("Error loading enunciate-provided root resource class. Skipping...", e);
      }
    }

    try {
      rc.getClasses().add(loadClass("org.codehaus.enunciate.modules.amf.JAXRSProvider"));
    }
    catch (Throwable e) {
      LOG.info("org.codehaus.enunciate.modules.amf.JAXRSProvider not found.");
    }

    try {
      rc.getClasses().add(loadClass("org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider"));
    }
    catch (Throwable e) {
      LOG.info("org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider not loaded. Perhaps Jackson isn't on the classpath?");
    }

    String pathBasedConneg = sc.getInitParameter(JerseyAdaptedHttpServletRequest.FEATURE_PATH_BASED_CONNEG);
    if (pathBasedConneg == null) {
      pathBasedConneg = Boolean.TRUE.toString();
    }
    rc.getFeatures().put(JerseyAdaptedHttpServletRequest.FEATURE_PATH_BASED_CONNEG, Boolean.valueOf(pathBasedConneg));

    String resourceProvider = sc.getInitParameter(JerseyAdaptedHttpServletRequest.PROPERTY_RESOURCE_PROVIDER_FACTORY);
    if (resourceProvider != null) {
      this.resourceProviderFactory = resourceProvider;
    }

    stream = loadResource("/media-type-mappings.properties");
    if (stream != null) {
      try {
        Properties mappings = new Properties();
        mappings.load(stream);
        for (Map.Entry<Object, Object> entry : mappings.entrySet()) {
          rc.getMediaTypeMappings().put(String.valueOf(entry.getKey()), MediaType.valueOf(String.valueOf(entry.getValue())));
        }
      }
      catch (IOException e) {
        //fall through...
      }
    }

    String servletPath = sc.getInitParameter(JerseyAdaptedHttpServletRequest.PROPERTY_SERVLET_PATH);
    this.servletPath = servletPath == null ? "" : servletPath;
    this.doPathBasedConneg = rc.getFeature(JerseyAdaptedHttpServletRequest.FEATURE_PATH_BASED_CONNEG);

    super.configure(sc, rc, wa);
  }

  @Override
  protected void initiate(ResourceConfig rc, WebApplication wa) {
    wa.initiate(rc, loadResourceProviderFacotry(rc));
    this.resourceConfig = rc;
  }

  @Override
  protected WebApplication create() {
    WebApplication wa = super.create();
    this.wa = wa;
    return wa;
  }

  /**
   * Attempts to load the spring component provider factory, if spring is enabled.
   * @param rc The resource config.
   * @return The component provider factory, or null if none.
   */
  protected IoCComponentProviderFactory loadResourceProviderFacotry(ResourceConfig rc) {
    try {
      return (IoCComponentProviderFactory) loadClass(this.resourceProviderFactory)
        .getConstructor(ResourceConfig.class, ServletContext.class)
        .newInstance(rc, getServletContext());
    }
    catch (Throwable e) {
      LOG.info("Unable to load the spring component provider factory. Using the jersey default...");
      return null;
    }
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    //to service the request, we have to calculate a "base" uri and a "request" uri.
    //Jersey determines which resource is being invoked by the difference between
    //the "base" uri and the "request" uri. Unfortunately, the default implementation
    //does an bogus job of calculating the "base" uri because it bases it off
    //the value of request.getServletPath(), which is the path defined in the
    //web.xml that matched the call to this servlet. So if we defined in web.xml
    //the exact path to the servlet, then the calculated request url will be the
    //same as the calculated base url. So we have to compensate for that here.
    //see http://jersey.576304.n2.nabble.com/ServletContainer-and-relative-path-resolution-td674105.html

    //first get what the request url is, from which we can base our calculations.
    UriBuilder requestUrl = UriBuilder.fromUri(String.valueOf(request.getRequestURL()));

    //now calculate the path of the base uri, always starting with the context path.
    StringBuilder baseUriPathBuilder = new StringBuilder();

    //the next part is specific to Enunciate because Enunciate allows users to configure a "rest subcontext"
    //so users can put their rest endpoints under, for example, "/rest" and their soap endpoints under, say "/soap".
    //furthermore, for rest endpoints, Enunciate provides for the ability to do path-based content negotiation
    //so the default resource will be at, for example, "/rest/resource", but the xml representation will be
    //at "/xml/resource" and the json at "/json/resource".
    String requestPath = request.getRequestURI(); //start with what was actually requested.

    final String contextPath = request.getContextPath();
    MediaType mediaType = null;
    if (!"".equals(contextPath) && requestPath.startsWith(contextPath)) {
      //the context path is part of the base uri.
      baseUriPathBuilder.append(contextPath);
      requestPath = requestPath.substring(contextPath.length());
    }
    if (!"".equals(this.servletPath) && requestPath.startsWith(this.servletPath)) {
      //the enunciate-configured servlet path ("rest subcontext") is part of the base uri
      baseUriPathBuilder.append(this.servletPath);
      requestPath = requestPath.substring(this.servletPath.length());
    }
    else {
      //the enunciate-configured servlet path ("rest subcontext") is NOT part of the request. Iterate through
      //each media type mapping and see if it's part of the path.
      if (requestPath.startsWith("/")) {
        requestPath = requestPath.substring(1);
      }

      for (Map.Entry<String, MediaType> mediaMapping : this.resourceConfig.getMediaTypeMappings().entrySet()) {
        if (requestPath.startsWith(mediaMapping.getKey())) {
          //found a match to a specific media type, so we need to append the media type's 'key' to the base uri.
          String mediaKey = mediaMapping.getKey();
          baseUriPathBuilder.append('/').append(mediaKey);
          requestPath = requestPath.substring(mediaKey.length());
          mediaType = mediaMapping.getValue();
          break;
        }
      }
    }


    final String baseUriPath = baseUriPathBuilder.append('/').toString();

    //this check is in the super class, so I thought I'd keep it here for grins.
    if (!baseUriPath.equals(UriComponent.encode(baseUriPath, UriComponent.Type.PATH))) {
        throw new ContainerException("The servlet context path and/or the servlet path contain characters that are percent enocded");
    }

    final URI baseUri = requestUrl.replacePath(baseUriPath).build();

    String queryParameters = request.getQueryString();
    if (queryParameters == null) {
      queryParameters = "";
    }

    final URI requestUri = requestUrl
      .path(requestPath)
      .replaceQuery(queryParameters)
      .build();

    request = new JerseyAdaptedHttpServletRequest(request, mediaType);
    response = new JerseyAdaptedHttpServletResponse(response, this.wa);

    service(baseUri, requestUri, request, response);
  }

  /**
   * Return the default ClassLoader to use: typically the thread context
   * ClassLoader, if available; the ClassLoader that loaded the EnunciateJAXBContextResolver
   * class will be used as fallback.
   * <p>Call this method if you intend to use the thread context ClassLoader
   * in a scenario where you absolutely need a non-null ClassLoader reference:
   * for example, for class path resource loading (but not necessarily for
   * <code>Class.forName</code>, which accepts a <code>null</code> ClassLoader
   * reference as well).
   * @return the default ClassLoader (never <code>null</code>)
   * @see java.lang.Thread#getContextClassLoader()
   */
  public static ClassLoader getDefaultClassLoader() {
    ClassLoader cl = null;
    try {
      cl = Thread.currentThread().getContextClassLoader();
    }
    catch (Throwable ex) {
      //fall through...
    }
    if (cl == null) {
      // No thread context class loader -> use class loader of this class.
      cl = EnunciateJAXBContextResolver.class.getClassLoader();
    }
    return cl;
  }

  /**
   * Loads a resource from the classpath.
   *
   * @param resource The resource to load.
   * @return The resource.
   */
  protected InputStream loadResource(String resource) {
    return getDefaultClassLoader().getResourceAsStream(resource);
  }

  /**
   * Loads a class from the classpath.
   *
   * @param classname The class name.
   * @return The class.
   */
  protected Class loadClass(String classname) throws ClassNotFoundException {
    return getDefaultClassLoader().loadClass(classname);
  }

}
