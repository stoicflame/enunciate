package com.webcohesion.enunciate.api.datatype;

import java.io.File;
import java.util.List;

/**
* @author Ryan Heaton
*/
public interface Namespace {

  String getUri();

  File getSchemaFile();

  List<? extends DataType> getTypes();

}
