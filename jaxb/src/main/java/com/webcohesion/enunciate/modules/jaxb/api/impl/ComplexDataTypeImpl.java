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

import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxb.model.Attribute;
import com.webcohesion.enunciate.modules.jaxb.model.ComplexTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.Element;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.util.BeanValidationUtils;

import java.util.*;

/**
 * @author Ryan Heaton
 */
public class ComplexDataTypeImpl extends DataTypeImpl {

  final ComplexTypeDefinition typeDefinition;

  public ComplexDataTypeImpl(ComplexTypeDefinition typeDefinition) {
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
    ArrayList<Property> properties = new ArrayList<Property>();
    FacetFilter facetFilter = this.typeDefinition.getContext().getContext().getConfiguration().getFacetFilter();

    List<Property> attributeProperties = new ArrayList<Property>();
    for (Attribute attribute : this.typeDefinition.getAttributes()) {
      if (!facetFilter.accept(attribute)) {
        continue;
      }

      attributeProperties.add(new PropertyImpl(attribute));
    }

    if (this.typeDefinition.getPropertyOrder() == null) {
      //if the property order isn't explicit, sort the attributes by name, then add them to the list.
      Collections.sort(attributeProperties, new Comparator<Property>() {
        @Override
        public int compare(Property o1, Property o2) {
          return o1.getName().compareTo(o2.getName());
        }
      });
    }
    properties.addAll(attributeProperties);

    if (this.typeDefinition.getValue() != null) {
      properties.add(new PropertyImpl(this.typeDefinition.getValue()));
    }
    else {
      List<Property> elementProperties = new ArrayList<Property>();
      for (Element element : this.typeDefinition.getElements()) {
        if (!facetFilter.accept(element)) {
          continue;
        }

        boolean wrapped = element.isWrapped();
        String wrapperName = wrapped ? element.getWrapperName() : null;
        String wrapperNamespace = wrapped ? element.getWrapperNamespace() : null;
        for (Element choice : element.getChoices()) {
          elementProperties.add(wrapped ? new WrappedPropertyImpl(choice, wrapperName, wrapperNamespace) : new PropertyImpl(choice));
        }
      }

      if (this.typeDefinition.getPropertyOrder() == null) {
        //if the property order isn't explicit, sort the elements by name, then add them to the list.
        Collections.sort(elementProperties, new Comparator<Property>() {
          @Override
          public int compare(Property o1, Property o2) {
            return o1.getName().compareTo(o2.getName());
          }
        });
      }
      properties.addAll(elementProperties);
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

    XmlType supertype = this.typeDefinition.getBaseType();
    while (supertype != null) {
      if (supertypes == null) {
        supertypes = new ArrayList<DataTypeReference>();
      }

      supertypes.add(new DataTypeReferenceImpl(supertype, false));
      supertype = supertype instanceof XmlClassType ?
        ((XmlClassType)supertype).getTypeDefinition() instanceof ComplexTypeDefinition ?
          ((XmlClassType)supertype).getTypeDefinition().getBaseType()
          : null
        : null;
    }

    return supertypes;
  }

  @Override
  public Example getExample() {
    return this.typeDefinition.getContext().isDisableExamples() ? null : new ExampleImpl(this.typeDefinition);
  }

  @Override
  public Map<String, String> getPropertyMetadata() {
    Map<String, String> propertyMetadata = new LinkedHashMap<String, String>();
    propertyMetadata.put("type", "type");
    propertyMetadata.put("namespaceInfo", "namespace");
    propertyMetadata.put("minMaxOccurs", "min/max occurs");

    //if any elements have a default value, show that, too.
    boolean showDefaultValue = false;
    boolean showWrapper = false;
    boolean showConstraints = false;
    for (Element element : this.typeDefinition.getElements()) {
      for (Element choice : element.getChoices()) {
        if (BeanValidationUtils.hasConstraints(choice, choice.isRequired())) {
          showConstraints = true;
        }

        if (choice.getDefaultValue() != null) {
          showDefaultValue = true;
        }
      }

      if (element.isWrapped()) {
        showWrapper = true;
      }
    }

    if (showDefaultValue) {
      propertyMetadata.put("defaultValue", "default");
    }

    if (showConstraints) {
      propertyMetadata.put("constraints", "constraints");
    }

    if (showWrapper) {
      propertyMetadata.put("wrapper", "wrapped by");
    }

    return propertyMetadata;
  }
}
