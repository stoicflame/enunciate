package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.Namespace;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;

import java.io.File;
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
    throw new UnsupportedOperationException();
  }
}
