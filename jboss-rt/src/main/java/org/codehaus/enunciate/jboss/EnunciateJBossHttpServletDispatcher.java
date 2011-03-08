package org.codehaus.enunciate.jboss;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

/**
 * @author Ryan Heaton
 */
public class EnunciateJBossHttpServletDispatcher extends HttpServletDispatcher {

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    getDispatcher().addHttpPreprocessor(new PathBasedConnegHttpPreprocessor(getDispatcher().getMediaTypeMappings()));
  }
}
