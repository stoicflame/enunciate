package com.webcohesion.enunciate.api.datatype;

import com.webcohesion.enunciate.api.InterfaceDescriptionFile;

import java.util.List;

/**
* @author Ryan Heaton
*/
public interface Namespace {

  String getUri();

  InterfaceDescriptionFile getSchemaFile();

  List<? extends DataType> getTypes();

}
