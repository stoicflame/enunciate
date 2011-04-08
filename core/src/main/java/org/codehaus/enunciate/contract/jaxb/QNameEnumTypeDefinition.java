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
import com.sun.mirror.type.TypeMirror;
import org.codehaus.enunciate.qname.XmlQNameEnum;
import org.codehaus.enunciate.qname.XmlQNameEnumValue;
import org.codehaus.enunciate.qname.XmlUnknownQNameEnumValue;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.validation.Validator;
import org.jdom.Element;
import org.jdom.Text;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * A qname enum type definition.
 *
 * @author Ryan Heaton
 */
public class QNameEnumTypeDefinition extends EnumTypeDefinition {

  private final String namespace;

  public QNameEnumTypeDefinition(EnumDeclaration delegate) {
    super(delegate);

    XmlQNameEnum xmlQNameEnum = getAnnotation(XmlQNameEnum.class);
    if (xmlQNameEnum == null) {
      throw new IllegalArgumentException(delegate.getQualifiedName() + " is not a qname enum (not annotated with @org.codehaus.enunciate.qname.XmlQNameEnum)");
    }

    String namespace = getPackage().getNamespace();
    if (!"##default".equals(xmlQNameEnum.namespace())) {
      namespace = xmlQNameEnum.namespace();
    }
    this.namespace = namespace;
  }

  @Override
  protected Map<String, Object> loadEnumValues() {
    String namespace = getPackage().getNamespace();
    XmlQNameEnum xmlQNameEnum = getAnnotation(XmlQNameEnum.class);
    if (xmlQNameEnum != null && !"##default".equals(xmlQNameEnum.namespace())) {
      namespace = xmlQNameEnum.namespace();
    }
    if (namespace == null) {
      namespace = "";
    }

    Map<String, Object> enumValueMap = new LinkedHashMap<String, Object>();
    Collection<EnumConstantDeclaration> enumConstants = ((EnumDeclaration) getDelegate()).getEnumConstants();
    HashSet<QName> enumValues = new HashSet<QName>(enumConstants.size());
    String unknownQNameConstant = null;
    for (EnumConstantDeclaration enumConstant : enumConstants) {
      XmlUnknownQNameEnumValue unknownQNameEnumValue = enumConstant.getAnnotation(XmlUnknownQNameEnumValue.class);
      if (unknownQNameEnumValue != null) {
        if (unknownQNameConstant != null) {
          throw new ValidationException(enumConstant.getPosition(), getQualifiedName() + ": no more than two constants can be annotated with @XmlUnknownQNameEnumValue.");
        }

        unknownQNameConstant = enumConstant.getSimpleName();
        continue;
      }

      String ns = namespace;
      String localPart = enumConstant.getSimpleName();
      XmlQNameEnumValue enumValueInfo = enumConstant.getAnnotation(XmlQNameEnumValue.class);
      if (enumValueInfo != null) {
        if (enumValueInfo.exclude()) {
          continue;
        }

        if (!"##default".equals(enumValueInfo.namespace())) {
          ns = enumValueInfo.namespace();
        }
        if (!"##default".equals(enumValueInfo.localPart())) {
          localPart = enumValueInfo.localPart();
        }
      }

      QName qname = new QName(ns, localPart);
      if (!enumValues.add(qname)) {
        throw new ValidationException(enumConstant.getPosition(), getQualifiedName() + ": duplicate qname enum value: " + qname);
      }

      enumValueMap.put(enumConstant.getSimpleName(), qname);
    }

    if (unknownQNameConstant != null) {
      //enter the unknown qname constant as a null qname.
      enumValueMap.put(unknownQNameConstant, null);
    }
    
    return enumValueMap;
  }

  @Override
  public String getNamespace() {
    return this.namespace;
  }

  // Inherited.
  @Override
  public XmlType getBaseType() {
    return KnownXmlType.QNAME;
  }

  @Override
  public TypeMirror getEnumBaseClass() {
    TypeDeclaration decl = getEnv().getTypeDeclaration(QName.class.getName());
    return getEnv().getTypeUtils().getDeclaredType(decl);
  }

  @Override
  public boolean isSimple() {
    return false;
  }

  @Override
  public boolean isEnum() {
    return true;
  }

  public boolean isQNameEnum() {
    return true;
  }

  @Override
  public ValidationResult accept(Validator validator) {
    return new ValidationResult();
  }

  @Override
  public void generateExampleXml(Element parent) {
    parent.addContent(new Text("..."));
  }
}
