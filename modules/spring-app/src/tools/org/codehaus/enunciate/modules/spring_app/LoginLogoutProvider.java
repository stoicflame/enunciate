package org.codehaus.enunciate.modules.spring_app;

/**
 * Marker interface for a provider of login/logout services;
 *
 * @author Ryan Heaton
 */
public interface LoginLogoutProvider {

  /**
   * Initialize this provider with the specified login/logout helper.
   *
   * @param helper The login-logout helper.
   */
  void initLoginLogoutHelper(LoginLogoutHelper helper);
}
