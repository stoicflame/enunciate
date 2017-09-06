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
package com.webcohesion.enunciate.modules.jackson.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson.model.types.KnownJsonType;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.*;

/**
 * An enum type definition.
 *
 * @author Ryan Heaton
 */
public class EnumTypeDefinition extends SimpleTypeDefinition {

  private List<EnumValue> enumValues;

  public EnumTypeDefinition(TypeElement delegate, EnunciateJacksonContext context) {
    super(delegate, context);
  }

  protected List<EnumValue> loadEnumValues() {
    List<VariableElement> enumConstants = enumValues();
    List<EnumValue> enumValueMap = new ArrayList<EnumValue>();
    HashSet<String> enumValues = new HashSet<String>(enumConstants.size());
    for (VariableElement enumConstant : enumConstants) {
      String value = enumConstant.getSimpleName().toString();

      if (context.isHonorJaxb()) {
        XmlEnumValue enumValue = enumConstant.getAnnotation(XmlEnumValue.class);
        if (enumValue != null) {
          value = enumValue.value();
        }
      }

      JsonProperty jsonProperty = enumConstant.getAnnotation(JsonProperty.class);
      if (jsonProperty != null) {
        value = jsonProperty.value();
      }

      if (!enumValues.add(value)) {
        throw new EnunciateException(getQualifiedName() + ": duplicate enum value: " + value);
      }

      enumValueMap.add(new EnumValue(this, enumConstant, enumConstant.getSimpleName().toString(), value));
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
  public List<EnumValue> getEnumValues() {
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
