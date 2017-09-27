package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.datatype.Example;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;

/**
 * @author Ryan Heaton
 */
public class MediaTypeDescriptorImpl implements MediaTypeDescriptor {

  private final MediaTypeDescriptor delegate;
  private final Example example;

  public MediaTypeDescriptorImpl(MediaTypeDescriptor delegate, Example example) {
    this.delegate = delegate;
    this.example = example;
  }

  @Override
  public String getMediaType() {
    return delegate.getMediaType();
  }

  @Override
  public DataTypeReference getDataType() {
    return delegate.getDataType();
  }

  @Override
  public String getSyntax() {
    return delegate.getSyntax();
  }

  @Override
  public float getQualityOfSourceFactor() {
    return delegate.getQualityOfSourceFactor();
  }

  @Override
  public Example getExample() {
    return this.example;
  }
}
