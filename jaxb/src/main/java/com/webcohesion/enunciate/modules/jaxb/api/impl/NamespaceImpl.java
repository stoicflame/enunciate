package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.Namespace;
import com.webcohesion.enunciate.modules.jaxb.model.ComplexTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class NamespaceImpl implements Namespace {

  private final SchemaInfo schema;

  public NamespaceImpl(SchemaInfo schema) {
    this.schema = schema;
  }

  @Override
  public String getUri() {
    return this.schema.getNamespace();
  }

  @Override
  public File getSchemaFile() {
    return this.schema.getSchemaFile();
  }

  @Override
  public List<? extends DataType> getTypes() {
    //todo: filter by facet
    ArrayList<DataType> dataTypes = new ArrayList<DataType>();
    for (TypeDefinition typeDefinition : this.schema.getTypeDefinitions()) {
      if (typeDefinition instanceof ComplexTypeDefinition) {
        dataTypes.add(new ComplexDataTypeImpl((ComplexTypeDefinition) typeDefinition));
      }
      else if (typeDefinition instanceof EnumTypeDefinition) {
        dataTypes.add(new EnumDataTypeImpl((EnumTypeDefinition)typeDefinition));
      }
    }
    return dataTypes;
  }
}
