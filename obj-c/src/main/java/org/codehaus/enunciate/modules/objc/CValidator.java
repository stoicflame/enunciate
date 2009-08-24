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

package org.codehaus.enunciate.modules.objc;

import org.codehaus.enunciate.contract.jaxb.Attribute;
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.Element;
import org.codehaus.enunciate.contract.jaxb.SimpleTypeDefinition;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.util.MapType;

/**
 * Validator for the C module.
 *
 * @author Ryan Heaton
 */
public class CValidator extends BaseValidator {

  @Override
  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    ValidationResult result = super.validateSimpleType(simpleType);
    if (simpleType.getValue() != null) {
      if (simpleType.getValue().isXmlIDREF()) {
        result.addWarning(simpleType.getValue(), "The C client code doesn't support strict IDREF object references, so only the IDs of these objects will be (de)serialized from C. " +
                                   "This may cause confusion to C consumers.");
      }

    }
    return result;
  }

  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = super.validateComplexType(complexType);
    for (Attribute attribute : complexType.getAttributes()) {
      if (attribute.isXmlIDREF()) {
        result.addWarning(attribute, "The C client code doesn't support strict IDREF object references, so only the IDs of these objects will be (de)serialized from C. " +
                                   "This may cause confusion to C consumers.");
      }

      if (attribute.isXmlList()) {
        result.addWarning(attribute, "The C client code won't serialize xml lists as an array, instead passing the list as a string that will need to be parsed. This may cause confusion to C consumers.");
      }

      if (attribute.isCollectionType() && attribute.isBinaryData()) {
        result.addError(attribute, "The C client code doesn't support a collection of items that are binary data. You'll have to define separate accessors for each item or disable the C module.");
      }
    }

    if (complexType.getValue() != null) {
      if (complexType.getValue().isXmlIDREF()) {
        result.addWarning(complexType.getValue(), "The C client code doesn't support strict IDREF object references, so only the IDs of these objects will be (de)serialized from C. " +
                                   "This may cause confusion to C consumers.");
      }

      if (complexType.getValue().isXmlList()) {
        result.addWarning(complexType.getValue(), "The C client code won't serialize xml lists as an array, instead passing the list as a string that will need to be parsed. This may cause confusion to C consumers.");
      }

      if (complexType.getValue().isCollectionType() && complexType.getValue().isBinaryData()) {
        result.addError(complexType.getValue(), "The C client code doesn't support a collection of items that are binary data. You'll have to define separate accessors for each item or disable the C module.");
      }
    }

    for (Element element : complexType.getElements()) {
      if (element.isXmlIDREF()) {
        result.addWarning(element, "The C client code doesn't support strict IDREF object references, so only the IDs of these objects will be (de)serialized from C. " +
                                   "This may cause confusion to C consumers.");
      }

      if (element.isXmlList()) {
        result.addWarning(element, "The C client code won't serialize xml lists as an array, instead passing the list as a string that will need to be parsed. This may cause confusion to C consumers.");
      }

      if (element.getAccessorType() instanceof MapType && !element.isAdapted()) {
        result.addError(element, "The C client doesn't have a built-in way of serializing a Map. So you're going to have to use @XmlJavaTypeAdapter to supply " +
          "your own adapter for the Map, or disable the C module.");
      }

      if (element.isCollectionType()) {
        if (element.getChoices().size() > 1) {
          result.addWarning(element, "The C client code doesn't fully support multiple choices for a collection. It has to separate each choice into its own array. " +
            "This makes the C API a bit awkward to use and makes it impossible to preserve the order of the collection. If order is relevant, consider breaking out " +
            "the choices into their own collection or otherwise refactoring the API.");
        }

        if (element.isBinaryData()) {
          result.addError(element, "The C client code doesn't support a collection of items that are binary data. You'll have to define separate accessors for each item or disable the C module.");
        }

        for (Element choice : element.getChoices()) {
          if (choice.isNillable()) {
            result.addWarning(choice, "The C client code doesn't support nillable items in a collection (the nil items will be skipped). This may cause confusion to C consumers.");
          }
        }
      }
    }


    return result;
  }

}
