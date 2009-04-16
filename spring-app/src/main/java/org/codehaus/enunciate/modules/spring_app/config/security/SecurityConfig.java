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

import java.util.*;

/**
 * <h1><a name="security">Spring Application Security</a></h1>
 *
 * <p>Enunciate provides a mechanism for securing your Web service API by leveraging the capabilities of <a href="http://static.springframework.org/spring-security/site/">Spring Security</a>
 * (soon to be known as Spring Security).</p>
 *
 * <p>Spring Security provides a variety of different ways to secure a Spring application.  These include:</p>
 *
 * <ul>
 *  <li><a href="http://static.springframework.org/spring-security/site/reference/html/springsecurity.html#basic">HTTP basic authentication</a></li>
 *  <li><a href="http://static.springframework.org/spring-security/site/reference/html/springsecurity.html#digest">HTTP digest authentication</a></li>
 *  <li><a href="http://static.springframework.org/spring-security/site/reference/html/springsecurity.html#form">Form-based login</a></li>
 *  <li><a href="http://static.springframework.org/spring-security/site/reference/html/springsecurity.html#remember-me">Remember-me token</a></li>
 *  <li><a href="http://static.springframework.org/spring-security/site/reference/html/springsecurity.html#x509">X509 Authentication</a></li>
 *  <li><a href="http://static.springframework.org/spring-security/site/reference/html/springsecurity.html#ldap">LDAP Authentication</a></li>
 *  <li>Etc.</li>
 * </ul>
 *
 * <p>In addition to the authentication mechanisms listed above, Spring Security provides other security features for a Spring-based web application.  These include:</p>
 *
 * <ul>
 *   <li><a href="http://static.springframework.org/spring-security/site/reference/html/springsecurity.html#anonymous">Anonymous identity loading</a></li>
 *   <li>Identity persistence across the HTTP Session</li>
 *   <li>Integration with the standard J2EE security context</li>
 *   <li><a href="http://static.springframework.org/spring-security/site/reference/html/springsecurity.html#secure-objects">Secure objects</a></li>
 *   <li><a href="http://static.springframework.org/spring-security/site/reference/html/springsecurity.html#runas">Run-as authentication replacement</a></li>
 * </ul>
 *
 * <p>Security, by nature, is very complex.  Enunciate attempts to provide an intuitive configuration mechanism for the most common security cases, but also
 * provides a means to enable a more advanced security policy.</p>
 *
 * <p>Enunciate will secure your Web service API according to the configuration you provide in the Enunciate configuration file and
 * via annotations on your service endpoints. <i>Note that security will not be enabled unless the "enableSecurity" attribute is set to "true" on the "spring-app"
 * element of the configuration file.</i></p>
 *
 * <h1><a name="security_annotations">Security Annotations</a></h1>
 *
 * <p>Web service endpoints are secured via the <a href="http://jcp.org/en/jsr/detail?id=250">JSR-250</a>-defined security annotations: <tt>javax.annotation.security.RolesAllowed</tt>,
 * <tt>javax.annotation.security.PermitAll</tt>, and <tt>javax.annotation.security.DenyAll</tt>.  These annotations can be applied to your endpoint interface methods to specify
 * the user roles that are allowed to access these methods and resources. In accordance with the JSR-250 specifiction, these annotations can be applied
 * at either the class-or-interface-level or at the method-level, with the roles granted at the method-level overriding those granted at the
 * class-or-interface-level.</p>
 *
 * <p>If no security annotations are applied to a method nor to its endpoint interface, then no security policy will be applied to the method and access will be
 * open publicly to all users.</p>
 *
 * <h1><a name="security_user_details">User Details Service</a></h1>
 *
 * <p>In order for a security policy to be implemented, Spring Security must know how to load a user and have access to that user's roles and credentials. You must define
 * in instance of <tt>org.springframework.security.userdetails.UserDetailsService</tt> in your own spring bean definition file and
 * <a href="module_spring_app.html#config_springImport">import</a> that file into the spring application context</a>.  Alternatively, you may define a class that implements
 * <tt>org.springframework.security.userdetails.UserDetailsService</tt> and specify that class with the "userDetailsService" child element of the "security"
 * element in the Enunciate configuration file (see below).</p>
 *
 * <h1><a name="security_config">The "security" configuration element</a></h1>
 *
 * <p>By default, Enunciate will use Spring Security to secure your Web service endpoints via HTTP Basic Authentication using your
 * <a href="module_spring_app_security.html#security_user_details">UserDetailsService</a> and your
 * <a href="module_spring_app_security.html#security_annotations">security annotations</a>. You can customize the security
 * mechanism that is used by using the "security" child element of the "spring_app" element in the Enunciate configuration file.</p>
 *
 * <p>The following example shows the structure of the security configuration elements.  Note that this shows only the structure.
 * Some configuration elements don't make sense when used together.</p>
 *
 * <code class="console">
 * &lt;enunciate&gt;
 * &nbsp;&nbsp;&lt;modules&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;spring-app ...&gt;
 * 
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;security enableFormBasedLogin="[true|false]" key="..." realmName="..."
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;enableRememberMeToken="[true|false]" enableFormBasedLogout="[true|false]"
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;loadAnonymousIdentity="[true|false]" enableBasicHTTPAuth="[true|false]"
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;enableDigestHTTPAuth="[true|false]" enableOAuth="[true|false]"
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;initJ2EESecurityContext="[true|false]"&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;userDetailsService beanName="..." className="..."/&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;anonymous key="..." userId="..." roles="..."/&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;basicAuth realmName="..."/&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;digestAuth key="..." realmName="..." nonceValiditySeconds="..."/&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;formBasedLogin url="..." redirectOnSuccessUrl="..." redirectOnFailureUrl="..."
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;loginPageURL="..." loginPageFile="..."/&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;oauth infoURL="..." infoPageFile="..." requestTokenURL="..."
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;accessConfirmationURL="..." confirmAccessPageFile="..."
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;accessConfirmedURL="..." accessConfirmedPageFile="..."
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;grantAccessURL="..." accessTokenURL="..."/&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;formBasedLogout url="..." redirectOnSuccessUrl="..."/&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;rememberMe key="..."/&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;onAuthenticationFailed redirectTo="..."&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;entryPoint beanName="..." className="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/onAuthenticationFailed&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;onAccessDenied redirectTo="..."/&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;filter beanName="..." className="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;filter beanName="..." className="..."/&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;url pattern="..." rolesAllowed="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;url pattern="..." rolesAllowed="..."/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;/security&gt;
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/spring-app&gt;
 * &nbsp;&nbsp;&lt;/modules&gt;
 * &lt;/enunciate&gt;
 * </code>
 *
 * <h3>attributes</h3>
 *
 * <p>The "security" element supports the following attributes:</p>
 *
 * <ul>
 *   <li>The "enableFormBasedLogin" attribute is used to enable a form-based login endpoint (for example, a browser-submitted form). Default: "false".
 *       This endpoint can be configured with the "formBasedLogin" child element (see below).</li>
 *   <li>The "enableFormBasedLogout" attribute is used to enable a form-based logout endpoint (for example, a browser-submitted form). Default: "false".
 *       This endpoint can be configured with the "formBasedLogout" child element (see below).</li>
 *   <li>The "enableRememberMeToken" attribute is used to enable Spring Security to set a remember-me token as a cookie in the HTTP response which can be used to
 *       "remember" the identity for a specified time.  Default: "false". Remember-me services can be configured with the "rememberMe" child element
 *       (see below).</li>
 *   <li>The "loadAnonymousIdentity" attribute is used to enable Spring Security to load an anonymous identity if no authentication is provided.  Default: "true". The
 *       anonymous identity loading behavior can be configured with the "anonymous" child element (see below).</li>
 *   <li>The "enableBasicHTTPAuth" attribute is used to enable HTTP Basic Authentication.  Default: "true". HTTP Basic Auth can be configured with the
 *       "basicAuth" child element (see below).</li>
 *   <li>The "enableDigestHTTPAuth" attribute is used to enable HTTP Digest Authentication.  Default: "false". HTTP Digest Auth can be configured with the
 *       "digestAuth" child element (see below).</li>
 *   <li>The "enableOAuth" attribute is used to enable <a href="http://oauth.net">OAuth</a>.  Default: "false". OAuth can be configured with the
 *       "oauth" child element (see below).</li>
 *   <li>The "initJ2EESecurityContext" attribute is used to enable Spring Security to initialize the J2EE security context with the attributes of the current identity.
 *       Default: "true".</li>
 *   <li>The "applySecurityToSOAPUrls" attribute is used to apply security to the SOAP URLs. This means that SOAP URLs will be secured to the roles specified by
 *       the @RolesAllowed annotation on the web service class. This is different from the security that is applied to the SOAP endpoints, which is always applied.
 *       Default: "false".</li>
 *   <li>The "key" attribute is used to specify the default security key that is to be used as necessary for security hashes. etc. If not supplied, a random
 *       default will be provided.</li>
 *   <li>The "realmName" attribute is used to specify the default realm name to be used for the authentication mechanisms that require it (e.g. HTTP Basic Auth,
 *       HTTP Digest Auth). The default value is "Spring Security Application".</li>
 *   <li>The "disableDefaultProvider" attribute is used to disable the Enunciate-supplied default authentication provider. If the Enunciate-supplied provider is
 *       disabled, one will have to be custom-provided in the spring context. Note that setting this to "true" effectively causes Enunciate to ignore the
 *       "userDetailsService" child element. Default: "false".</li>
 * </ul>
 *
 * <p>The "security" element also supports a number of child elements that can be used to further configure the Web service security mechanism.</p>
 *
 * <h3>userDetailsService</h3>
 *
 * <p>The "userDetailsService" child element is used to specify the implementation of <a href="http://static.springframework.org/spring-security/site/apidocs/org/springframework/security/userdetails/UserDetailsService.html">org.springframework.security.userdetails.UserDetailsService</a> to use for loading a
 * user. This element supports one of two attributes: "beanName" and "className".  The "beanName" attribute specifies the name of a spring bean to use as the
 * user details service.  The "className" attribute is used to specify the fully-qualified class name of the implementation of UserDetailsService to use.</p>
 *
 * <h3>onAuthenticationFailed</h3>
 *
 * <p>The "onAuthenticationFailed" child element is used to specify the action to take if authentication fails. This element supports a "redirectTo" attribute
 * that will specify that the request is to be redirected to the given URL. The "onAuthenticationFailed" element also supports a child element, "useEntryPoint",
 * that supports one of two attributes: "beanName" and "className".  The "beanName" attribute specifies the name of a spring bean to use as the
 * authentication entry point.  The "className" attribute is used to specify the fully-qualified class name of the authentication entry point to use. The
 * authentcation entry point must implement <a href="http://static.springframework.org/spring-security/site/apidocs/org/springframework/security/ui/AuthenticationEntryPoint.html">org.springframework.security.ui.AuthenticationEntryPoint</a>.</p>
 *
 * <p>The default action Enunciate takes if authentication fails depends on the security configuration.  If HTTP Digest Auth is enabled, the action is to
 * commence digest authentication.  Otherwise, if HTTP Basic Auth is enabled, the default action is to commence basic authentication.  Otherwise, the default
 * action is simply to issue an HTTP 401 (Unauthenticated) error.</p>
 *
 * <h3>onAccessDenied</h3>
 *
 * <p>The "onAccessDenied" child element is used to specify the action to take if access is denied. This element supports a "redirectTo" attribute
 * that will specify that the request is to be redirected to the given URL.</p>
 *
 * <p>The default action Enunciate takes if access is denied is simply to issue an HTTP 403 (Forbidden) error.</p>
 *
 * <h3>anonymous</h3>
 *
 * <p>The "anonymous" child element is used to configure the anonymous identity processing.  The "userId" attribute specifies the id of the anonymous user.
 * The default value is "anonymous".  The "roles" attribute is used to specify the (comma-separated) roles that are applied to the anonymous identity.
 * The default value is "ANONYMOUS".  The "key" attribute specified the anonymous authentication key. If no key is supplied, the key supplied in the general
 * security configuration will be used.</p>
 *
 * <p>For more information about anonymous identity loading, see For more information, see <a href="http://static.springframework.org/spring-security/site/apidocs/org/springframework/security/providers/anonymous/AnonymousProcessingFilter.html">org.springframework.security.providers.anonymous.AnonymousProcessingFilter</a>.</p>
 *
 * <h3>basicAuth</h3>
 *
 * <p>The "basicAuth" child element is used to configure HTTP Basic Auth.  The "realmName" attribute specifies the authentication realm name. The default
 * value is the realm name of the general security configuration.</p>
 *
 * <p>For more information about basic authentication configuration, see <a href="http://static.springframework.org/spring-security/site/apidocs/org/springframework/security/ui/basicauth/BasicProcessingFilter.html">org.springframework.security.ui.basicauth.BasicProcessingFilter</a>.</p>
 *
 * <h3>digestAuth</h3>
 *
 * <p>The "digestAuthConfig" child element is used to configure HTTP Digest Auth.  The "realmName" attribute specifies the authentication realm name. The default
 * value is the realm name of the general security configuration. The "key" attribute is the security key used to encrypt the digest.  The default is the
 * key configured in the general security configuration.  The "nonceValiditySeconds" attribute specifies how long the digest nonce is valid.  The default is
 * 300 seconds.</p>
 *
 * <p>For more information about digest authentication configuration, see <a href="http://static.springframework.org/spring-security/site/apidocs/org/springframework/security/ui/digestauth/DigestProcessingFilter.html">org.springframework.security.ui.digestauth.DigestProcessingFilter</a>.</p>
 *
 * <h3>oauth</h3>
 *
 * <p>The "oauth" child element is used to configure OAuth access.  OAuth is used to grant access to a third party to a specific user's protected resources without requiring
 * that the user give out any credentials to that third party.  To learn more about OAuth, please refer to the <a href="http://oauth.net/">OAuth project</a>.
 * You may also want to read <a href="http://www.hueniverse.com/hueniverse/2007/10/beginners-gui-1.html">this fine illustration</a> of OAuth in action.</p>
 *
 * <p>To enable OAuth, you must provide an implementation of <a href="http://spring-security-oauth.codehaus.org/apidocs/org/springframework/security/oauth/provider/ConsumerDetailsService.html">org.springframework.security.oauth.provider.ConsumerDetailsService</a>,
 * which you can do by defining the bean in your own spring bean configuration file (and importing it) or by using the configuration of the "oauth" element (see below.)</p>
 *
 * <p>The "oauth" element supports the following attributes:</p>
 *
 * <ul>
 *   <li>The "infoURL" attribute is used to specify the URL at which the OAuth info page is mounted. This is a simple JSP page that will display the URLs of the
 *       relevant endpoints needed for an OAuth consumer. The default is "/oauth/info.html".</li>
 *   <li>The "infoPageFile" attribute is used to specify a JSP file on the filesystem that will be used to display the info page. If none is provided, 
 *       a default will be supplied.</li>
 *   <li>The "accessConfirmationURL" attribute is used to specify the URL at which the user will need to be redirected to confirm access to the protected
 *       resource. The default is "/oauth/confirm_access".</li>
 *   <li>The "confirmAccessPageFile" attribute is used to specify a JSP file on the filesystem that will be used to display the page to confirm access. If none is provided,
 *       a default will be supplied.</li>
 *   <li>The "accessConfirmedURL" attribute is used to specify the URL to which the user will be redirected after confirming access if no callback URL was
 *       supplied by the consumer. The default is "/oauth/access_confirmed".</li>
 *   <li>The "accessConfirmedPageFile" attribute is used to specify a JSP file on the filesystem that will be used to display the access confirmation. If none is provided,
 *       a default will be supplied.</li>
 *   <li>The "requestTokenURL" attribute is used to specify the URL at which a request for an OAuth request token will be handled.  Default:
 *       "/oauth/request_token".</li>
 *   <li>The "grantAccessURL" attribute is used to specify the URL at which a request to confirm access will be handled.  Default:
 *       "/oauth/authorize".</li>
 *   <li>The "accessTokenURL" attribute is used to specify the URL at which a request for an OAuth access token will be handled.  Default:
 *       "/oauth/access_token".</li>
 * </ul>
 *
 * <p>The "oauth" element supports two child elements, "tokenServices" and "consumerDetailsService".  Each of these elements support two attributes, "beanName"
 * and "className" which can be used to specify the implementation of OAuthProviderTokenServices and ConsumerDetailsService to use.  The "beanName" attribute
 * is used to specify the name of a spring bean.  The "className" attribute is used to specify the FQN of the implementation class. It should be noted that
 * if these elements are not supplied, an instance of OAuthProviderTokenServices and ConsumerDetailsService will attempt to be looked up in the context. If
 * an instance for either of these is not found, OAuth will fail.</p>
 *
 * <p>For more information about OAuth configuration, refer to <a href="http://spring-security-oauth.codehaus.org/">OAuth for Spring Security</a>.</p>
 *
 * <h3>formBasedLogin</h3>
 *
 * <p>The "formBasedLogin" child element is used to configure the form-based login endpoint.  The "url" attribute specifies the URL where the form-based
 * login will be mounted.  The default is "/form/login".  The "loginPageFile" attribute is used to specify a JSP file on the filesystem that will be
 * used to display the login page. If none is provided, a default will be supplied. The "loginPageURL" is the URL at which said loginPageFile will be mounted.
 * The default is "login.jsp". The "redirectOnSuccessUrl" specifies the URL to which a successful 
 * login will be redirected. The default value is "/".  The "redirectOnFailureUrl" specifies the URL to which an unsuccessful login will be redirected.
 * The default value is "/".</p>
 *
 * <p>For more information about the form-based login endpoint, see <a href="http://static.springframework.org/spring-security/site/apidocs/org/springframework/security/ui/webapp/AuthenticationProcessingFilter.html">org.springframework.security.ui.webapp.AuthenticationProcessingFilter</a>.</p>
 *
 * <h3>formBasedLogout</h3>
 *
 * <p>The "formBasedLogout" child element is used to configure the form-based logout endpoint.  The "url" attribute specifies the URL where the form-based
 * logout will be mounted.  The default is "/form/logout".  The "redirectOnSuccessUrl" specifies the URL to which a successful logout will be redirected. The
 * default value is "/".</p>
 *
 * <p>For more information about the form-based logout endpoint, see <a href="http://static.springframework.org/spring-security/site/apidocs/org/springframework/security/ui/logout/LogoutFilter.html">org.springframework.security.ui.logout.LogoutFilter</a>.</p>
 *
 * <h3>rememberMe</h3>
 *
 * <p>The "rememberMe" child element is used to configure the remember-me identity processing.  The "key" attribute specifies the security key that will
 * be used to encode the remember-me token.  The default value is the key supplied in the general security configuration.</p>
 *
 * <p>For more information about remember-me identity processing, see <a href="http://static.springframework.org/spring-security/site/apidocs/org/springframework/security/ui/rememberme/TokenBasedRememberMeServices.html">org.springframework.security.ui.rememberme.TokenBasedRememberMeServices</a>.</p>
 *
 * <h3>url</h3>
 *
 * <p>The "url" child element specifies another URL pattern (in addition to the Web service endpoints) to which the security policy is to be applied.  This
 * can be used, for example, to secure static and/or internal resources in your web application (e.g. images, jsps, etc.).  The "url" element requires a "pattern"
 * attribute that is used to apply a URL pattern to which the security policy is to be applied.  The pattern is interpresed per the "url-pattern" element
 * of the servlet spec.  The "url" element also supports an optional "rolesAllowed" attribute that is a comma-separated list of roles that are allowed to
 * access the URL at the specified pattern.</p>
 *
 * <h3>filter</h3>
 *
 * <p>The "filter" child element specifies another security filter to be applied to provide securit services. The "filter"
 * element supports one of two attributes: "beanName" and "className".  The "beanName" attribute specifies the name of a spring bean to use as the
 * security filter.  The "className" attribute is used to specify the fully-qualified class name of the security filter to use. A
 * security filter implements the javax.servlet.Filter interface. You can provide any number of additional
 * security filters to Enunciate (e.g. <a href="http://static.springframework.org/spring-security/site/apidocs/org/springframework/security/ui/x509/X509ProcessingFilter.html">X509ProcessingFilter</a>, <a href="http://static.springframework.org/spring-security/site/apidocs/org/springframework/security/providers/siteminder/SiteminderAuthenticationProvider.html">SiteminderAuthenticationProcessingFilter</a>, etc.).</p>
 *
 * <h3>primaryProvider</h3>
 *
 * <p>The "primaryProvider" child element specifies the primary Spring Security authentication provider that is to be used to provide authentication services.
 * The "primaryProvider" element supports one of two attributes: "beanName" and "className".  The "beanName" attribute specifies the name of a spring bean to use as the
 * primary authentication provider.  The "className" attribute is used to specify the fully-qualified class name of the authentication provider to use. The
 * primary authentication provider implements the <a href="http://static.springframework.org/spring-security/site/apidocs/org/springframework/security/providers/AuthenticationProvider.html">org.springframework.security.providers.AuthenticationProvider</a> interface.</p>
 *
 * <h1><a name="security_login_logout">Login and Logout API Methods</a></h1>
 *
 * <p>You may be interested in implementing "login" and "logout" Web service API methods. To do this, you may
 * <a href="http://static.springframework.org/spring/docs/2.5.x/reference/beans.html#beans-annotation-config">autowire</a> an instance of
 * <tt>org.codehaus.enunciate.modules.spring_app.LoginLogoutHelper</tt> into your service endpoint bean. The definition of your login method may
 * then delegate the login and logout method calls to the supplied <tt>LoginLogoutHelper</tt>, which will put/remove the identity in the
 * current Spring Security security context.  (Note that if you want this identity to persist across the HTTP session, make sure
 * that "persistIdentityAcrossHttpSession" on the security config is set to "true".)</p>
 *
 * @author Ryan Heaton
 * @docFileName module_spring_app_security.html
 */
