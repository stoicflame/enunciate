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

package org.codehaus.enunciate.modules.c;

import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;

/**
 * Validator for the Ruby module.
 *
 * @author Ryan Heaton
 */
public class CValidator extends BaseValidator {

  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = super.validateComplexType(complexType);

    //WARNING: optional boolean accesors
    //WARNING: nillable elements in a collection
    //WARNING: ID/IDREF
    //WARNING: multiple choices
    //ERROR: XML lists (simple types)
    //ERROR: Maps
    //ERROR: collection of binary data.
    //ERROR: multiple anonymous types?

    return result;
  }
}
