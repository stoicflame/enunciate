package com.webcohesion.enunciate.modules.spring_web.model;

import com.webcohesion.enunciate.modules.spring_web.EnunciateSpringWebContext;

import java.util.LinkedHashMap;

/**
 * @author Ryan Heaton
 */
public interface PathContext {

  LinkedHashMap<String, String> getPathComponents();

  EnunciateSpringWebContext getContext();
}
