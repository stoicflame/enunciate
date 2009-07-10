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

package org.codehaus.enunciate.modules.ruby;

import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.Element;
import org.codehaus.enunciate.contract.jaxb.ElementRef;

import javax.xml.bind.annotation.XmlElements;

/**
 * Validator for the Ruby module.
 *
 * @author Ryan Heaton
 */
public class RubyValidator extends BaseValidator {

  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = super.validateComplexType(complexType);
    if (!Character.isUpperCase(complexType.getClientSimpleName().charAt(0))) {
      result.addError(complexType, "Ruby requires your class name to be upper-case. Please rename the class or apply the @org.codehaus.enunciate.ClientName annotation to the class.");
    }
    for (Element element : complexType.getElements()) {
      if (element instanceof ElementRef && ((ElementRef) element).isElementRefs()) {
        result.addWarning(complexType, "The Ruby client library doesn't fully support the @XmlElementRefs annotation. The items in the collection will be read-only and will only be available to Ruby clients in the form of a Hash. Consider redesigning using a collection of a single type. See http://jira.codehaus.org/browse/ENUNCIATE-300 for more information.");
      }
      else if (element.getAnnotation(XmlElements.class) != null) {
        result.addWarning(complexType, "The Ruby client library doesn't fully support the @XmlElements annotation. The items in the collection will be read-only and will only be available to Ruby clients in the form of a Hash. Consider redesigning using a collection of a single type. See http://jira.codehaus.org/browse/ENUNCIATE-300 for more information.");
      }
    }
    return result;
  }
}
