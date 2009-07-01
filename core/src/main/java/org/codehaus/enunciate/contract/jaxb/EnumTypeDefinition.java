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

package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.validation.Validator;
import org.jdom.*;
import org.jdom.Element;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.DeclarationDecorator;

/**
 * An enum type definition.
 *
 * @author Ryan Heaton
 */
public class EnumTypeDefinition extends SimpleTypeDefinition {

  private final XmlEnum xmlEnum;
  private final Map<String, String> enumValues;

  public EnumTypeDefinition(EnumDeclaration delegate) {
    super(delegate);

    this.xmlEnum = getAnnotation(XmlEnum.class);
    enumValues = new LinkedHashMap<String, String>();
    Collection<EnumConstantDeclaration> enumConstants = ((EnumDeclaration) getDelegate()).getEnumConstants();
    HashSet<String> enumValues = new HashSet<String>(enumConstants.size());
    for (EnumConstantDeclaration enumConstant : enumConstants) {
      String value = enumConstant.getSimpleName();
      XmlEnumValue enumValue = enumConstant.getAnnotation(XmlEnumValue.class);
      if (enumValue != null) {
        value = enumValue.value();
      }

      if (!enumValues.add(value)) {
        throw new ValidationException(enumConstant.getPosition(), getQualifiedName() + ": duplicate enum value: " + value);
      }

      this.enumValues.put(enumConstant.getSimpleName(), value);
    }

  }

  public Collection<EnumConstantDeclaration> getEnumConstants() {
    return DeclarationDecorator.decorate(((EnumDeclaration)delegate).getEnumConstants());
  }

  // Inherited.
  @Override
  public XmlType getBaseType() {
    XmlType xmlType = KnownXmlType.STRING;

    if (xmlEnum != null) {
      try {
        try {
          Class enumClass = xmlEnum.value();
          xmlType = XmlTypeFactory.getXmlType(enumClass);
        }
        catch (MirroredTypeException e) {
          xmlType = XmlTypeFactory.getXmlType(e.getTypeMirror());
        }
      }
      catch (XmlTypeException e) {
        throw new ValidationException(getPosition(), getQualifiedName() + ": " + e.getMessage());
      }
    }

    return xmlType;
  }

  /**
   * The enum base class.
   *
   * @return The enum base class.
   */
  public TypeMirror getEnumBaseClass() {
    try {
      Class enumClass = xmlEnum == null ? String.class : xmlEnum.value();
      return getEnumBaseClass(enumClass);
    }
    catch (MirroredTypeException e) {
      return e.getTypeMirror();
    }
  }

  /**
   * @param enumClass The enum class.
   *
   * @return The enum base class for the specified class.
   */
  protected TypeMirror getEnumBaseClass(Class enumClass) {
    if (enumClass.isPrimitive()) {
      if (Integer.TYPE == enumClass) {
        return getEnv().getTypeUtils().getPrimitiveType(PrimitiveType.Kind.INT);
      }
      else if (Boolean.TYPE == enumClass) {
        return getEnv().getTypeUtils().getPrimitiveType(PrimitiveType.Kind.BOOLEAN);
      }
      else if (Character.TYPE == enumClass) {
        return getEnv().getTypeUtils().getPrimitiveType(PrimitiveType.Kind.CHAR);
      }
      else if (Byte.TYPE == enumClass) {
        return getEnv().getTypeUtils().getPrimitiveType(PrimitiveType.Kind.BYTE);
      }
      else if (Short.TYPE == enumClass) {
        return getEnv().getTypeUtils().getPrimitiveType(PrimitiveType.Kind.SHORT);
      }
      else if (Long.TYPE == enumClass) {
        return getEnv().getTypeUtils().getPrimitiveType(PrimitiveType.Kind.LONG);
      }
      else if (Float.TYPE == enumClass) {
        return getEnv().getTypeUtils().getPrimitiveType(PrimitiveType.Kind.FLOAT);
      }
      else if (Double.TYPE == enumClass) {
        return getEnv().getTypeUtils().getPrimitiveType(PrimitiveType.Kind.DOUBLE);
      }
      else {
        throw new IllegalStateException();
      }
    }
    else if (enumClass.isArray()) {
      TypeMirror componentType = getEnumBaseClass(enumClass.getComponentType());
      return getEnv().getTypeUtils().getArrayType(componentType);
    }
    else {
      TypeDeclaration decl = getEnv().getTypeDeclaration(enumClass.getName());
      return getEnv().getTypeUtils().getDeclaredType(decl);
    }
  }

  /**
   * The map of constant declarations (simple names) to their enum constant values.
   *
   * @return The map of constant declarations to their enum constant values.
   */
  public Map<String, String> getEnumValues() {
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

  @Override
  public ValidationResult accept(Validator validator) {
    return validator.validateEnumType(this);
  }

  /**
   * The current environment.
   *
   * @return The current environment.
   */
  protected AnnotationProcessorEnvironment getEnv() {
    return Context.getCurrentEnvironment();
  }

  @Override
  public void generateExampleXml(Element parent) {
    parent.addContent(new org.jdom.Text("..."));
  }
}
