package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.datatype.BaseType;
import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.api.datatype.Value;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.modules.jackson.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jackson.model.EnumValue;

import java.util.ArrayList;
import java.util.List;

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

        values.add(new ValueImpl(enumValue.getValue(), enumValue.getJavaDoc().toString(), Styles.gatherStyles(enumValue, this.typeDefinition.getContext().getContext().getConfiguration().getAnnotationStyles())));
      }
    }
    return values;
  }

  @Override
  public List<? extends Property> getProperties() {
    return null;
  }

}
