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
import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.datatype.BaseType;
import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.api.datatype.Value;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.EnumValue;
import com.webcohesion.enunciate.util.Memoized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class EnumDataTypeImpl extends DataTypeImpl {

  private final EnumTypeDefinition typeDefinition;

  public EnumDataTypeImpl(EnumTypeDefinition typeDefinition, ApiRegistrationContext registrationContext) {
    super(typeDefinition, registrationContext);
    this.typeDefinition = typeDefinition;
  }

  @Override
  public BaseType getBaseType() {
    return BaseType.string;
  }

  @Override
  public Value findValue(String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }
    for (EnumValue enumValue : this.typeDefinition.getEnumValues()) {
      if (name.equals(enumValue.getName())) {
        return createValue(enumValue);
      }
    }
    return null;
  }

  @Override
  public List<? extends Value> getValues() {
    FacetFilter facetFilter = this.registrationContext.getFacetFilter();
    List<EnumValue> enumValues = this.typeDefinition.getEnumValues();
    ArrayList<Value> values = new ArrayList<Value>(enumValues.size());
    for (EnumValue enumValue : enumValues) {
      if (enumValue.getValue() != null) {
        if (!facetFilter.accept(enumValue)) {
          continue;
        }

        values.add(createValue(enumValue));
      }
    }
    return values;
  }

  private ValueImpl createValue(EnumValue enumValue) {
    return new ValueImpl(enumValue.getValue().toString(), new Memoized<>(() -> enumValue.getJavaDoc(this.registrationContext.getTagHandler())),
            Styles.gatherStyles(enumValue, this.typeDefinition.getContext().getContext().getConfiguration().getAnnotationStyles()),
            enumValue.getFacets());
  }

  public String getXmlName() {
	return this.typeDefinition.getXmlName();
  }

  @Override
  public Property findProperty(String name) {
    return null;
  }

  @Override
  public List<? extends Property> getProperties() {
    return null;
  }

  @Override
  public Map<String, String> getPropertyMetadata() {
    return Collections.emptyMap();
  }
}
