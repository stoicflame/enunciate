package org.codehaus.enunciate.modules.spring_app.config.security;

/**
 * Configuration for the remember-me authentication services.
 *
 * @author Ryan Heaton
 */
public class RememberMeConfig {

  private String key;
  private String cookieName;
  private Integer tokenValiditySeconds;

  /**
   * The remember-me key.
   *
   * @return The remember-me key.
   */
  public String getKey() {
    return key;
  }

  /**
   * The remember-me key.
   *
   * @param key The remember-me key.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * The name of the cookie used for the remember-me token.
   *
   * @return The name of the cookie used for the remember-me token.
   */
  public String getCookieName() {
    return cookieName;
  }

  /**
   * The name of the cookie used for the remember-me token.
   *
   * @param cookieName The name of the cookie used for the remember-me token.
   */
  public void setCookieName(String cookieName) {
    this.cookieName = cookieName;
  }

  /**
   * The number of seconds the remember-me token is valid.
   *
   * @return The number of seconds the remember-me token is valid.
   */
  public Integer getTokenValiditySeconds() {
    return tokenValiditySeconds;
  }

  /**
   * The number of seconds the remember-me token is valid.
   *
   * @param tokenValiditySeconds The number of seconds the remember-me token is valid.
   */
  public void setTokenValiditySeconds(Integer tokenValiditySeconds) {
    this.tokenValiditySeconds = tokenValiditySeconds;
  }
}
