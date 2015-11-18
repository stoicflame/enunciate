package com.webcohesion.enunciate.modules.spring_web.model;

import com.webcohesion.enunciate.modules.spring_web.EnunciateSpringWebContext;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface PathContext {

  List<PathSegment> getPathSegments();

  EnunciateSpringWebContext getContext();
}
