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

package org.codehaus.enunciate.contract.validation;

import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.EnumTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.SimpleTypeDefinition;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.rest.RESTMethod;
import org.codehaus.enunciate.contract.rest.RESTNoun;
import org.codehaus.enunciate.contract.rest.ContentTypeHandler;
import org.codehaus.enunciate.contract.jaxrs.RootResource;

import java.util.Map;
import java.util.List;

/**
 * Validator for the contract classes.  A single validator will be assigned to one set of source classes.
 * This means that a validator may keep state without the fear that the methods will be called more than
 * once (or less than once, for that matter) per type declaration.
 *
 * @author Ryan Heaton
 */
public interface Validator {

  /**
   * Validates an endpoint interface.
   *
   * @param ei The endpoint interface to validate.
   * @return The result of the validation.
   */
  ValidationResult validateEndpointInterface(EndpointInterface ei);

  /**
   * Validates the REST API.
   *
   * @param restAPI The map of nouns to their accessor methods.
   * @return The result of the validation.
   */
  ValidationResult validateRESTAPI(Map<RESTNoun, List<RESTMethod>> restAPI);

  /**
   * Validate the JAX-RS root resources.
   *
   * @param rootResources The root resources to validate.
   * @return The result of the validation.
   */
  ValidationResult validateRootResources(List<RootResource> rootResources);

  /**
   * Validate the content type handlers.
   *
   * @param contentTypeHandlers The content type handlers.
   * @return The validation result.
   */
  ValidationResult validateContentTypeHandlers(List<ContentTypeHandler> contentTypeHandlers);

  /**
   * Validate a complex type definition.
   *
   * @param complexType The complex type to validate.
   * @return The results of the validation.
   */
  ValidationResult validateComplexType(ComplexTypeDefinition complexType);

  /**
   * Valiate a simple type definition.
   *
   * @param simpleType The simple type to validate.
   * @return The results of the validation.
   */
  ValidationResult validateSimpleType(SimpleTypeDefinition simpleType);

  /**
   * Valiate an enum type definition.
   *
   * @param enumType The simple type to validate.
   * @return The results of the validation.
   */
  ValidationResult validateEnumType(EnumTypeDefinition enumType);

  /**
   * Validate a global element declaration.
   *
   * @param rootElementDeclaration The global element declaration.
   * @return The results of the validation.
   */
  ValidationResult validateRootElement(RootElementDeclaration rootElementDeclaration);
}
