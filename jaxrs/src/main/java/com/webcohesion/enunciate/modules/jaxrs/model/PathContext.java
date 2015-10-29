package com.webcohesion.enunciate.modules.jaxrs.model;

import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;

import java.util.LinkedHashMap;

/**
 * @author Ryan Heaton
 */
public interface PathContext {

  LinkedHashMap<String, String> getPathComponents();

  EnunciateJaxrsContext getContext();
}
