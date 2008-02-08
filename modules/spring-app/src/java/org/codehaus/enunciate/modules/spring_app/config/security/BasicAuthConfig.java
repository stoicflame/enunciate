package org.codehaus.enunciate.modules.spring_app.config.security;

/**
 * Configuration for the HTTP Basic auth.
 *
 * @author Ryan Heaton
 */
public class BasicAuthConfig {

  private String realmName;

  /**
   * The realm name for the basic auth.
   *
   * @return The realm name for the basic auth.
   */
  public String getRealmName() {
    return realmName;
  }

  /**
   * The realm name for the basic auth.
   *
   * @param realmName The realm name for the basic auth.
   */
  public void setRealmName(String realmName) {
    this.realmName = realmName;
  }
}
