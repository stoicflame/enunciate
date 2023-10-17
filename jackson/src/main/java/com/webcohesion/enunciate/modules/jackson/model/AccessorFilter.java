/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jackson.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;
import com.webcohesion.enunciate.javac.decorations.element.PropertyElement;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * Filter for potential accessors.
 *
 * @author Ryan Heaton
 */
public class AccessorFilter {

  private final EnunciateJacksonContext context;
  private final JsonAutoDetect accessType;
  private final Set<String> propertiesToIgnore;
  private final boolean honorJaxb;
  private final XmlAccessorType jaxbAccessorType;
  private final AccessorVisibilityChecker defaultVisibility;

  public AccessorFilter(EnunciateJacksonContext context, JsonAutoDetect accessType, JsonIgnoreProperties ignoreProperties, XmlAccessorType jaxbAccessorType) {
    this.context = context;
    this.accessType = accessType;
    this.propertiesToIgnore = new TreeSet<String>();
    if (ignoreProperties != null) {
      Collections.addAll(this.propertiesToIgnore, ignoreProperties.value());
    }
    this.honorJaxb = context.isHonorJaxb();
    this.jaxbAccessorType = jaxbAccessorType;
    this.defaultVisibility = context.getDefaultVisibility();
  }

  /**
   * Whether to accept the given member declaration as an accessor.
   *
   * @param element The declaration to filter.
   * @return Whether to accept the given member declaration as an accessor.
   */
  public boolean accept(DecoratedElement<?> element) {
    if (context.isIgnored(element)) {
      return false;
    }

    if (element.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class) != null) {
      //if there's an explicit json property annotation, we'll include it.
      return true;
    }

    if (this.honorJaxb) {
      if (element.getAnnotation(XmlTransient.class) != null) {
        return false;
      }

      for (String annotationName : element.getAnnotations().keySet()) {
        if (annotationName.startsWith("jakarta.xml.bind.annotation")) {
          //if the property has an explicit annotation, we'll include it.
          return true;
        }
      }
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

      if (!isVisible(findGetterVisibility(getter), getter)) {
        return false;
      }

      DecoratedExecutableElement setter = property.getSetter();
      if (setter != null && !isVisible(findSetterVisibility(), setter)) {
        return false;
      }

      return true;
    }
    else if (element instanceof DecoratedVariableElement) {

      if (!isVisible(findFieldVisibility(), element)) {
        return false;
      }

      if (element.isStatic() || element.isTransient()) {
        return false;
      }

      return true;
    }

    if(element.getKind() == ElementKind.RECORD_COMPONENT) {
      return true;
    }

    return false;
  }

  protected JsonAutoDetect.Visibility findFieldVisibility() {
    if (this.accessType != null) {
      return this.accessType.fieldVisibility();
    }

    if (this.honorJaxb && this.jaxbAccessorType != null) {
      switch (this.jaxbAccessorType.value()) {
        case FIELD:
          return JsonAutoDetect.Visibility.ANY;
        case NONE:
          return JsonAutoDetect.Visibility.NONE;
        case PROPERTY:
          return JsonAutoDetect.Visibility.NONE;
        case PUBLIC_MEMBER:
          return JsonAutoDetect.Visibility.PUBLIC_ONLY;
      }
    }

    return defaultVisibility.getVisibility(PropertyAccessor.FIELD);
  }

  protected JsonAutoDetect.Visibility findSetterVisibility() {
    if (this.accessType != null) {
      return this.accessType.setterVisibility();
    }

    if (this.honorJaxb && this.jaxbAccessorType != null) {
      switch (this.jaxbAccessorType.value()) {
        case FIELD:
          return JsonAutoDetect.Visibility.NONE;
        case NONE:
          return JsonAutoDetect.Visibility.NONE;
        case PROPERTY:
        case PUBLIC_MEMBER:
          return JsonAutoDetect.Visibility.PUBLIC_ONLY;
      }
    }

    return defaultVisibility.getVisibility(PropertyAccessor.SETTER);
  }

  protected JsonAutoDetect.Visibility findGetterVisibility(DecoratedExecutableElement getter) {
    boolean isIsGetter = getter.getSimpleName().toString().startsWith("is");
    if (this.accessType != null) {
      return isIsGetter ? this.accessType.isGetterVisibility() : this.accessType.getterVisibility();
    }

    if (this.honorJaxb && this.jaxbAccessorType != null) {
      switch (this.jaxbAccessorType.value()) {
        case FIELD:
          return JsonAutoDetect.Visibility.NONE;
        case NONE:
          return JsonAutoDetect.Visibility.NONE;
        case PROPERTY:
        case PUBLIC_MEMBER:
          return JsonAutoDetect.Visibility.PUBLIC_ONLY;
      }
    }

    return defaultVisibility.getVisibility(isIsGetter ? PropertyAccessor.IS_GETTER : PropertyAccessor.GETTER);
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
