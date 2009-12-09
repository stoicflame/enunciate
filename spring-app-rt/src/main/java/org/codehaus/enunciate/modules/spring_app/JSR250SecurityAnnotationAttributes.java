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

import org.springframework.security.SecurityConfig;

import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;
import javax.annotation.security.RolesAllowed;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Ryan Heaton
 */
public class JSR250SecurityAnnotationAttributes implements org.springframework.metadata.Attributes {

  // Inherited.
  public Collection getAttributes(Class targetClass) {
    return Collections.emptyList();
  }

  // Inherited.
  public Collection getAttributes(Class targetClass, Class filter) {
    throw new UnsupportedOperationException();
  }

  // Inherited.
  public Collection getAttributes(Method targetMethod) {
    ArrayList<SecurityConfig> attributes = new ArrayList<SecurityConfig>();

    //todo: throw an exception if two or more of @DenyAll, @PermitAll, and @RolesAllowed are present (illegal according to JSR250)?

    DenyAll denyAll = targetMethod.getAnnotation(DenyAll.class);
    if (denyAll != null) {
      attributes.add(JSR250SecurityConfig.DENY_ALL_ATTRIBUTE);
    }
    else {
      PermitAll permitAll = targetMethod.getAnnotation(PermitAll.class);
      if (permitAll != null) {
        return Collections.emptyList();
      }
      else {
        RolesAllowed rolesAllowed = targetMethod.getAnnotation(RolesAllowed.class);
        if (rolesAllowed != null) {
          for (String role : rolesAllowed.value()) {
            attributes.add(new JSR250SecurityConfig(role));
          }
        }
        else {
          //now check the class-level attributes:
          denyAll = targetMethod.getDeclaringClass().getAnnotation(DenyAll.class);
          if (denyAll != null) {
            attributes.add(JSR250SecurityConfig.DENY_ALL_ATTRIBUTE);
          }
          else {
            permitAll = targetMethod.getDeclaringClass().getAnnotation(PermitAll.class);
            if (permitAll != null) {
              return Collections.emptyList();
            }
            else {
              rolesAllowed = targetMethod.getDeclaringClass().getAnnotation(RolesAllowed.class);
              if (rolesAllowed != null) {
                for (String role : rolesAllowed.value()) {
                  attributes.add(new JSR250SecurityConfig(role));
                }
              }
            }
          }
        }
      }
    }

    return attributes;
  }

  // Inherited.
  public Collection getAttributes(Method targetMethod, Class filter) {
    throw new UnsupportedOperationException();
  }

  // Inherited.
  public Collection getAttributes(Field targetField) {
    throw new UnsupportedOperationException();
  }

  // Inherited.
  public Collection getAttributes(Field targetField, Class filter) {
    throw new UnsupportedOperationException();
  }

}
