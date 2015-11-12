package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;

/**
 * @author Ryan Heaton
 */
public class CustomMediaTypeDescriptor implements MediaTypeDescriptor {

  private final String mediaType;

  public CustomMediaTypeDescriptor(String mediaType) {
    this.mediaType = mediaType;
  }

  @Override
  public String getMediaType() {
    return mediaType;
  }

  @Override
  public DataTypeReference getDataType() {
    return null;
  }

  @Override
  public String getSyntax() {
    return null;
  }
}
