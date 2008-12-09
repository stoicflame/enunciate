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

package org.codehaus.enunciate.modules.jaxws_client;

import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.Element;
import org.codehaus.enunciate.util.MapType;

import javax.xml.bind.annotation.XmlElement;

/**
 * The validator for the jaxws-client module.
 *
 * @author Ryan Heaton
 */
public class JAXWSClientValidator extends BaseValidator {

  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = new ValidationResult();

    for (Element element : complexType.getElements()) {
      if (element.getAccessorType() instanceof MapType && element.getAnnotation(XmlElement.class) != null) {
        result.addError(element, "Because of a bug in JAXB, an Map property can't have and @XmlElement annotation. You must either " +
          "eliminate the @XmlElement annotation or disable the JAX-WS client module.");
      }
    }

    return result;
  }
}
