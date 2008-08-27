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

  private static final Log LOG = LogFactory.getLog(EnunciateSpringServlet.class);
  private ResourceConfig resourceConfig;

  @Override
  protected void configure(ServletConfig sc, ResourceConfig rc, WebApplication wa) {
    //todo: uncomment when issues 101 and 102 are resolved.
//    try {
//      //attempt to load the JSON providers.
//      rc.getClasses().add(ClassUtils.forName("com.sun.jersey.impl.provider.entity.JSONRootElementProvider"));
//      rc.getClasses().add(ClassUtils.forName("com.sun.jersey.impl.provider.entity.JSONJAXBElementProvider"));
//      rc.getClasses().add(ClassUtils.forName("com.sun.jersey.impl.provider.entity.JSONArrayProvider"));
//      rc.getClasses().add(ClassUtils.forName("com.sun.jersey.impl.provider.entity.JSONObjectProvider"));
//    }
//    catch (Throwable e) {
//      LOG.info("Apparently, no JSON providers are on the classpath (" + e.getMessage() + ").");
//    }

    InputStream stream = getClass().getResourceAsStream("/jaxrs-providers.list");
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

    stream = getClass().getResourceAsStream("/jaxrs-root-resources.list");
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

    String pathBasedConneg = sc.getInitParameter(JerseyAdaptedHttpServletRequest.FEATURE_PATH_BASED_CONNEG);
    if (pathBasedConneg == null) {
      pathBasedConneg = Boolean.TRUE.toString();
    }
    rc.getFeatures().put(JerseyAdaptedHttpServletRequest.FEATURE_PATH_BASED_CONNEG, Boolean.valueOf(pathBasedConneg));

    String servletPath = sc.getInitParameter(JerseyAdaptedHttpServletRequest.PROPERTY_SERVLET_PATH);
    if (servletPath != null) {
      rc.getProperties().put(JerseyAdaptedHttpServletRequest.PROPERTY_SERVLET_PATH, servletPath);
    }

    stream = getClass().getResourceAsStream("/media-type-mappings.properties");
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
    wa.initiate(rc, new EnunciateSpringComponentProvider(WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext())));
    this.resourceConfig = rc;
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request = new JerseyAdaptedHttpServletRequest(request, this.resourceConfig);
    super.service(request, response);
  }
}
