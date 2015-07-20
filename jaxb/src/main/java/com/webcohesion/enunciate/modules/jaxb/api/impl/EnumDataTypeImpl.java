package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.api.datatype.Value;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;

import javax.lang.model.element.VariableElement;
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
  public List<? extends Value> getValues() {
    //todo: support for filtering enum values by facet?

    Map<String, Object> enumValues = this.typeDefinition.getEnumValues();
    List<VariableElement> enumConstants = this.typeDefinition.getEnumConstants();
    ArrayList<Value> values = new ArrayList<Value>(enumValues.size());
    for (VariableElement constant : enumConstants) {
      Object enumValue = enumValues.get(constant.getSimpleName().toString());
      if (enumValue != null) {
        values.add(new ValueImpl(enumValue.toString(), ((DecoratedVariableElement)constant).getJavaDoc().toString()));
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
