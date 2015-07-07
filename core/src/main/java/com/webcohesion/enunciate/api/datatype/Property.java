package com.webcohesion.enunciate.api.datatype;

/**
 * @author Ryan Heaton
 */
public interface Property {

  String getName();

  String getDescription();

  DataTypeReference getDataType();

}
