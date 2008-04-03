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
 * Configuration for OAuth.
 * 
 * @author Ryan Heaton
 */
public class OAuthConfig {

  private String infoURL = "/oauth/info.html";
  private String infoPageFile = null;
  private String requestTokenURL = "/oauth/request_token";
  private String accessConfirmationURL = "/oauth/confirm_access";
  private String confirmAccessPageFile = null;
  private String accessConfirmedURL = "/oauth/access_confirmed";
  private String accessConfirmedPageFile = null;
  private String grantAccessURL = "/oauth/authorize";
  private String accessTokenURL = "/oauth/access_token";
  private BeanReference tokenServices;
  private BeanReference consumerDetailsService;

  /**
   * The URL to the OAuth information page.
   *
   * @return The URL to the OAuth information page.
   */
  public String getInfoURL() {
    return infoURL;
  }

  /**
   * The URL to the OAuth information page.
   *
   * @param infoURL The URL to the OAuth information page.
   */
  public void setInfoURL(String infoURL) {
    this.infoURL = infoURL;
  }

  /**
   * The file (jsp) to use as the info page.
   *
   * @return The file (jsp) to use as the info page.
   */
  public String getInfoPageFile() {
    return infoPageFile;
  }

  /**
   * The file (jsp) to use as the info page.
   *
   * @param infoPageFile The file (jsp) to use as the info page.
   */
  public void setInfoPageFile(String infoPageFile) {
    this.infoPageFile = infoPageFile;
  }

  /**
   * The URL for the request token.
   *
   * @return The URL for the request token.
   */
  public String getRequestTokenURL() {
    return requestTokenURL;
  }

  /**
   * The URL for the request token.
   *
   * @param requestTokenURL The URL for the request token.
   */
  public void setRequestTokenURL(String requestTokenURL) {
    this.requestTokenURL = requestTokenURL;
  }

  /**
   * The URL to which the user will be redirected to confirm access.
   *
   * @return The URL to which the user will be redirected to confirm access.
   */
  public String getAccessConfirmationURL() {
    return accessConfirmationURL;
  }

  /**
   * The URL to which the user will be redirected to confirm access.
   *
   * @param accessConfirmationURL The URL to which the user will be redirected to confirm access.
   */
  public void setAccessConfirmationURL(String accessConfirmationURL) {
    this.accessConfirmationURL = accessConfirmationURL;
  }

  /**
   * A file (jsp) to use as the confirm access page.
   *
   * @return A file (jsp) to use as the confirm access page.
   */
  public String getConfirmAccessPageFile() {
    return confirmAccessPageFile;
  }

  /**
   * A file (jsp) to use as the confirm access page.
   *
   * @param confirmAccessPageFile A file (jsp) to use as the confirm access page.
   */
  public void setConfirmAccessPageFile(String confirmAccessPageFile) {
    this.confirmAccessPageFile = confirmAccessPageFile;
  }

  /**
   * The URL to which the user will be redirected if access was granted an no callback was provided.
   *
   * @return The URL to which the user will be redirected if access was granted an no callback was provided.
   */
  public String getAccessConfirmedURL() {
    return accessConfirmedURL;
  }

  /**
   * The URL to which the user will be redirected if access was granted an no callback was provided.
   *
   * @param accessConfirmedURL The URL to which the user will be redirected if access was granted an no callback was provided.
   */
  public void setAccessConfirmedURL(String accessConfirmedURL) {
    this.accessConfirmedURL = accessConfirmedURL;
  }

  /**
   * A file (jsp) to use as the access confirmed page.
   *
   * @return A file (jsp) to use as the access confirmed page.
   */
  public String getAccessConfirmedPageFile() {
    return accessConfirmedPageFile;
  }

  /**
   * A file (jsp) to use as the access confirmed page.
   *
   * @param accessConfirmedPageFile A file (jsp) to use as the access confirmed page.
   */
  public void setAccessConfirmedPageFile(String accessConfirmedPageFile) {
    this.accessConfirmedPageFile = accessConfirmedPageFile;
  }

  /**
   * The URL used to process the request to grant access.
   *
   * @return The URL used to process the request to grant access.
   */
  public String getGrantAccessURL() {
    return grantAccessURL;
  }

  /**
   * The URL used to process the request to grant access.
   *
   * @param grantAccessURL The URL used to process the request to grant access.
   */
  public void setGrantAccessURL(String grantAccessURL) {
    this.grantAccessURL = grantAccessURL;
  }

  /**
   * The URL used get an access token.
   *
   * @return The URL used get an access token.
   */
  public String getAccessTokenURL() {
    return accessTokenURL;
  }

  /**
   * The URL used get an access token.
   *
   * @param accessTokenURL The URL used get an access token.
   */
  public void setAccessTokenURL(String accessTokenURL) {
    this.accessTokenURL = accessTokenURL;
  }

  /**
   * Bean reference to the OAuth token services to use.
   *
   * @return Bean reference to the OAuth token services to use.
   */
  public BeanReference getTokenServices() {
    return tokenServices;
  }

  /**
   * Bean reference to the OAuth token services to use.
   *
   * @param tokenServices Bean reference to the OAuth token services to use.
   */
  public void setTokenServices(BeanReference tokenServices) {
    this.tokenServices = tokenServices;
  }

  /**
   * Bean reference to the OAuth consumer details service to use.
   *
   * @return Bean reference to the OAuth consumer details service to use.
   */
  public BeanReference getConsumerDetailsService() {
    return consumerDetailsService;
  }

  /**
   * Bean reference to the OAuth consumer details service to use.
   *
   * @param consumerDetailsService Bean reference to the OAuth consumer details service to use.
   */
  public void setConsumerDetailsService(BeanReference consumerDetailsService) {
    this.consumerDetailsService = consumerDetailsService;
  }
}
