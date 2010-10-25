package org.codehaus.enunciate.samples.genealogy.services;

import javax.servlet.*;
import java.io.IOException;

/**
 * @author Ryan Heaton
 */
public class NoOpFilter implements Filter {

  public void init(FilterConfig filterConfig) throws ServletException {
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    filterChain.doFilter(servletRequest, servletResponse);
  }

  public void destroy() {
  }
}