public class SecurityConfig {

  private boolean enableFormBasedLogin = false;
  private boolean enableFormBasedLogout = false;
  private boolean enableRememberMeToken = false;
  private boolean loadAnonymousIdentity = true;
  private boolean enableBasicHTTPAuth = true;
  private boolean enableDigestHTTPAuth = false;
  private boolean initJ2EESecurityContext = true;
  private boolean applySecurityToSOAPUrls = false;
  private boolean enableOAuth = false;

  private boolean disableDefaultProvider = false;

  private AnonymousConfig anonymousConfig = new AnonymousConfig();
  private BasicAuthConfig basicAuthConfig = new BasicAuthConfig();
  private DigestAuthConfig digestAuthConfig = new DigestAuthConfig();
  private FormBasedLoginConfig formBasedLoginConfig = new FormBasedLoginConfig();
  private FormBasedLogoutConfig formBasedLogoutConfig = new FormBasedLogoutConfig();
  private RememberMeConfig rememberMeConfig = new RememberMeConfig();
  private OAuthConfig OAuthConfig = new OAuthConfig();

  private EntryPointConfig onAuthenticationFailed;
  private EntryPointConfig onAccessDenied;

  private BeanReference userDetailsService;
  private List<BeanReference> additionalAuthenticationFilters;

