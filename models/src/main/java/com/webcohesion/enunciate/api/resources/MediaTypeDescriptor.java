package com.webcohesion.enunciate.api.resources;

import com.webcohesion.enunciate.api.datatype.DataType;

/**
 * @author Ryan Heaton
 */
public interface MediaTypeDescriptor {

  String getMediaType();

  DataType getDataType();
}
