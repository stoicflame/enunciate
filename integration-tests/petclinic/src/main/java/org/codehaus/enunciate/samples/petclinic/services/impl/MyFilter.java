package org.codehaus.enunciate.samples.petclinic.services.impl;

import org.codehaus.enunciate.modules.spring_app.LoginLogoutHelper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import java.io.IOException;

/**
 * Just a test to make sure ENUNCIATE-511 and ENUNCIATE-512 are working.
 * @author Ryan Heaton
 */
public class MyFilter implements Filter {

  private LoginLogoutHelper helper;

  public void init(FilterConfig filterConfig) throws ServletException {
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    filterChain.doFilter(servletRequest, servletResponse);
  }

  public void destroy() {
  }

  public LoginLogoutHelper getHelper() {
    return helper;
  }

  @Autowired
  public void setHelper(LoginLogoutHelper helper) {
    this.helper = helper;
  }
}
