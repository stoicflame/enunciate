package org.codehaus.enunciate.modules.jersey;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;

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

  static final ThreadLocal<HttpServletRequest> CURRENT_REQUEST = new ThreadLocal<HttpServletRequest>();
  static final ThreadLocal<HttpServletResponse> CURRENT_RESPONSE = new ThreadLocal<HttpServletResponse>();

  private static final Log LOG = LogFactory.getLog(EnunciateJerseyServletContainer.class);
  private ResourceConfig resourceConfig;

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
      rc.getClasses().add(loadClass("org.codehaus.jackson.jaxrs.JacksonJsonProvider"));
    }
    catch (Throwable e) {
      LOG.info("org.codehaus.jackson.jaxrs.JacksonJsonProvider not loaded. Perhaps Jackson isn't on the classpath?");
    }

    try {
      loadClass("org.codehaus.jackson.xc.JaxbAnnotationIntrospector");
      rc.getClasses().add(loadClass("org.codehaus.enunciate.modules.jersey.JacksonObjectMapperContextResolver"));
    }
    catch (Throwable e) {
      LOG.info("org.codehaus.enunciate.modules.jersey.JacksonObjectMapperContextResolver not loaded. Perhaps Jackson-XC isn't on the classpath?");
    }

    String pathBasedConneg = sc.getInitParameter(JerseyAdaptedHttpServletRequest.FEATURE_PATH_BASED_CONNEG);
    if (pathBasedConneg == null) {
      pathBasedConneg = Boolean.TRUE.toString();
    }
    rc.getFeatures().put(JerseyAdaptedHttpServletRequest.FEATURE_PATH_BASED_CONNEG, Boolean.valueOf(pathBasedConneg));

    String servletPath = sc.getInitParameter(JerseyAdaptedHttpServletRequest.PROPERTY_SERVLET_PATH);
    if (servletPath != null) {
      rc.getProperties().put(JerseyAdaptedHttpServletRequest.PROPERTY_SERVLET_PATH, servletPath);
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

    super.configure(sc, rc, wa);
  }

  @Override
  protected void initiate(ResourceConfig rc, WebApplication wa) {
    wa.initiate(rc, loadResourceProviderFacotry(rc));
    this.resourceConfig = rc;
  }

  /**
   * Attempts to load the spring component provider factory, if spring is enabled.
   * @param rc The resource config.
   * @return The component provider factory, or null if none.
   */
  protected IoCComponentProviderFactory loadResourceProviderFacotry(ResourceConfig rc) {
    try {
      return (IoCComponentProviderFactory) loadClass("org.codehaus.enunciate.modules.jersey.EnunciateSpringComponentProviderFactory")
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
    request = new JerseyAdaptedHttpServletRequest(request, this.resourceConfig);
    try {
      CURRENT_REQUEST.set(request);
      CURRENT_RESPONSE.set(response);
      super.service(request, response);
    }
    finally {
      CURRENT_REQUEST.remove();
      CURRENT_RESPONSE.remove();
    }
  }

  @Override
  protected WebApplication create() {
    return new EnunciateWebApplication(super.create());
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
