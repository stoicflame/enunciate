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

import org.acegisecurity.vote.AccessDecisionVoter;
import org.acegisecurity.ConfigAttribute;
import org.acegisecurity.Authentication;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.GrantedAuthority;

import java.util.Iterator;

/**
 * Voter on JSR-250 configuration attributes.
 *
 * @author Ryan Heaton
 */
public class JSR250Voter implements AccessDecisionVoter {

  /**
   * The specified config attribute is supported if its an instance of a {@link org.codehaus.enunciate.modules.spring_app.JSR250SecurityConfig}.
   *
   * @param configAttribute The config attribute.
   * @return whether the config attribute is supported.
   */
  public boolean supports(ConfigAttribute configAttribute) {
    return configAttribute instanceof JSR250SecurityConfig;
  }

  /**
   * All classes are supported.
   *
   * @param clazz the class.
   * @return true
   */
  public boolean supports(Class clazz) {
    return true;
  }

  /**
   * Votes according to JSR 250.
   *
   * @param authentication The authentication object.
   * @param object The access object.
   * @param definition The configuration definition.
   * @return The vote.
   */
  public int vote(Authentication authentication, Object object, ConfigAttributeDefinition definition) {
    int result = ACCESS_ABSTAIN;
    Iterator iter = definition.getConfigAttributes();

    while (iter.hasNext()) {
      ConfigAttribute attribute = (ConfigAttribute) iter.next();

      if (JSR250SecurityConfig.PERMIT_ALL_ATTRIBUTE.equals(attribute)) {
        return ACCESS_GRANTED;
      }
      else if (JSR250SecurityConfig.DENY_ALL_ATTRIBUTE.equals(attribute)) {
        return ACCESS_DENIED;
      }
      else if (supports(attribute)) {
        result = ACCESS_DENIED;

        // Attempt to find a matching granted authority
        for (GrantedAuthority authority : authentication.getAuthorities()) {
          if (attribute.getAttribute().equals(authority.getAuthority())) {
            return ACCESS_GRANTED;
          }
        }
      }
    }

    return result;
  }
}
