package org.codehaus.enunciate.modules.spring_app.config.security;

import java.util.List;
import java.util.ArrayList;

/**
 * Spring security configuration.
 *
 * @author Ryan Heaton
 */
public class SecurityConfig {

  private boolean enableFormBasedLogin = false;
  private boolean enableFormBasedLogout = false;
  private boolean persistIdentityAcrossHttpSession = false;
  private boolean enableRememberMeToken = false;
  private boolean loadAnonymousIdentity = true;
  private boolean enableBasicHTTPAuth = true;
  private boolean enableDigestHTTPAuth = false;
  private boolean initJ2EESecurityContext = true;

  private AnonymousConfig anonymousConfig = new AnonymousConfig();
  private BasicAuthConfig basicAuthConfig = new BasicAuthConfig();
  private DigestAuthConfig digestAuthConfig = new DigestAuthConfig();
  private FormBasedLoginConfig formBasedLoginConfig = new FormBasedLoginConfig();
  private FormBasedLogoutConfig formBasedLogoutConfig = new FormBasedLogoutConfig();
  private RememberMeConfig rememberMeConfig = new RememberMeConfig();

  private EntryPointConfig onAuthenticationFailed;
  private EntryPointConfig onAccessDenied;

  private BeanReference userDetailsService;
  private List<BeanReference> additionalAuthenticationProviders;

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
   * Whether to persist the identity across the HTTP session.
   *
   * @return Whether to persist the identity across the HTTP session.
   */
  public boolean isPersistIdentityAcrossHttpSession() {
    return persistIdentityAcrossHttpSession;
  }

  /**
   * Whether to persist the identity across the HTTP session.
   *
   * @param persistIdentityAcrossHttpSession Whether to persist the identity across the HTTP session.
   */
  public void setPersistIdentityAcrossHttpSession(boolean persistIdentityAcrossHttpSession) {
    this.persistIdentityAcrossHttpSession = persistIdentityAcrossHttpSession;
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
   * Any additional authentication service providers.
   *
   * @return Any additional authentication service providers.
   */
  public List<BeanReference> getAdditionalAuthenticationProviders() {
    return additionalAuthenticationProviders;
  }

  /**
   * Any additional authentication service providers.
   *
   * @param additionalAuthenticationProviders Any additional authentication service providers.
   */
  public void setAdditionalAuthenticationProviders(List<BeanReference> additionalAuthenticationProviders) {
    this.additionalAuthenticationProviders = additionalAuthenticationProviders;
  }

  /**
   * Add an additional authentication provider.
   *
   * @param additionalAuthenticationProvider The additional authentication provider.
   */
  public void addAdditionalAuthenticationProvider(BeanReference additionalAuthenticationProvider) {
    if (this.additionalAuthenticationProviders == null) {
      this.additionalAuthenticationProviders = new ArrayList<BeanReference>();
    }

    this.additionalAuthenticationProviders.add(additionalAuthenticationProvider);
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