  private final Map<String, String> secureUrls = new LinkedHashMap<String, String>();

  //global defaults
  private String key;
  private String realmName;

  /**
   * Whether to enable form-based login.
   *
   * @return Whether to enable form-based login.
   */
  public boolean isEnableFormBasedLogin() {
    return enableFormBasedLogin;
  }

  /**
   * Whether to enable form-based login.
   *
   * @param enableFormBasedLogin Whether to enable form-based login.
   */
  public void setEnableFormBasedLogin(boolean enableFormBasedLogin) {
    this.enableFormBasedLogin = enableFormBasedLogin;
  }

  /**
   * Whether to disable the default authentication provider (required one to be user-supplied).  Default: false.
   *
   * @return Whether to disable the default authentication provider (required one to be user-supplied).  Default: false.
   */
  public boolean isDisableDefaultProvider() {
    return disableDefaultProvider;
  }

  /**
   * Whether to disable the default authentication provider (required one to be user-supplied).  Default: false.
   *
   * @param disableDefaultProvider Whether to disable the default authentication provider (required one to be user-supplied).  Default: false.
   */
  public void setDisableDefaultProvider(boolean disableDefaultProvider) {
    this.disableDefaultProvider = disableDefaultProvider;
  }

  /**
   * Whether to enable form-based logout.
   *
   * @return Whether to enable form-based logout.
   */
  public boolean isEnableFormBasedLogout() {
    return enableFormBasedLogout;
  }

