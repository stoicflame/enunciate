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
 * Configuration for a form-based login.
 *
 * @author Ryan Heaton
 */
public class FormBasedLoginConfig {

  private String url = "/form/login";
  private String redirectOnSuccessUrl = "/";
  private String redirectOnFailureUrl = "/login.jsp";
  private String loginPageURL = "/login.jsp";
  private String loginPageFile;
  private BeanReference loginPageController;
  private boolean enableOpenId = false;

  /**
   * The URL of the form-based login endpoint.
   *
   * @return The URL of the form-based login endpoint.
   */
  public String getUrl() {
    return url;
  }

  /**
   * The URL of the form-based login endpoint.
   *
   * @param url The URL of the form-based login endpoint.
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * The URL to which to redirect on login success.
   *
   * @return The URL to which to redirect on login success.
   */
  public String getRedirectOnSuccessUrl() {
    return redirectOnSuccessUrl;
  }

  /**
   * The URL to which to redirect on login success.
   *
   * @param redirectOnSuccessUrl The URL to which to redirect on login success.
   */
  public void setRedirectOnSuccessUrl(String redirectOnSuccessUrl) {
    this.redirectOnSuccessUrl = redirectOnSuccessUrl;
  }

  /**
   * The URL to which to redirect on login failure.
   *
   * @return The URL to which to redirect on login failure.
   */
  public String getRedirectOnFailureUrl() {
    return redirectOnFailureUrl;
  }

  /**
   * The URL to which to redirect on login failure.
   *
   * @param redirectOnFailureUrl The URL to which to redirect on login failure.
   */
  public void setRedirectOnFailureUrl(String redirectOnFailureUrl) {
    this.redirectOnFailureUrl = redirectOnFailureUrl;
  }

  /**
   * The URL of the login page.
   *
   * @return The URL of the login page.
   */
  public String getLoginPageURL() {
    return loginPageURL;
  }

  /**
   * The URL of the login page.
   *
   * @param loginPageURL The URL of the login page.
   */
  public void setLoginPageURL(String loginPageURL) {
    this.loginPageURL = loginPageURL;
  }

  /**
   * File to be used as the login page.
   *
   * @return File to be used as the login page.
   */
  public String getLoginPageFile() {
    return loginPageFile;
  }

  /**
   * File to be used as the login page.
   *
   * @param loginPageFile File to be used as the login page.
   */
  public void setLoginPageFile(String loginPageFile) {
    this.loginPageFile = loginPageFile;
  }

  /**
   * The controller that handles the login page.
   *
   * @return The controller that handles the login page.
   */
  public BeanReference getLoginPageController() {
    return loginPageController;
  }

  /**
   * The controller that handles the login page.
   *
   * @param loginPageController The controller that handles the login page.
   */
  public void setLoginPageController(BeanReference loginPageController) {
    this.loginPageController = loginPageController;
  }

  /**
   * Whether to enable OpenId in a form-based login.
   *
   * @return Whether to enable OpenId in a form-based login.
   */
  public boolean isEnableOpenId() {
    return enableOpenId;
  }

  /**
   * Whether to enable OpenId in a form-based login.
   *
   * @param enableOpenId Whether to enable OpenId in a form-based login.
   */
  public void setEnableOpenId(boolean enableOpenId) {
    this.enableOpenId = enableOpenId;
  }
}