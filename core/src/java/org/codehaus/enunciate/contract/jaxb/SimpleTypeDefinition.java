/*
 * Copyright 2006 Web Cohesion
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
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.validation.Validator;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * A simple type definition.
 *
 * @author Ryan Heaton
 */
public class SimpleTypeDefinition extends TypeDefinition {

  public SimpleTypeDefinition(ClassDeclaration delegate) {
    super(delegate);
  }

  /**
   * The base type for this simple type, or null if none exists.
   *
   * @return The base type for this simple type.
   */
  public XmlType getBaseType() {
    Value value = getValue();

    if (value != null) {
      return value.getBaseType();
    }

    return null;
  }

  public ValidationResult accept(Validator validator) {
    return validator.validateSimpleType(this);
  }

  @Override
  public boolean isSimple() {
    return getAnnotation(XmlJavaTypeAdapter.class) == null;
  }

}
