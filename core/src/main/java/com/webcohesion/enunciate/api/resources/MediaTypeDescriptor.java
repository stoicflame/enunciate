package com.webcohesion.enunciate.api.resources;

import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;

/**
 * @author Ryan Heaton
 */
public interface MediaTypeDescriptor {

  String getMediaType();

  DataTypeReference getDataType();
}
