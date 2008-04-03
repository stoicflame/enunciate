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