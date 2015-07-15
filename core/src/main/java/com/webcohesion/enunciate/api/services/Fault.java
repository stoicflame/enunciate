package com.webcohesion.enunciate.api.services;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;

/**
 * @author Ryan Heaton
 */
public interface Fault {

  String getName();

  String getDescription();

  DataTypeReference getDataType();
}
