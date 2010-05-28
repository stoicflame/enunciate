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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Simple authentication provider that supports autowiring.
 *
 * @author Ryan Heaton
 */
public class DefaultEnunciateAuthenticationProvider extends DaoAuthenticationProvider {

  @Autowired (required = false)
  @Override
  public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
    super.setPasswordEncoder(passwordEncoder);
  }

  @Autowired (required = false)
  @Override
  public void setSaltSource(SaltSource saltSource) {
    super.setSaltSource(saltSource);
  }

  @Autowired (required = false)
  @Override
  public void setUserDetailsService(UserDetailsService userDetailsService) {
    super.setUserDetailsService(userDetailsService);
  }

  @Autowired (required = false)
  @Override
  public void setUserCache(UserCache userCache) {
    super.setUserCache(userCache);
  }
}
