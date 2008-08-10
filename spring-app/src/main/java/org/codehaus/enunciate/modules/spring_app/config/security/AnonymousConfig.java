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

package org.codehaus.enunciate.modules.spring_app.config.security;

/**
 * Configuration for anonymous identity processing.
 *
 * @author Ryan Heaton
 */
public class AnonymousConfig {

  private String key;
  private String userId = "anonymous";
  private String roles = "ANONYMOUS";

  /**
   * The secure key used to process the anonymous identity token.
   *
   * @return The secure key used to process the anonymous identity token.
   */
  public String getKey() {
    return key;
  }

  /**
   * The secure key used to process the anonymous identity token.
   *
   * @param key The secure key used to process the anonymous identity token.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * The user id of the anonymous user.
   *
   * @return The user id of the anonymous user.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * The user id of the anonymous user.
   *
   * @param userId The user id of the anonymous user.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * The roles (comma-separated) assigned to the anonymous user.
   *
   * @return The roles (comma-separated) assigned to the anonymous user.
   */
  public String getRoles() {
    return roles;
  }

  /**
   * The roles (comma-separated) assigned to the anonymous user.
   *
   * @param roles The roles (comma-separated) assigned to the anonymous user.
   */
  public void setRoles(String roles) {
    this.roles = roles;
  }
}
