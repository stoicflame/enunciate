/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jaxb.model.util;

import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class JAXBCodeErrors {

  static final String CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY = "com.webcohesion.enunciate.modules.jaxb.model.util.JAXBCodeErrors#CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY";

  private JAXBCodeErrors() {}

  public static List<String> findConflictingAccessorNamingErrors(EnunciateJaxbContext context) {
    List<String> errors = (List<String>) context.getContext().getProperty(CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY);
    if (errors == null) {
      errors = new ArrayList<String>();
      context.getContext().setProperty(CONFLICTING_JAXB_ACCESSOR_NAMING_ERRORS_PROPERTY, errors);

      for (SchemaInfo schemaInfo : context.getSchemas().values()) {
        for (TypeDefinition typeDefinition : schemaInfo.getTypeDefinitions()) {
          Map<String, Accessor> accessorsBySimpleName = new HashMap<String, Accessor>();
          for (Accessor accessor : typeDefinition.getAllAccessors()) {
            String name = accessor.getClientSimpleName();
            Accessor conflict = accessorsBySimpleName.get(name);
            if (conflict != null) {
              errors.add(String.format("%s: accessor \"%s\" conflicts with accessor \"%s\" of %s: both are named \"%s\".", typeDefinition.getQualifiedName(), accessor, conflict, conflict.getTypeDefinition().getQualifiedName(), name));
            }
            else {
              accessorsBySimpleName.put(name, accessor);
            }
          }
        }
      }
    }
    return errors;
  }
}
