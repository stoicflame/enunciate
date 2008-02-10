package org.codehaus.enunciate.modules.spring_app;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.AuthenticationException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Exception handler that's aware of how to deal with security exceptions.
 *
 * @author Ryan Heaton
 */
public class SecurityAwareExceptionHandler implements HandlerExceptionResolver {

  private final HandlerExceptionResolver delegate;

  public SecurityAwareExceptionHandler(HandlerExceptionResolver delegate) {
    if (delegate == null) {
      throw new IllegalArgumentException("A delegate must be provided.");
    }
    this.delegate = delegate;
  }

  public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    if ((ex instanceof AccessDeniedException) || (ex instanceof AuthenticationException)) {
      throw (RuntimeException) ex;
    }
    else {
      return delegate.resolveException(request, response, handler, ex);
    }
  }

}
