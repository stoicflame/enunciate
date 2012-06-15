package org.codehaus.enunciate.jboss;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class EnunciateJBossHttpServletDispatcher extends HttpServletDispatcher {

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    Map<String, MediaType> mappings = getDispatcher().getMediaTypeMappings();
    getDispatcher().addHttpPreprocessor(new PathBasedConnegHttpPreprocessor(mappings));
  }
}
