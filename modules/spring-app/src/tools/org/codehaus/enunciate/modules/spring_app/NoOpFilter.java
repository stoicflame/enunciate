package org.codehaus.enunciate.modules.spring_app;

import javax.servlet.*;
import java.io.IOException;

/**
 * A no-op filter that just invokes the next filter chain.
 *
 * @author Ryan Heaton
 */
public class NoOpFilter implements Filter {

  public void init(FilterConfig filterConfig) throws ServletException {
    //no-op.
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    filterChain.doFilter(request, response);
  }

  public void destroy() {
    //no-op.
  }
}
