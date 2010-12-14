package org.codehaus.enunciate.samples.petclinic.services.impl;

import org.springframework.security.AuthenticationException;
import org.springframework.security.ui.basicauth.BasicProcessingFilterEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Ryan Heaton
 */
public class MyEntryPoint extends BasicProcessingFilterEntryPoint {

  public MyEntryPoint() {
    setRealmName("whatever");
  }

  @Override
  public void commence(ServletRequest request, ServletResponse response, AuthenticationException authException) throws IOException, ServletException {
    HttpServletResponse httpResponse = (HttpServletResponse) response;
//    httpResponse.addHeader("WWW-Authenticate", "Basic realm=\"" + realmName + "\"");
    httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
  }
}
