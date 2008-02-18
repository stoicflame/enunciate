package org.codehaus.enunciate.modules.spring_app;

import org.codehaus.enunciate.service.SecurityExceptionChecker;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AccessDeniedException;

/**
 * Security checker for Acegi.
 *
 * @author Ryan Heaton
 */
public class SpringSecurityExceptionChecker implements SecurityExceptionChecker {

  public boolean isAuthenticationFailed(Throwable throwable) {
    return throwable instanceof AuthenticationException;
  }

  public boolean isAccessDenied(Throwable throwable) {
    return throwable instanceof AccessDeniedException;
  }
}
