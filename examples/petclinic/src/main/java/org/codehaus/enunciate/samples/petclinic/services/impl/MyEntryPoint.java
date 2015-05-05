package org.codehaus.enunciate.samples.petclinic.services.impl;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Ryan Heaton
 */
public class MyEntryPoint extends BasicAuthenticationEntryPoint  {

  public MyEntryPoint() {
    setRealmName("whatever");
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
    //    httpResponse.addHeader("WWW-Authenticate", "Basic realm=\"" + realmName + "\"");
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
  }
}
