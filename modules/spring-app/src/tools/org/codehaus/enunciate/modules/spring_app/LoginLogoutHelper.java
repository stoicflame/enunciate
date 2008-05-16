/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.modules.spring_app;

import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;

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
   * @throws org.springframework.security.AuthenticationException If login fails.
   */
  void loginWithUsernameAndPassword(String username, String password) throws AuthenticationException;

  /**
   * Login with a custom authentication token. The login will use the spring
   * security mechanisms to authenticate and establish the identity.
   *
   * @param authToken The auth token.
   * @throws org.springframework.security.AuthenticationException If login fails.
   */
  void login(Authentication authToken) throws AuthenticationException;

  /**
   * Logout.
   */
  void logout();

}
