package org.codehaus.enunciate.modules.spring_app.config.security;

/**
 * Configuration for a form-based logout.
 * 
 * @author Ryan Heaton
 */
public class FormBasedLogoutConfig {

  private String url = "/form/logout";
  private String redirectOnSuccessUrl = "/";

  /**
   * The URL of the form-based logout endpoint.
   *
   * @return The URL of the form-based logout endpoint.
   */
  public String getUrl() {
    return url;
  }

  /**
   * The URL of the form-based logout endpoint.
   *
   * @param url The URL of the form-based logout endpoint.
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * The URL to which to redirect on sucessful logout.
   *
   * @return The URL to which to redirect on sucessful logout.
   */
  public String getRedirectOnSuccessUrl() {
    return redirectOnSuccessUrl;
  }

  /**
   * The URL to which to redirect on sucessful logout.
   *
   * @param redirectOnSuccessUrl The URL to which to redirect on sucessful logout.
   */
  public void setRedirectOnSuccessUrl(String redirectOnSuccessUrl) {
    this.redirectOnSuccessUrl = redirectOnSuccessUrl;
  }
}
