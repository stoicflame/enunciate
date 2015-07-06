package com.webcohesion.enunciate.api.resources;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface Entity {

  String getDescription();

  List<? extends MediaTypeDescriptor> getMediaTypes();
}
