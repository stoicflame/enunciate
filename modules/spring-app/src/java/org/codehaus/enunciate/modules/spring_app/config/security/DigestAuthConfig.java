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