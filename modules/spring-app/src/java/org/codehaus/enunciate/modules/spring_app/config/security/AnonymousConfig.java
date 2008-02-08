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
