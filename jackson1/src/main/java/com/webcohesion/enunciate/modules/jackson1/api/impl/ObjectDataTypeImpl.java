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
package com.webcohesion.enunciate.modules.jackson1.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jackson1.model.Member;
import com.webcohesion.enunciate.modules.jackson1.model.ObjectTypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonClassType;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonTypeFactory;
import org.codehaus.jackson.annotate.JsonTypeInfo;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class ObjectDataTypeImpl extends DataTypeImpl {

  final ObjectTypeDefinition typeDefinition;

  public ObjectDataTypeImpl(ObjectTypeDefinition typeDefinition, ApiRegistrationContext registrationContext) {
    super(typeDefinition, registrationContext);
    this.typeDefinition = typeDefinition;
  }

  @Override
  public BaseType getBaseType() {
    return BaseType.object;
  }

  @Override
  public Value findValue(String name) {
    return null;
  }

  @Override
  public List<? extends Value> getValues() {
    return null;
  }

  @Override
  public Property findProperty(String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }
    for (Member member : this.typeDefinition.getMembers()) {
      if (member.getSimpleName().contentEquals(name)) {
        return new PropertyImpl(member, registrationContext);
      }
    }
    return null;
  }

  @Override
  public List<? extends Property> getProperties() {
    List<Member> members = this.typeDefinition.getMembers();
    ArrayList<Property> properties;

    if (this.typeDefinition.getTypeIdInclusion() == JsonTypeInfo.As.PROPERTY) {
      properties = new ArrayList<Property>(members.size() + 1);
      if (this.typeDefinition.getTypeIdProperty() != null
              && members.stream().noneMatch(m -> this.typeDefinition.getTypeIdProperty().equals(m.getName()))) {
        properties.add(new TypeReferencePropertyImpl(this.typeDefinition.getTypeIdProperty()));
      }
    }
    else {
      properties = new ArrayList<Property>(members.size());
    }

    FacetFilter facetFilter = this.registrationContext.getFacetFilter();
    for (Member member : members) {
      if (!facetFilter.accept(member)) {
        continue;
      }

      if (member.getChoices().size() > 1) {
        JsonTypeInfo.As inclusion = member.getSubtypeIdInclusion();
        if (inclusion == JsonTypeInfo.As.WRAPPER_ARRAY || inclusion == JsonTypeInfo.As.WRAPPER_OBJECT) {
          for (Member choice : member.getChoices()) {
            properties.add(new PropertyImpl(choice, registrationContext, member.isCollectionType()));
          }
        }
        else {
          properties.add(new PropertyImpl(member, registrationContext));
        }
      }
      else {
        properties.add(new PropertyImpl(member, registrationContext));
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

      supertypes.add(new DataTypeReferenceImpl(supertype, registrationContext));
      supertype = supertype instanceof JsonClassType ?
        ((JsonClassType) supertype).getTypeDefinition() instanceof ObjectTypeDefinition ?
          ((ObjectTypeDefinition) ((JsonClassType) supertype).getTypeDefinition()).getSupertype()
          : null
        : null;
    }

    return supertypes;
  }

  @Override
  public Set<DataTypeReference> getInterfaces() {
    Set<DataTypeReference> interfaces = new TreeSet<DataTypeReference>(new Comparator<DataTypeReference>() {
      @Override
      public int compare(DataTypeReference o1, DataTypeReference o2) {
        return o1.getSlug().compareTo(o2.getSlug());
      }
    });

    gatherInterfaces(this.typeDefinition, interfaces);

    return interfaces.isEmpty() ? null : interfaces;
  }

  private void gatherInterfaces(TypeElement clazz, Set<DataTypeReference> interfaces) {
    if (clazz == null) {
      return;
    }

    if (clazz.getQualifiedName().contentEquals(Object.class.getName())) {
      return;
    }

    List<? extends TypeMirror> ifaces = clazz.getInterfaces();
    for (TypeMirror iface : ifaces) {
      DecoratedTypeMirror decorated = (DecoratedTypeMirror) iface;
      decorated = this.typeDefinition.getContext().resolveSyntheticType(decorated);
      TypeDefinition typeDefinition = this.typeDefinition.getContext().findTypeDefinition(((DeclaredType) decorated).asElement());
      if (typeDefinition != null) {
        interfaces.add(new DataTypeReferenceImpl(new JsonClassType(typeDefinition), registrationContext));
      }
    }

    TypeMirror superclass = clazz.getSuperclass();
    if (superclass instanceof DeclaredType) {
      gatherInterfaces((TypeElement) ((DeclaredType) superclass).asElement(), interfaces);
    }
  }

  @Override
  public List<DataTypeReference> getSubtypes() {
    ArrayList<DataTypeReference> subtypes = new ArrayList<DataTypeReference>();
    for (TypeDefinition td : this.typeDefinition.getContext().getTypeDefinitions()) {
      if (td instanceof ObjectTypeDefinition && !td.getQualifiedName().contentEquals(this.typeDefinition.getQualifiedName()) && ((DecoratedTypeMirror) td.asType()).isInstanceOf(this.typeDefinition)) {
        subtypes.add(new DataTypeReferenceImpl(JsonTypeFactory.getJsonType(td.asType(), this.typeDefinition.getContext()), registrationContext));
      }
    }
    return subtypes.isEmpty() ? null : subtypes;
  }

  @Override
  public Example getExample() {
    return this.typeDefinition.getContext().isDisableExamples() ? null : new DataTypeExampleImpl(this.typeDefinition, registrationContext);
  }
}
