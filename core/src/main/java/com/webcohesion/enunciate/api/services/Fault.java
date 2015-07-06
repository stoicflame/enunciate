package com.webcohesion.enunciate.api.services;

import com.webcohesion.enunciate.api.datatype.DataType;

/**
 * @author Ryan Heaton
 */
public interface Fault {

  String getName();

  String getDescription();

  DataType getDataType();
}
