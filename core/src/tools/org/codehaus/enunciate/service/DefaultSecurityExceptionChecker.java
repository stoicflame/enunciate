package org.codehaus.enunciate.service;

/**
 * Default security exception checker.
 * 
 * @author Ryan Heaton
 */
public class DefaultSecurityExceptionChecker implements SecurityExceptionChecker {

  public boolean isAuthenticationFailed(Throwable throwable) {
    return false;
  }

  public boolean isAccessDenied(Throwable throwable) {
    return false;
  }
}
