package org.codehaus.enunciate.modules.spring_app;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;

/**
 * Helper utility for logging in, logging out.
 * 
 * @author Ryan Heaton
 */
public interface LoginLogoutHelper {

  /**
   * Login with a username and password. The login will use the spring
   * security mechanisms to authenticate and establish the identity.
   *
   * @param username The username.
   * @param password The password.
   * @throws org.acegisecurity.AuthenticationException If login fails.
   */
  void loginWithUsernameAndPassword(String username, String password) throws AuthenticationException;

  /**
   * Login with a custom authentication token. The login will use the spring
   * security mechanisms to authenticate and establish the identity.
   *
   * @param authToken The auth token.
   * @throws org.acegisecurity.AuthenticationException If login fails.
   */
  void login(Authentication authToken) throws AuthenticationException;

  /**
   * Logout.
   */
  void logout();

}
