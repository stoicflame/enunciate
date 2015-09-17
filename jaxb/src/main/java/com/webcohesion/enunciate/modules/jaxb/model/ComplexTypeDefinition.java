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

package com.webcohesion.enunciate.modules.jaxb.model;

import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.types.KnownXmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlTypeFactory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A complex type definition.
 *
 * @author Ryan Heaton
 */
public class ComplexTypeDefinition extends SimpleTypeDefinition {

  public ComplexTypeDefinition(TypeElement delegate, EnunciateJaxbContext context) {
    super(delegate, context);
  }

  @Override
  public XmlType getBaseType() {
    XmlType baseType = super.getBaseType();

    if (baseType == null) {
      TypeMirror superclass = getSuperclass();
      if (superclass != null && superclass.getKind() != TypeKind.NONE) {
        baseType = XmlTypeFactory.getXmlType(superclass, this.context);
      }
      else {
        baseType = KnownXmlType.ANY_TYPE;
      }
    }

    return baseType;
  }

  /**
   * The compositor for this type definition.
   *
   * @return The compositor for this type definition.
   */
  public String getCompositorName() {
    //"all" isn't supported because the spec isn't clear on what to do when:
    // 1. A class with the "all" compositor is extended.
    // 2. an "element" content element has maxOccurs > 0
    //return getPropertyOrder() == null ? "all" : "sequence";
    return "sequence";
  }

  /**
   * The content type of this complex type definition.
   *
   * @return The content type of this complex type definition.
   */
  public ComplexContentType getContentType() {
    if (!getElements().isEmpty()) {
      if (isBaseObject()) {
        return ComplexContentType.IMPLIED;
      }
      else {
        return ComplexContentType.COMPLEX;
      }
    }
    else if (getBaseType().isSimple()) {
      return ComplexContentType.SIMPLE;
    }
    else {
      return ComplexContentType.EMPTY;
    }
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean isComplex() {
    return getAnnotation(XmlJavaTypeAdapter.class) == null;
  }

  @Override
  public boolean isBaseObject() {
    TypeMirror superclass = getSuperclass();
    if (superclass.getKind() == TypeKind.NONE) {
      return true;
    }

    TypeElement superDeclaration = (TypeElement) this.env.getTypeUtils().asElement(superclass);
    return superDeclaration == null
      || Object.class.getName().equals(superDeclaration.getQualifiedName().toString())
      || isXmlTransient(superDeclaration);
  }

}