  /**
   * Whether to enable form-based logout.
   *
   * @param enableFormBasedLogout Whether to enable form-based logout.
   */
  public void setEnableFormBasedLogout(boolean enableFormBasedLogout) {
    this.enableFormBasedLogout = enableFormBasedLogout;
  }

  /**
   * Whether to enable the remember-me token.
   *
   * @return Whether to enable the remember-me token.
   */
  public boolean isEnableRememberMeToken() {
    return enableRememberMeToken;
  }

  /**
   * Whether to enable the remember-me token.
   *
   * @param enableRememberMeToken Whether to enable the remember-me token.
   */
  public void setEnableRememberMeToken(boolean enableRememberMeToken) {
    this.enableRememberMeToken = enableRememberMeToken;
  }

  /**
   * Whether to load the anonymous identity.
   *
   * @return Whether to load the anonymous identity.
   */
  public boolean isLoadAnonymousIdentity() {
    return loadAnonymousIdentity;
  }

  /**
   * Whether to load the anonymous identity.
   *
   * @param loadAnonymousIdentity Whether to load the anonymous identity.
   */
  public void setLoadAnonymousIdentity(boolean loadAnonymousIdentity) {
    this.loadAnonymousIdentity = loadAnonymousIdentity;
  }

  /**
   * Whether to enable HTTP basic auth.
   *
   * @return Whether to enable HTTP basic auth.
   */
  public boolean isEnableBasicHTTPAuth() {
    return enableBasicHTTPAuth;
  }

