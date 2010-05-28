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

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.MethodSecurityMetadataSource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Enunciate-specific attributes for Web service method security.
 *
 * @author Ryan Heaton
 */
public class WebMethodSecurityMetadataSource implements MethodSecurityMetadataSource {

  private final Collection<MethodSecurityMetadataSource> metadataSources;

  public WebMethodSecurityMetadataSource(Collection<MethodSecurityMetadataSource> metadataSources) {
    this.metadataSources = metadataSources;
  }

  public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
    ArrayList<ConfigAttribute> attributes = new ArrayList<ConfigAttribute>();

    for (MethodSecurityMetadataSource metadataSource : this.metadataSources) {
      attributes.addAll(metadataSource.getAttributes(method, targetClass));
    }

    return attributes;
  }

  public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
    ArrayList<ConfigAttribute> attributes = new ArrayList<ConfigAttribute>();

    for (MethodSecurityMetadataSource metadataSource : this.metadataSources) {
      attributes.addAll(metadataSource.getAttributes(object));
    }

    return attributes;
  }

  public Collection<ConfigAttribute> getAllConfigAttributes() {
    ArrayList<ConfigAttribute> attributes = new ArrayList<ConfigAttribute>();

    for (MethodSecurityMetadataSource metadataSource : this.metadataSources) {
      Collection<ConfigAttribute> allConfigAttributes = metadataSource.getAllConfigAttributes();
      if (allConfigAttributes != null) {
        attributes.addAll(allConfigAttributes);
      }
    }

    return attributes.isEmpty() ? null : attributes;
  }

  public boolean supports(Class<?> clazz) {
    for (MethodSecurityMetadataSource metadataSource : this.metadataSources) {
      if (metadataSource.supports(clazz)) {
        return true;
      }
    }

    return false;
  }
}