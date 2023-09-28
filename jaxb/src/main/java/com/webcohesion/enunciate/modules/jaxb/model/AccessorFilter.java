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
package com.webcohesion.enunciate.modules.jaxb.model;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;
import com.webcohesion.enunciate.javac.decorations.element.PropertyElement;
import com.webcohesion.enunciate.util.AnnotationUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.List;

/**
 * Filter for potential accessors.
 *
 * @author Ryan Heaton
 */
public class AccessorFilter {

  private final XmlAccessType accessType;

  public AccessorFilter(XmlAccessType accessType) {
    this.accessType = accessType;

    if (accessType == null) {
      throw new IllegalArgumentException("An access type must be specified.");
    }
  }

  /**
   * Whether to accept the given member declaration as an accessor.
   *
   * @param element The declaration to filter.
   * @return Whether to accept the given member declaration as an accessor.
   */
  public boolean accept(DecoratedElement<?> element) {
    if (AnnotationUtils.isIgnored(element)) {
      return false;
    }
    if (element.getAnnotation(XmlTransient.class) != null) {
      return false;
    }

    if (element instanceof PropertyElement) {
      PropertyElement property = ((PropertyElement) element);

      if ("".equals(property.getPropertyName())) {
        return false;
      }

      for (String annotationName : property.getAnnotations().keySet()) {
        if (annotationName.startsWith("jakarta.xml.bind.annotation")) {
          //if the property has an explicit annotation, we'll include it.
          return true;
        }
      }

      DecoratedExecutableElement getter = property.getGetter();
      if (getter == null) {
        //needs a getter.
        return false;
      }

      DecoratedExecutableElement setter = property.getSetter();
      if (setter == null) {
        //needs a setter.
        return false;
      }

      if (!getter.isPublic()) {
        //we only have to worry about public methods ("properties" are only defined by public accessor methods).
        return false;
      }

      if (!setter.isPublic()) {
        //we only have to worry about public methods ("properties" are only defined by public accessor methods).
        return false;
      }

      return (((accessType != XmlAccessType.NONE) && (accessType != XmlAccessType.FIELD)) || (explicitlyDeclaredAccessor(element)));
    }
    else if (element instanceof DecoratedVariableElement) {
      if (element.isStatic() || element.isTransient()) {
        return false;
      }

      if ((accessType == XmlAccessType.NONE) || (accessType == XmlAccessType.PROPERTY)) {
        return explicitlyDeclaredAccessor(element);
      }

      if (accessType == XmlAccessType.PUBLIC_MEMBER) {
        return (element.isPublic() || (explicitlyDeclaredAccessor(element)));
      }

      //the accessType is FIELD.  Include it.
      return true;
    }

    return false;
  }

  /**
   * Whether the specified member declaration is explicitly declared to be an accessor.
   *
   * @param element The declaration to check whether it is explicitly declared to be an accessor.
   * @return Whether the specified member declaration is explicitly declared to be an accessor.
   */
  protected boolean explicitlyDeclaredAccessor(DecoratedElement<?> element) {
    List<? extends AnnotationMirror> mirrors = element.getAnnotationMirrors();
    for (AnnotationMirror annotationMirror : mirrors) {
      DeclaredType annotationType = annotationMirror.getAnnotationType();
      if (annotationType != null) {
        TypeElement annotationDeclaration = (TypeElement) annotationType.asElement();
        if ((annotationDeclaration != null) && (annotationDeclaration.getQualifiedName().toString().startsWith(XmlElement.class.getPackage().getName()))) {
          //if it's annotated with anything in jakarta.xml.bind.annotation, (exception XmlTransient) we'll consider it to be "explicitly annotated."
          return !annotationDeclaration.getQualifiedName().toString().equals(XmlTransient.class.getName());
        }
      }
    }

    return false;
  }

}
