package com.webcohesion.enunciate.api.datatype;

import java.util.List;

/**
* @author Ryan Heaton
*/
public class Namespace {

  private String uri;
  private String schemaFile;
  private List<? extends DataType> types;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getSchemaFile() {
    return schemaFile;
  }

  public void setSchemaFile(String schemaFile) {
    this.schemaFile = schemaFile;
  }

  public List<? extends DataType> getTypes() {
    return types;
  }

  public void setTypes(List<? extends DataType> types) {
    this.types = types;
  }
}
