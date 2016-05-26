package com.webcohesion.enunciate.api.resources;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface StatusCode {

  int getCode();

  String getCondition();

  List<? extends MediaTypeDescriptor> getMediaTypes();
}
