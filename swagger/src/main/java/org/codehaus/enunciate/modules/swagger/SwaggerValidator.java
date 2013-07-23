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

package org.codehaus.enunciate.modules.swagger;

import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;

/**
 * Validator for the C# module.
 *
 * @author Ryan Heaton
 */
public class SwaggerValidator extends BaseValidator {

  @Override
  public ValidationResult validateSimpleType(SimpleTypeDefinition type) {
    ValidationResult result = super.validateSimpleType(type);
    validateTypeDefinition(type, result);
    return result;
  }

  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition type) {
    ValidationResult result = super.validateComplexType(type);
    validateTypeDefinition(type, result);
    return result;
  }

  private void validateTypeDefinition(TypeDefinition type, ValidationResult result) {
    for (Attribute attribute : type.getAttributes()) {
      if (attribute.getBaseType().isAnonymous()) {
        result.addError(attribute, "Swagger isn't smart enough to handle anonymous types (such as maps).");
      }
    }

    if (type.getValue() != null && type.getValue().getBaseType().isAnonymous()) {
      result.addError(type.getValue(), "Swagger isn't smart enough to handle anonymous types (such as maps).");
    }

    for (Element element : type.getElements()) {
      if (element.getChoices().size() > 1) {
        result.addError(element, "Swagger isn't smart enough to handle multiple choices for a property.");
      }

      for (Element choice : element.getChoices()) {
        if (!choice.isElementRef() && choice.getBaseType().isAnonymous()) {
          result.addError(choice, "Swagger isn't smart enough to handle anonymous types (such as maps).");
        }
      }
    }
  }

}
