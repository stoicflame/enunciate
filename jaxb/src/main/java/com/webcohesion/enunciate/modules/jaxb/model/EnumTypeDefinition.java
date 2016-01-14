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

package com.webcohesion.enunciate.modules.jaxb.model;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.types.KnownXmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlTypeFactory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * An enum type definition.
 *
 * @author Ryan Heaton
 */
public class EnumTypeDefinition extends SimpleTypeDefinition {

  private final XmlEnum xmlEnum;
  private List<EnumValue> enumValues;

  public EnumTypeDefinition(TypeElement delegate, EnunciateJaxbContext context) {
    super(delegate, context);
    this.xmlEnum = getAnnotation(XmlEnum.class);
  }

  protected List<EnumValue> loadEnumValues() {
    List<VariableElement> enumConstants = enumValues();
    List<EnumValue> enumValues = new ArrayList<EnumValue>();
    HashSet<String> enumValueNames = new HashSet<String>(enumConstants.size());
    for (VariableElement enumConstant : enumConstants) {
      String value = enumConstant.getSimpleName().toString();
      XmlEnumValue enumValue = enumConstant.getAnnotation(XmlEnumValue.class);
      if (enumValue != null) {
        value = enumValue.value();
      }

      if (!enumValueNames.add(value)) {
        throw new EnunciateException(getQualifiedName() + ": duplicate enum value: " + value);
      }

      enumValues.add(new EnumValue(this, enumConstant, enumConstant.getSimpleName().toString(), value));
    }
    return enumValues;
  }

  // Inherited.
  @Override
  public XmlType getBaseType() {
    XmlType xmlType = KnownXmlType.STRING;

    if (xmlEnum != null) {
      DecoratedTypeMirror typeMirror = Annotations.mirrorOf(new Callable<Class<?>>() {
        @Override
        public Class<?> call() throws Exception {
          return xmlEnum.value();
        }
      }, this.env);

      xmlType = XmlTypeFactory.getXmlType(typeMirror, this.context);
    }

    return xmlType;
  }

  /**
   * The enum base class.
   *
   * @return The enum base class.
   */
  public DecoratedTypeMirror getEnumBaseClass() {
    try {
      return TypeMirrorUtils.mirrorOf(xmlEnum == null ? String.class : xmlEnum.value(), this.env);
    }
    catch (MirroredTypeException e) {
      return (DecoratedTypeMirror) TypeMirrorDecorator.decorate(e.getTypeMirror(), this.env);
    }
  }

  /**
   * The enum values for this type definition.
   *
   * @return The enum values for this type definition.
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
    return getAnnotation(XmlJavaTypeAdapter.class) == null;
  }

}
