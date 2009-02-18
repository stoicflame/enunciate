package org.codehaus.enunciate.modules.jersey;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class EnunciateSpringServlet extends ServletContainer {

  static final ThreadLocal<HttpServletRequest> CURRENT_REQUEST = new ThreadLocal<HttpServletRequest>();
  static final ThreadLocal<HttpServletResponse> CURRENT_RESPONSE = new ThreadLocal<HttpServletResponse>();

  private static final Log LOG = LogFactory.getLog(EnunciateSpringServlet.class);
  private ResourceConfig resourceConfig;

  @Override
  protected void configure(ServletConfig sc, ResourceConfig rc, WebApplication wa) {
    rc.getClasses().add(EnunciateJAXBContextResolver.class);

    InputStream stream = ClassUtils.getDefaultClassLoader().getResourceAsStream("/jaxrs-providers.list");
    if (stream != null) {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf-8"));
        String line = reader.readLine();
        while (line != null) {
          rc.getClasses().add(ClassUtils.forName(line));
          line = reader.readLine();
        }
      }
      catch (Throwable e) {
        LOG.error("Error loading enunciate-provided provider class. Skipping...", e);
      }
    }

    stream = ClassUtils.getDefaultClassLoader().getResourceAsStream("/jaxrs-root-resources.list");
    if (stream != null) {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "utf-8"));
        String line = reader.readLine();
        while (line != null) {
          rc.getClasses().add(ClassUtils.forName(line));
          line = reader.readLine();
        }
      }
      catch (Throwable e) {
        LOG.error("Error loading enunciate-provided root resource class. Skipping...", e);
      }
    }

    try {
      rc.getClasses().add(ClassUtils.forName("org.codehaus.enunciate.modules.amf.JAXRSProvider"));
    }
    catch (Throwable e) {
      LOG.info("org.codehaus.enunciate.modules.amf.JAXRSProvider not found.");
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

    stream = ClassUtils.getDefaultClassLoader().getResourceAsStream("/media-type-mappings.properties");
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
    wa.initiate(rc, new EnunciateSpringComponentProviderFactory(rc, WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext())));
    this.resourceConfig = rc;
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
}
