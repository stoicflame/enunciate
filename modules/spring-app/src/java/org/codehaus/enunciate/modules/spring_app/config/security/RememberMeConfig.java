package org.codehaus.enunciate.modules.spring_app.config.security;

/**
 * Configuration for the remember-me authentication services.
 *
 * @author Ryan Heaton
 */
public class RememberMeConfig {

  private String key;

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
}
