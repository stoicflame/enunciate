package org.codehaus.enunciate.modules.spring_app;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.oauth.common.OAuthException;
import org.springframework.security.oauth.provider.ConsumerDetails;
import org.springframework.security.oauth.provider.ConsumerDetailsService;

import java.util.Map;

/**
 * Consumer details service that looks up a delegate in the application context.  If no consumer details service
 * is configured in the application context, there will be no consumers able to be loaded.
 *
 * @author Ryan Heaton
 */
public class DelegatingConsumerDetailsService extends ApplicationObjectSupport implements ConsumerDetailsService {

  private ConsumerDetailsService delegate;

  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    Map<String, ConsumerDetailsService> userDetailsServiceMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), ConsumerDetailsService.class);
    for (ConsumerDetailsService userDetailsService : userDetailsServiceMap.values()) {
      if (!userDetailsService.equals(this)) {
        if (delegate != null) {
          throw new ApplicationContextException("There are multiple beans of type org.springframework.security.oauth.provider.ConsumerDetailsService defined in the context.  Please specify which one to use in the Enunciate configuration file.");
        }
        else {
          delegate = userDetailsService;
        }
      }
    }
  }

  public ConsumerDetails loadConsumerByConsumerKey(String consumerKey) throws OAuthException {
    if (delegate == null) {
      throw new OAuthException("No consumer details service is configured for this system.");
    }
    else {
      return delegate.loadConsumerByConsumerKey(consumerKey);
    }
  }

}