package com.webcohesion.enunciate.api.datatype;

import java.util.List;

/**
* @author Ryan Heaton
*/
public interface Namespace {

  String getUri();

  String getSchemaFile();

  List<? extends DataType> getTypes();

}
