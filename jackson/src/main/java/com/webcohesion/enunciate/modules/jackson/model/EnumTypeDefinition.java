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
import com.fasterxml.jackson.annotation.JsonValue;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson.model.types.KnownJsonType;
import com.webcohesion.enunciate.modules.jackson.model.util.JacksonUtil;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.xml.bind.annotation.XmlEnumValue;
import java.util.*;

/**
 * An enum type definition.
 *
 * @author Ryan Heaton
 */
public class EnumTypeDefinition extends SimpleTypeDefinition {

  private List<EnumValue> enumValues;
  private KnownJsonType baseType;

  public EnumTypeDefinition(TypeElement delegate, EnunciateJacksonContext context) {
    super(delegate, context);

    this.baseType = loadBaseType(delegate);
  }

  protected KnownJsonType loadBaseType(TypeElement delegate) {
    KnownJsonType baseType = KnownJsonType.STRING;
    for (ExecutableElement method : ElementFilter.methodsIn(delegate.getEnclosedElements())) {
      JsonValue jsonValue = method.getAnnotation(JsonValue.class);
      if (jsonValue != null && jsonValue.value()) {
        TypeMirror returnType = method.getReturnType();
        switch (returnType.getKind()) {
          case BOOLEAN:
            baseType = KnownJsonType.BOOLEAN;
            break;
          case FLOAT:
          case DOUBLE:
            baseType = KnownJsonType.NUMBER;
            break;
          case INT:
          case LONG:
          case SHORT:
          case BYTE:
            baseType = KnownJsonType.WHOLE_NUMBER;
            break;
        }
        break;
      }
    }
    return baseType;
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

      if (context.isHonorGson()) {
        AnnotationValue av = JacksonUtil.findAnnotationValueByName(JacksonUtil.findAnnotationByClassName(
            enumConstant.getAnnotationMirrors(), JacksonUtil.GSON_SERIALIZED_NAME_CLASS), "value");
        if (av != null) {
          value = (String) av.getValue();
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
    return baseType;
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
