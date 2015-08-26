package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.datatype.BaseType;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.modules.jaxb.model.ComplexTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.ElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.types.KnownXmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;

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

  public DataTypeReferenceImpl(XmlType xmlType, boolean list) {
    DataType dataType = null;
    QName elementQName = null;
    if (xmlType instanceof XmlClassType) {
      TypeDefinition typeDef = ((XmlClassType) xmlType).getTypeDefinition();
      if (typeDef instanceof ComplexTypeDefinition) {
        dataType = new ComplexDataTypeImpl((ComplexTypeDefinition) typeDef);
      }
      else if (typeDef instanceof EnumTypeDefinition) {
        dataType = new EnumDataTypeImpl((EnumTypeDefinition) typeDef);
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
  }

  public XmlType getXmlType() {
    return xmlType;
  }

  @Override
  public BaseType getBaseType() {
    QName qname = getXmlType().getQname();
    if (KnownXmlType.BOOLEAN.getQname().equals(qname)) {
      return BaseType.bool;
    }
    else if (KnownXmlType.BYTE.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.DECIMAL.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.DOUBLE.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.FLOAT.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.INT.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.INTEGER.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.LONG.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.NEGATIVE_INTEGER.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.NONNEGATIVE_INTEGER.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.NONPOSITIVE_INTEGER.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.POSITIVE_INTEGER.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.SHORT.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.UNSIGNED_BYTE.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.UNSIGNED_INT.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.UNSIGNED_LONG.getQname().equals(qname)) {
      return BaseType.number;
    }
    else if (KnownXmlType.UNSIGNED_SHORT.getQname().equals(qname)) {
      return BaseType.number;
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
}
