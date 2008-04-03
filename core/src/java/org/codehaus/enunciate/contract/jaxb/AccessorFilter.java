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

package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.declaration.*;
import com.sun.mirror.type.AnnotationType;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Collection;

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
   * The access type for this filter.
   * 
   * @return The access type for this filter.
   */
  public XmlAccessType getAccessType() {
    return accessType;
  }

  /**
   * Whether to accept the given member declaration as an accessor.
   *
   * @param declaration The declaration to filter.
   * @return Whether to accept the given member declaration as an accessor.
   */
  public boolean accept(MemberDeclaration declaration) {
    if (isXmlTransient(declaration)) {
      return false;
    }

    if (declaration instanceof PropertyDeclaration) {
      PropertyDeclaration property = ((PropertyDeclaration) declaration);

      DecoratedMethodDeclaration getter = property.getGetter();
      if (getter == null) {
        //needs a getter.
        return false;
      }

      DecoratedMethodDeclaration setter = property.getSetter();
      if (setter == null) {
        //needs a setter.
        return false;
      }

      if (!getter.getModifiers().contains(Modifier.PUBLIC)) {
        //we only have to worry about public methods ("properties" are only defined by public accessor methods).
        return false;
      }

      if (!setter.getModifiers().contains(Modifier.PUBLIC)) {
        //we only have to worry about public methods ("properties" are only defined by public accessor methods).
        return false;
      }

      return (((accessType != XmlAccessType.NONE) && (accessType != XmlAccessType.FIELD)) || (explicitlyDeclaredAccessor(declaration)));
    }
    else if (declaration instanceof FieldDeclaration) {
      if (declaration.getModifiers().contains(Modifier.STATIC) || declaration.getModifiers().contains(Modifier.TRANSIENT)) {
        return false;
      }

      if ((accessType == XmlAccessType.NONE) || (accessType == XmlAccessType.PROPERTY)) {
        return explicitlyDeclaredAccessor(declaration);
      }

      if (accessType == XmlAccessType.PUBLIC_MEMBER) {
        return (declaration.getModifiers().contains(Modifier.PUBLIC) || (explicitlyDeclaredAccessor(declaration)));
      }

      //the accessType is FIELD.  Include it.
      return true;
    }

    return false;
  }

  /**
   * Whether the specified member declaration is explicitly declared to be an accessor.
   *
   * @param declaration The declaration to check whether it is explicitly declared to be an accessor.
   * @return Whether the specified member declaration is explicitly declared to be an accessor.
   */
  protected boolean explicitlyDeclaredAccessor(MemberDeclaration declaration) {
    Collection<AnnotationMirror> annotationMirrors = declaration.getAnnotationMirrors();
    for (AnnotationMirror annotationMirror : annotationMirrors) {
      AnnotationType annotationType = annotationMirror.getAnnotationType();
      if (annotationType != null) {
        AnnotationTypeDeclaration annotationDeclaration = annotationType.getDeclaration();
        if ((annotationDeclaration != null) && (annotationDeclaration.getQualifiedName().startsWith(XmlElement.class.getPackage().getName()))) {
          //if it's annotated with anything in javax.xml.bind.annotation, (exception XmlTransient) we'll consider it to be "explicitly annotated."
          return !annotationDeclaration.getQualifiedName().equals(XmlTransient.class.getName());
        }
      }
    }

    return false;
  }

  /**
   * Whether a declaration is xml transient.
   *
   * @param declaration The declaration on which to determine xml transience.
   * @return Whether a declaration is xml transient.
   */
  protected boolean isXmlTransient(Declaration declaration) {
    return (declaration.getAnnotation(XmlTransient.class) != null);
  }
}
