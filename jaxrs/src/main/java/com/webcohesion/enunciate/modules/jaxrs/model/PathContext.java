package com.webcohesion.enunciate.modules.jaxrs.model;

import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface PathContext {

  List<PathSegment> getPathComponents();

  EnunciateJaxrsContext getContext();
}
