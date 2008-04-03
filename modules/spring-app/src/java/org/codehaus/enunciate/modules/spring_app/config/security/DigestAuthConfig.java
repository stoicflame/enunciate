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
 * Configuration for HTTP Digest auth.
 *
 * @author Ryan Heaton
 */
public class DigestAuthConfig {

  private String realmName;
  private String key;
  private Integer nonceValiditySeconds;

  /**
   * The realm name for the digest authentication.
   *
   * @return The realm name for the digest authentication.
   */
  public String getRealmName() {
    return realmName;
  }

  /**
   * The realm name for the digest authentication.
   *
   * @param realmName The realm name for the digest authentication.
   */
  public void setRealmName(String realmName) {
    this.realmName = realmName;
  }

  /**
   * The digest secure key.
   *
   * @return The digest secure key.
   */
  public String getKey() {
    return key;
  }

  /**
   * The digest secure key.
   *
   * @param key The digest secure key.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * The number of seconds for which the digest nonce is valid.
   *
   * @return The number of seconds for which the digest nonce is valid.
   */
  public Integer getNonceValiditySeconds() {
    return nonceValiditySeconds;
  }

  /**
   * The number of seconds for which the digest nonce is valid.
   *
   * @param nonceValiditySeconds The number of seconds for which the digest nonce is valid.
   */
  public void setNonceValiditySeconds(Integer nonceValiditySeconds) {
    this.nonceValiditySeconds = nonceValiditySeconds;
  }
}