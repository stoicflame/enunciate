/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.modules.jaxb.model.ComplexTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.ElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.types.KnownXmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.webcohesion.enunciate.api.datatype.BaseTypeFormat.*;

/**
 * @author Ryan Heaton
 */
public class DataTypeReferenceImpl implements DataTypeReference {

  private final XmlType xmlType;
  private final String label;
  private final String slug;
  private final List<ContainerType> containers;
  private final DataType dataType;
  private final QName elementQName;
  private final ApiRegistrationContext registrationContext;

  private static final Map<QName, BaseTypeAndFormat> type2typeformat = new HashMap<QName, BaseTypeAndFormat>();

  static {
    type2typeformat.put(KnownXmlType.BOOLEAN.getQname(), typeFormat(BaseType.bool, null));
    type2typeformat.put(KnownXmlType.BYTE.getQname(), typeFormat(BaseType.number, INT32));
    type2typeformat.put(KnownXmlType.DECIMAL.getQname(), typeFormat(BaseType.number, INT64)); // is this correct? can contain fractions
    type2typeformat.put(KnownXmlType.DOUBLE.getQname(), typeFormat(BaseType.number, DOUBLE));
    type2typeformat.put(KnownXmlType.FLOAT.getQname(), typeFormat(BaseType.number, FLOAT));
    type2typeformat.put(KnownXmlType.INT.getQname(), typeFormat(BaseType.number, INT32));
    type2typeformat.put(KnownXmlType.INTEGER.getQname(), typeFormat(BaseType.number, INT32));
    type2typeformat.put(KnownXmlType.LONG.getQname(), typeFormat(BaseType.number, INT64));
    type2typeformat.put(KnownXmlType.NEGATIVE_INTEGER.getQname(), typeFormat(BaseType.number, INT32));
    type2typeformat.put(KnownXmlType.NONNEGATIVE_INTEGER.getQname(), typeFormat(BaseType.number, INT32));
    type2typeformat.put(KnownXmlType.NONPOSITIVE_INTEGER.getQname(), typeFormat(BaseType.number, INT32));
    type2typeformat.put(KnownXmlType.POSITIVE_INTEGER.getQname(), typeFormat(BaseType.number, INT32));
    type2typeformat.put(KnownXmlType.SHORT.getQname(), typeFormat(BaseType.number, INT32));
    type2typeformat.put(KnownXmlType.UNSIGNED_BYTE.getQname(), typeFormat(BaseType.number, INT32));
    type2typeformat.put(KnownXmlType.UNSIGNED_INT.getQname(), typeFormat(BaseType.number, INT32));
    type2typeformat.put(KnownXmlType.UNSIGNED_LONG.getQname(), typeFormat(BaseType.number, INT64));
    type2typeformat.put(KnownXmlType.UNSIGNED_SHORT.getQname(), typeFormat(BaseType.number, INT32));
  }

  public DataTypeReferenceImpl(XmlType xmlType, boolean list, ApiRegistrationContext registrationContext) {
    DataType dataType = null;
    QName elementQName = null;
    if (xmlType instanceof XmlClassType) {
      TypeDefinition typeDef = ((XmlClassType) xmlType).getTypeDefinition();
      if (typeDef instanceof ComplexTypeDefinition) {
        dataType = new ComplexDataTypeImpl((ComplexTypeDefinition) typeDef, registrationContext);
      }
      else if (typeDef instanceof EnumTypeDefinition) {
        dataType = new EnumDataTypeImpl((EnumTypeDefinition) typeDef, registrationContext);
      }

      ElementDeclaration elementDecl = typeDef.getContext().findElementDeclaration(typeDef);
      if (elementDecl != null) {
        elementQName = elementDecl.getQname();
      }
    }

    this.xmlType = xmlType;
    this.label = dataType == null ? xmlType.getName() : dataType.getLabel();
    this.slug = dataType == null ? null : dataType.getSlug();
    this.containers = list ? Arrays.asList(ContainerType.list) : null;
    this.dataType = dataType;
    this.elementQName = elementQName;
    this.registrationContext = registrationContext;
  }

  public XmlType getXmlType() {
    return xmlType;
  }

  @Override
  public BaseTypeFormat getBaseTypeFormat() {
    QName qname = getXmlType().getQname();
    BaseTypeAndFormat tf = type2typeformat.get(qname);
    return tf == null ? null : tf.format;
  }

  @Override
  public BaseType getBaseType() {
    QName qname = getXmlType().getQname();
    BaseTypeAndFormat tf = type2typeformat.get(qname);
    if (tf != null) {
      return tf.type;
    }
    return xmlType.isSimple() ? BaseType.string : BaseType.object;
  }

  @Override
  public String getLabel() {
    return this.label;
  }

  @Override
  public String getSlug() {
    return this.slug;
  }

  @Override
  public List<ContainerType> getContainers() {
    return this.containers;
  }

  @Override
  public DataType getValue() {
    return this.dataType;
  }

  public QName getElementQName() {
    return this.elementQName;
  }

  @Override
  public Example getExample() {
    Example example = null;
    if (this.dataType instanceof ComplexDataTypeImpl) {
      ComplexTypeDefinition typeDefinition = ((ComplexDataTypeImpl) this.dataType).typeDefinition;
      example = typeDefinition == null || typeDefinition.getContext().isDisableExamples() ? null : new ComplexTypeExampleImpl(typeDefinition, this.containers, registrationContext);
    }
    return example;
  }

  static BaseTypeAndFormat typeFormat(BaseType type, BaseTypeFormat format) {
    return new BaseTypeAndFormat(type, format);
  }

  static class BaseTypeAndFormat {
    final BaseType type;
    final BaseTypeFormat format;

    BaseTypeAndFormat(BaseType type, BaseTypeFormat format) {
      this.type = type;
      this.format = format;
    }
  }
}
