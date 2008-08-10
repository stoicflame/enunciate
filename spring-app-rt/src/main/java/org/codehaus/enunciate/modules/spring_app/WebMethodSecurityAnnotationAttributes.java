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

import org.springframework.metadata.Attributes;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Enunciate-specific attributes for Web service method security.
 *
 * @author Ryan Heaton
 */
public class WebMethodSecurityAnnotationAttributes implements Attributes {

  private final Collection<Attributes> methodAttributes;

  public WebMethodSecurityAnnotationAttributes(Collection<Attributes> methodAttributes) {
    this.methodAttributes = methodAttributes;
  }

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
    ArrayList attributes = new ArrayList();

    for (Attributes delegate : getMethodAttributes()) {
      attributes.addAll(delegate.getAttributes(targetMethod));
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

  public Collection<Attributes> getMethodAttributes() {
    return methodAttributes;
  }
}