package org.codehaus.enunciate.service;

/**
 * @author Ryan Heaton
 */
public interface SecurityExceptionChecker {

  /**
   * Whether the given throwable is an authentication failed exception.
   *
   * @param throwable The throwable to check.
   * @return Whether the given throwable is an authentication failed exception.
   */
  boolean isAuthenticationFailed(Throwable throwable);

  /**
   * Whether the given throwable is an access denied exception.
   *
   * @param throwable The throwable to check.
   * @return Whether the given throwable is an access denied exception.
   */
  boolean isAccessDenied(Throwable throwable);

}