  /**
   * Whether to enable HTTP basic auth.
   *
   * @param enableBasicHTTPAuth Whether to enable HTTP basic auth.
   */
  public void setEnableBasicHTTPAuth(boolean enableBasicHTTPAuth) {
    this.enableBasicHTTPAuth = enableBasicHTTPAuth;
  }

  /**
   * Whether to enable HTTP digest auth.
   *
   * @return Whether to enable HTTP digest auth.
   */
  public boolean isEnableDigestHTTPAuth() {
    return enableDigestHTTPAuth;
  }

  /**
   * Whether to enable HTTP digest auth.
   *
   * @param enableDigestHTTPAuth Whether to enable HTTP digest auth.
   */
  public void setEnableDigestHTTPAuth(boolean enableDigestHTTPAuth) {
    this.enableDigestHTTPAuth = enableDigestHTTPAuth;
  }

  /**
   * Whether to initialize the J2EE security context.
   *
   * @return Whether to initialize the J2EE security context.
   */
  public boolean isInitJ2EESecurityContext() {
    return initJ2EESecurityContext;
  }

  /**
   * Whether to initialize the J2EE security context.
   *
   * @param initJ2EESecurityContext Whether to initialize the J2EE security context.
   */
  public void setInitJ2EESecurityContext(boolean initJ2EESecurityContext) {
    this.initJ2EESecurityContext = initJ2EESecurityContext;
  }

