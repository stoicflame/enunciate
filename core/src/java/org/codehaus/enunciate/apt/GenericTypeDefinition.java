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

package org.codehaus.enunciate.apt;

import com.sun.mirror.declaration.ClassDeclaration;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeMirror;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.validation.Validator;

/**
 * Internal class used to inherit some functionality for determining whether a declaration is a simple type
 * or a complex type.
 */
class GenericTypeDefinition extends TypeDefinition {

  protected GenericTypeDefinition(ClassDeclaration delegate) {
    super(delegate);
  }

  public ValidationResult accept(Validator validator) {
    return new ValidationResult();
  }

  public XmlTypeMirror getBaseType() {
    return KnownXmlType.ANY_TYPE;
  }
}
