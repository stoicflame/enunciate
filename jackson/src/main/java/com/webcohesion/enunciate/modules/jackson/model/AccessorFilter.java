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

package com.webcohesion.enunciate.modules.jackson.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;
import com.webcohesion.enunciate.javac.decorations.element.PropertyElement;

import javax.lang.model.element.Modifier;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Filter for potential accessors.
 *
 * @author Ryan Heaton
 */
public class AccessorFilter {

  private final JsonAutoDetect accessType;
  private final Set<String> propertiesToIgnore;

  public AccessorFilter(JsonAutoDetect accessType, JsonIgnoreProperties ignoreProperties) {
    this.accessType = accessType;

    if (accessType == null) {
      throw new IllegalArgumentException("An access type must be specified.");
    }

    this.propertiesToIgnore = new TreeSet<String>();
    if (ignoreProperties != null) {
      Collections.addAll(this.propertiesToIgnore, ignoreProperties.value());
    }
  }

  /**
   * Whether to accept the given member declaration as an accessor.
   *
   * @param element The declaration to filter.
   * @return Whether to accept the given member declaration as an accessor.
   */
  public boolean accept(DecoratedElement<?> element) {
    if (element.getAnnotation(JsonIgnore.class) != null) {
      return false;
    }

    if (element.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class) != null) {
      //if there's an explicit json property annotation, we'll include it.
      return true;
    }

    String name = element.getSimpleName().toString();
    if ("".equals(name)) {
      return false;
    }

    if (this.propertiesToIgnore.contains(name)) {
      return false;
    }

    if (element instanceof PropertyElement) {
      PropertyElement property = ((PropertyElement) element);

      DecoratedExecutableElement getter = property.getGetter();
      if (getter == null) {
        //needs a getter.
        return false;
      }

      JsonAutoDetect.Visibility visibility = getter.getSimpleName().toString().startsWith("is") ? this.accessType.isGetterVisibility() : this.accessType.getterVisibility();
      if (!isVisible(visibility, getter)) {
        return false;
      }

      DecoratedExecutableElement setter = property.getSetter();
      if (setter != null && !isVisible(this.accessType.setterVisibility(), setter)) {
        return false;
      }

      return true;
    }
    else if (element instanceof DecoratedVariableElement) {

      if (!isVisible(this.accessType.fieldVisibility(), element)) {
        return false;
      }

      if (element.isStatic() || element.isTransient()) {
        return false;
      }

      return true;
    }

    return false;
  }

  protected boolean isVisible(JsonAutoDetect.Visibility visibility, DecoratedElement element) {
    switch (visibility) {
      case ANY:
        return true;
      case NONE:
        return false;
      case NON_PRIVATE:
        return !element.getModifiers().contains(Modifier.PRIVATE);
      case PROTECTED_AND_PUBLIC:
        return element.getModifiers().contains(Modifier.PROTECTED) || element.getModifiers().contains(Modifier.PUBLIC);
      default:
        return element.getModifiers().contains(Modifier.PUBLIC);
    }
  }

}
