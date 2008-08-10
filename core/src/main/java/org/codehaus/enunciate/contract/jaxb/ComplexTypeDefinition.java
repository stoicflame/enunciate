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

import com.sun.mirror.declaration.ClassDeclaration;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.validation.Validator;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A complex type definition.
 *
 * @author Ryan Heaton
 */
public class ComplexTypeDefinition extends SimpleTypeDefinition {

  public ComplexTypeDefinition(ClassDeclaration delegate) {
    super(delegate);
  }

  @Override
  public XmlType getBaseType() {
    XmlType baseType = super.getBaseType();

    if (baseType == null) {
      try {
        baseType = XmlTypeFactory.getXmlType(getSuperclass());
      }
      catch (XmlTypeException e) {
        throw new ValidationException(getPosition(), e.getMessage());
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
    // 2. an "element" content elemnt has maxOccurs > 0
    //return getPropertyOrder() == null ? "all" : "sequence";
    return "sequence";
  }

  /**
   * The content type of this complex type definition.
   *
   * @return The content type of this complex type definition.
   */
  public ContentType getContentType() {
    if (!getElements().isEmpty()) {
      if (isBaseObject()) {
        return ContentType.IMPLIED;
      }
      else {
        return ContentType.COMPLEX;
      }
    }
    else if (getBaseType().isSimple()) {
      return ContentType.SIMPLE;
    }
    else {
      return ContentType.EMPTY;
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
    return getSuperclass().getDeclaration() == null || Object.class.getName().equals(getSuperclass().getDeclaration().getQualifiedName());
  }

  @Override
  public ValidationResult accept(Validator validator) {
    return validator.validateComplexType(this);
  }

}
