/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jackson.model.Member;
import com.webcohesion.enunciate.modules.jackson.model.ObjectTypeDefinition;
import com.webcohesion.enunciate.modules.jackson.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonClassType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonTypeFactory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * @author Ryan Heaton
 */
public class ObjectDataTypeImpl extends DataTypeImpl {

  final ObjectTypeDefinition typeDefinition;

  public ObjectDataTypeImpl(ObjectTypeDefinition typeDefinition) {
    super(typeDefinition);
    this.typeDefinition = typeDefinition;
  }

  @Override
  public BaseType getBaseType() {
    return BaseType.object;
  }

  @Override
  public List<? extends Value> getValues() {
    return null;
  }

  @Override
  public List<? extends Property> getProperties() {
    SortedSet<Member> members = this.typeDefinition.getMembers();
    ArrayList<Property> properties = new ArrayList<Property>(members.size());
    FacetFilter facetFilter = this.typeDefinition.getContext().getContext().getConfiguration().getFacetFilter();
    for (Member member : members) {
      if (!facetFilter.accept(member)) {
        continue;
      }

      if (member.getChoices().size() > 1) {
        JsonTypeInfo.As inclusion = member.getSubtypeIdInclusion();
        if (inclusion == JsonTypeInfo.As.WRAPPER_ARRAY || inclusion == JsonTypeInfo.As.WRAPPER_OBJECT) {
          for (Member choice : member.getChoices()) {
            properties.add(new PropertyImpl(choice));
          }
        }
        else {
          properties.add(new PropertyImpl(member));
        }
      }
      else {
        properties.add(new PropertyImpl(member));
      }
    }

    return properties;
  }

  public List<? extends Property> getRequiredProperties() {
    ArrayList<Property> requiredProperties = new ArrayList<Property>();
    for (Property property : getProperties()) {
      if (property.isRequired()) {
        requiredProperties.add(property);
      }
    }
    return requiredProperties;
  }

  @Override
  public List<DataTypeReference> getSupertypes() {
    ArrayList<DataTypeReference> supertypes = null;

    JsonType supertype = this.typeDefinition.getSupertype();
    while (supertype != null) {
      if (supertypes == null) {
        supertypes = new ArrayList<DataTypeReference>();
      }

      supertypes.add(new DataTypeReferenceImpl(supertype));
      supertype = supertype instanceof JsonClassType ?
        ((JsonClassType) supertype).getTypeDefinition() instanceof ObjectTypeDefinition ?
          ((ObjectTypeDefinition) ((JsonClassType) supertype).getTypeDefinition()).getSupertype()
          : null
        : null;
    }

    return supertypes;
  }

  @Override
  public List<DataTypeReference> getSubtypes() {
    ArrayList<DataTypeReference> subtypes = new ArrayList<DataTypeReference>();
    String myClassName = this.typeDefinition.getQualifiedName().toString();

    for (TypeDefinition td : this.typeDefinition.getContext().getTypeDefinitions()) {
      if (td instanceof ObjectTypeDefinition) {
        TypeMirror superclass = td.getSuperclass();
        if (superclass instanceof DeclaredType && (((TypeElement) ((DeclaredType) superclass).asElement()).getQualifiedName().toString().equals(myClassName))) {
          subtypes.add(new DataTypeReferenceImpl(JsonTypeFactory.getJsonType(td.asType(), this.typeDefinition.getContext())));
        }
      }
    }
    return subtypes.isEmpty() ? null : subtypes;
  }

  @Override
  public Example getExample() {
    return this.typeDefinition.getContext().isDisableExamples() ? null : new ExampleImpl(this.typeDefinition);
  }
}
