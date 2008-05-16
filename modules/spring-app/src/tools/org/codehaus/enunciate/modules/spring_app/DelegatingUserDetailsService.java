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

import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.context.ApplicationContextException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;

import java.util.Map;

/**
 * User details service that looks up a delegate in the application context.  If no user details service
 * is configured in the application context, there will be no users abled to be loaded.
 *
 * @author Ryan Heaton
 */
public class DelegatingUserDetailsService extends ApplicationObjectSupport implements UserDetailsService {

  private UserDetailsService delegate;

  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    Map<String, UserDetailsService> userDetailsServiceMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), UserDetailsService.class);
    for (UserDetailsService userDetailsService : userDetailsServiceMap.values()) {
      if (!userDetailsService.equals(this)) {
        if (delegate != null) {
          throw new ApplicationContextException("There are multiple beans of type org.springframework.security.userdetails.UserDetailsService defined in the context.  Please specify which one to use in the Enunciate configuration file.");
        }
        else {
          delegate = userDetailsService;
        }
      }
    }
  }

  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
    if (delegate == null) {
      throw new UsernameNotFoundException("No user details service is configured for this system.");
    }
    else {
      return delegate.loadUserByUsername(username);
    }
  }

}