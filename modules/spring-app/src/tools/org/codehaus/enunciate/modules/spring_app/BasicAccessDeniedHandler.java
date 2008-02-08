package org.codehaus.enunciate.modules.spring_app;

import org.acegisecurity.ui.AccessDeniedHandler;
import org.acegisecurity.AccessDeniedException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Just sends the error.
 * 
 * @author Ryan Heaton
 */
public class BasicAccessDeniedHandler implements AccessDeniedHandler {

  public void handle(ServletRequest request, ServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
  }
}
