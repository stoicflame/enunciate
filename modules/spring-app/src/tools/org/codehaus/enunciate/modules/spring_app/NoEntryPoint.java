package org.codehaus.enunciate.modules.spring_app;

import org.acegisecurity.ui.AuthenticationEntryPoint;
import org.acegisecurity.AuthenticationException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Entry point specifying that no entry point is available.
 *
 * @author Ryan Heaton
 */
public class NoEntryPoint implements AuthenticationEntryPoint {

  public void commence(ServletRequest request, ServletResponse response, AuthenticationException authException) throws IOException, ServletException {
    ((HttpServletResponse)response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }
}
