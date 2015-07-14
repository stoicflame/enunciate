package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.ComplexTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.SimpleTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.util.MapType;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class DataTypeReferenceImpl implements DataTypeReference {

  private final String label;
  private final String slug;
  private final List<ContainerType> containers;
  private final DataType dataType;

  public DataTypeReferenceImpl(XmlType xmlType, EnunciateJaxbContext context, boolean list) {
    DataType dataType = null;
    if (xmlType instanceof XmlClassType) {
      TypeDefinition typeDef = ((XmlClassType) xmlType).getTypeDefinition();
      if (typeDef instanceof ComplexTypeDefinition) {
        dataType = new ComplexDataTypeImpl((ComplexTypeDefinition) typeDef);
      }
      else if (typeDef instanceof EnumTypeDefinition) {
        dataType = new EnumDataTypeImpl((EnumTypeDefinition) typeDef);
      }
    }


    this.label = dataType == null ? xmlType.getName() : dataType.getLabel();
    this.slug = dataType == null ? null : dataType.getSlug();
    this.containers = list ? Arrays.asList(ContainerType.list) : null;
    this.dataType = dataType;
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
}
