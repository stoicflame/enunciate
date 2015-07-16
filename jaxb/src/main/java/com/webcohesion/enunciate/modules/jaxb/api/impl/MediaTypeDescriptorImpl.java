package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;

/**
 * @author Ryan Heaton
 */
public class MediaTypeDescriptorImpl implements MediaTypeDescriptor {

  private final String mediaType;
  private final DataTypeReference dataType;

  public MediaTypeDescriptorImpl(String mediaType, DataTypeReference dataType) {
    this.mediaType = mediaType;
    this.dataType = dataType;
  }

  @Override
  public String getMediaType() {
    return this.mediaType;
  }

  @Override
  public DataTypeReference getDataType() {
    return this.dataType;
  }
}