  /**
   * Whether to apply security to the soap urls.
   *
   * @return Whether to apply security to the soap urls.
   */
  public boolean isApplySecurityToSOAPUrls() {
    return applySecurityToSOAPUrls;
  }

  /**
   * Whether to apply security to the soap urls.
   *
   * @param applySecurityToSOAPUrls Whether to apply security to the soap urls.
   */
  public void setApplySecurityToSOAPUrls(boolean applySecurityToSOAPUrls) {
    this.applySecurityToSOAPUrls = applySecurityToSOAPUrls;
  }

  /**
   * Whether to enable OAuth provider support.
   *
   * @return Whether to enable OAuth provider support.
   */
  public boolean isEnableOAuth() {
    return enableOAuth;
  }

  /**
   * Whether to enable OAuth provider support.
   *
   * @param enableOAuth Whether to enable OAuth provider support.
   */
  public void setEnableOAuth(boolean enableOAuth) {
    this.enableOAuth = enableOAuth;
  }

  /**
   * Configuration for anonymous user processing.
   *
   * @return Configuration for anonymous user processing.
   */
  public AnonymousConfig getAnonymousConfig() {
    return anonymousConfig;
  }

  /**
   * Configuration for anonymous user processing.
   *
   * @param anonymousConfig Configuration for anonymous user processing.
   */
  public void setAnonymousConfig(AnonymousConfig anonymousConfig) {
    this.anonymousConfig = anonymousConfig;
  }

