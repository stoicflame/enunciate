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
package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.Namespace;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxb.model.ComplexTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class NamespaceImpl implements Namespace {

  private final SchemaInfo schema;
  private ApiRegistrationContext registrationContext;

  public NamespaceImpl(SchemaInfo schema, ApiRegistrationContext registrationContext) {
    this.schema = schema;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getUri() {
    String ns = this.schema.getNamespace();
    return ns == null ? "" : ns;
  }

  @Override
  public InterfaceDescriptionFile getSchemaFile() {
    return this.schema.getSchemaFile();
  }

  @Override
  public List<? extends DataType> getTypes() {
    FacetFilter facetFilter = this.registrationContext.getFacetFilter();

    ArrayList<DataType> dataTypes = new ArrayList<DataType>();
    for (TypeDefinition typeDefinition : this.schema.getTypeDefinitions()) {
      if (!facetFilter.accept(typeDefinition)) {
        continue;
      }
      
      if (typeDefinition instanceof ComplexTypeDefinition) {
        dataTypes.add(new ComplexDataTypeImpl((ComplexTypeDefinition) typeDefinition, registrationContext));
      }
      else if (typeDefinition instanceof EnumTypeDefinition) {
        dataTypes.add(new EnumDataTypeImpl((EnumTypeDefinition) typeDefinition, registrationContext));
      }
    }
    return dataTypes;
  }
}
