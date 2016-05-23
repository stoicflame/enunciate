package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.datatype.BaseType;
import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.api.datatype.Value;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.EnumValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class EnumDataTypeImpl extends DataTypeImpl {

  private final EnumTypeDefinition typeDefinition;

  public EnumDataTypeImpl(EnumTypeDefinition typeDefinition) {
    super(typeDefinition);
    this.typeDefinition = typeDefinition;
  }

  @Override
  public BaseType getBaseType() {
    return BaseType.string;
  }

  @Override
  public List<? extends Value> getValues() {
    FacetFilter facetFilter = this.typeDefinition.getContext().getContext().getConfiguration().getFacetFilter();
    List<EnumValue> enumValues = this.typeDefinition.getEnumValues();
    ArrayList<Value> values = new ArrayList<Value>(enumValues.size());
    for (EnumValue enumValue : enumValues) {
      if (enumValue.getValue() != null) {
        if (!facetFilter.accept(enumValue)) {
          continue;
        }

        values.add(new ValueImpl(enumValue.getName(), enumValue.getJavaDoc().toString(), Styles.gatherStyles(enumValue, this.typeDefinition.getContext().getContext().getConfiguration().getAnnotationStyles())));
      }
    }
    return values;
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