  /**
   * Configuration for HTTP Basic Auth.
   *
   * @return Configuration for HTTP Basic Auth.
   */
  public BasicAuthConfig getBasicAuthConfig() {
    return basicAuthConfig;
  }

  /**
   * Configuration for HTTP Basic Auth.
   *
   * @param basicAuthConfig Configuration for HTTP Basic Auth.
   */
  public void setBasicAuthConfig(BasicAuthConfig basicAuthConfig) {
    this.basicAuthConfig = basicAuthConfig;
  }

  /**
   * Configuration for HTTP Digest Auth.
   *
   * @return Configuration for HTTP Digest Auth.
   */
  public DigestAuthConfig getDigestAuthConfig() {
    return digestAuthConfig;
  }

  /**
   * Configuration for HTTP Digest Auth.
   *
   * @param digestAuthConfig Configuration for HTTP Digest Auth.
   */
  public void setDigestAuthConfig(DigestAuthConfig digestAuthConfig) {
    this.digestAuthConfig = digestAuthConfig;
  }

  /**
   * Form-based login configuration.
   *
   * @return Form-based login configuration.
   */
  public FormBasedLoginConfig getFormBasedLoginConfig() {
    return formBasedLoginConfig;
  }

  /**
   * Form-based login configuration.
   *
   * @param formBasedLoginConfig Form-based login configuration.
   */
  public void setFormBasedLoginConfig(FormBasedLoginConfig formBasedLoginConfig) {
    this.formBasedLoginConfig = formBasedLoginConfig;
  }

  /**
   * Form-based logout configuration.
   *
   * @return Form-based logout configuration.
   */
  public FormBasedLogoutConfig getFormBasedLogoutConfig() {
    return formBasedLogoutConfig;
  }

