/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webcohesion.enunciate.modules.jackson1.model;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.modules.jackson1.EnunciateJackson1Context;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson1.model.types.KnownJsonType;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An enum type definition.
 *
 * @author Ryan Heaton
 */
public class EnumTypeDefinition extends SimpleTypeDefinition {

  private Map<String, String> enumValues;

  public EnumTypeDefinition(TypeElement delegate, EnunciateJackson1Context context) {
    super(delegate, context);
  }

  protected Map<String, String> loadEnumValues() {
    List<VariableElement> enumConstants = getEnumConstants();
    Map<String, String> enumValueMap = new LinkedHashMap<String, String>();
    HashSet<String> enumValues = new HashSet<String>(enumConstants.size());
    for (VariableElement enumConstant : enumConstants) {
      String value = enumConstant.getSimpleName().toString();

      if (context.isHonorJaxb()) {
        XmlEnumValue enumValue = enumConstant.getAnnotation(XmlEnumValue.class);
        if (enumValue != null) {
          value = enumValue.value();
        }
      }

      if (!enumValues.add(value)) {
        throw new EnunciateException(getQualifiedName() + ": duplicate enum value: " + value);
      }

      enumValueMap.put(enumConstant.getSimpleName().toString(), value);
    }
    return enumValueMap;
  }

  // Inherited.
  @Override
  public JsonType getBaseType() {
    return KnownJsonType.STRING;
  }

  /**
   * The map of constant declarations (simple names) to their enum constant values.
   *
   * @return The map of constant declarations to their enum constant values.
   */
  public Map<String, String> getEnumValues() {
    if (this.enumValues == null) {
      this.enumValues = loadEnumValues();
    }

    return this.enumValues;
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean isEnum() {
    return true;
  }

}
