package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.api.datatype.Value;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;
import com.webcohesion.enunciate.modules.jackson.model.EnumTypeDefinition;

import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
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
    //todo: filter by facet?

    Map<String, String> enumValues = this.typeDefinition.getEnumValues();
    List<VariableElement> enumConstants = this.typeDefinition.getEnumConstants();
    ArrayList<Value> values = new ArrayList<Value>(enumValues.size());
    for (VariableElement constant : enumConstants) {
      String enumValue = enumValues.get(constant.getSimpleName().toString());
      if (enumValue != null) {
        values.add(new ValueImpl(enumValue, ((DecoratedVariableElement)constant).getJavaDoc().toString()));
      }
    }
    return values;
  }

  @Override
  public List<? extends Property> getProperties() {
    return null;
  }

}