  /**
   * Form-based logout configuration.
   *
   * @param formBasedLogoutConfig Form-based logout configuration.
   */
  public void setFormBasedLogoutConfig(FormBasedLogoutConfig formBasedLogoutConfig) {
    this.formBasedLogoutConfig = formBasedLogoutConfig;
  }

  /**
   * Configuration for remember-me processing.
   *
   * @return Configuration for remember-me processing.
   */
  public RememberMeConfig getRememberMeConfig() {
    return rememberMeConfig;
  }

  /**
   * Configuration for remember-me processing.
   *
   * @param rememberMeConfig Configuration for remember-me processing.
   */
  public void setRememberMeConfig(RememberMeConfig rememberMeConfig) {
    this.rememberMeConfig = rememberMeConfig;
  }

  /**
   * Configuration for OAuth.
   *
   * @return Configuration for OAuth.
   */
  public OAuthConfig getOAuthConfig() {
    return OAuthConfig;
  }

  /**
   * Configuration for OAuth.
   *
   * @param OAuthConfig Configuration for OAuth.
   */
  public void setOAuthConfig(OAuthConfig OAuthConfig) {
    this.OAuthConfig = OAuthConfig;
  }

  /**
   * The bean to use for the user details service.
   *
   * @return The bean to use for the user details service.
   */
  public BeanReference getUserDetailsService() {
    return userDetailsService;
  }

  /**
   * The bean to use for the user details service.
   *
   * @param userDetailsService The bean to use for the user details service.
   */
  public void setUserDetailsService(BeanReference userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  /**
   * Any additional authentication service filters.
   *
   * @return Any additional authentication service filters.
   */
  public List<BeanReference> getAdditionalAuthenticationFilters() {
    return additionalAuthenticationFilters;
  }

  /**
   * Any additional authentication service filters.
   *
   * @param additionalAuthenticationFilters Any additional authentication service filters.
   */
  public void setAdditionalAuthenticationFilters(List<BeanReference> additionalAuthenticationFilters) {
    this.additionalAuthenticationFilters = additionalAuthenticationFilters;
  }

  /**
   * Add an additional authentication filter.
   *
   * @param additionalAuthenticationFilter The additional authentication filter.
   */
  public void addAdditionalAuthenticationFilter(BeanReference additionalAuthenticationFilter) {
    if (this.additionalAuthenticationFilters == null) {
      this.additionalAuthenticationFilters = new ArrayList<BeanReference>();
    }

    this.additionalAuthenticationFilters.add(additionalAuthenticationFilter);
  }

  /**
   * The secure urls.
   *
   * @return The secure urls.
   */
  public Map<String, String> getSecureUrls() {
    return secureUrls;
  }

  /**
   * Add a secure URL.
   *
   * @param pattern The pattern for the secure URL.
   * @param rolesAllowed The roles allowed to the secure URL.
   */
  public void addSecureUrl(String pattern, String rolesAllowed) {
    if (pattern == null) {
      throw new IllegalArgumentException("A 'pattern' attribute must be supplied.");
    }

    this.secureUrls.put(pattern, rolesAllowed);
  }

  /**
   * The entry point for the action on authentication failed.
   *
   * @return The entry point for the action on authentication failed.
   */
  public EntryPointConfig getOnAuthenticationFailed() {
    return onAuthenticationFailed;
  }

  /**
   * The entry point for the action on authentication failed.
   *
   * @param onAuthenticationFailed The entry point for the action on authentication failed.
   */
  public void setOnAuthenticationFailed(EntryPointConfig onAuthenticationFailed) {
    this.onAuthenticationFailed = onAuthenticationFailed;
  }

  /**
   * The entry point for the action on access denied.
   *
   * @return The entry point for the action on access denied.
   */
  public EntryPointConfig getOnAccessDenied() {
    return onAccessDenied;
  }

  /**
   * The entry point for the action on access denied.
   *
   * @param onAccessDenied The entry point for the action on access denied.
   */
  public void setOnAccessDenied(EntryPointConfig onAccessDenied) {
    this.onAccessDenied = onAccessDenied;
  }

  /**
   * A secure key to use.
   *
   * @return A secure key to use.
   */
  public String getKey() {
    return key;
  }

  /**
   * A secure key to use.
   *
   * @param key A secure key to use.
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * A realm name to use.
   *
   * @return A realm name to use.
   */
  public String getRealmName() {
    return realmName;
  }

  /**
   * A realm name to use.
   *
   * @param realmName A realm name to use.
   */
  public void setRealmName(String realmName) {
    this.realmName = realmName;
  }
}
