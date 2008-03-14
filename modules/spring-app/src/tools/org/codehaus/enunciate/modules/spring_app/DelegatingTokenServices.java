package org.codehaus.enunciate.modules.spring_app;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.oauth.provider.token.OAuthAccessProviderToken;
import org.springframework.security.oauth.provider.token.OAuthProviderToken;
import org.springframework.security.oauth.provider.token.OAuthProviderTokenServices;

import java.util.Map;

/**
 * Token services that looks up a delegate in the application context.  If no token services
 * are configured in the application context, there will be no users able to be loaded.
 *
 * @author Ryan Heaton
 */
public class DelegatingTokenServices extends ApplicationObjectSupport implements OAuthProviderTokenServices {

  private OAuthProviderTokenServices delegate;

  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    Map<String, OAuthProviderTokenServices> userDetailsServiceMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), OAuthProviderTokenServices.class);
    for (OAuthProviderTokenServices service : userDetailsServiceMap.values()) {
      if (!service.equals(this)) {
        if (delegate != null) {
          throw new ApplicationContextException("There are multiple beans of type org.springframework.security.oauth.provider.token.OAuthProviderTokenServices defined in the context.  Please specify which one to use in the Enunciate configuration file.");
        }
        else {
          delegate = service;
        }
      }
    }
  }

  public OAuthProviderToken getToken(String token) throws AuthenticationException {
    if (delegate == null) {
      throw new IllegalStateException("No token services are configured for this project.");
    }
    return delegate.getToken(token);
  }

  public OAuthProviderToken createUnauthorizedRequestToken(String consumerKey) throws AuthenticationException {
    if (delegate == null) {
      throw new IllegalStateException("No token services are configured for this project.");
    }
    return delegate.createUnauthorizedRequestToken(consumerKey);
  }

  public void authorizeRequestToken(String requestToken, Authentication authentication) throws AuthenticationException {
    if (delegate == null) {
      throw new IllegalStateException("No token services are configured for this project.");
    }
    delegate.authorizeRequestToken(requestToken, authentication);
  }

  public OAuthAccessProviderToken createAccessToken(String requestToken) throws AuthenticationException {
    if (delegate == null) {
      throw new IllegalStateException("No token services are configured for this project.");
    }
    return delegate.createAccessToken(requestToken);
  }
}