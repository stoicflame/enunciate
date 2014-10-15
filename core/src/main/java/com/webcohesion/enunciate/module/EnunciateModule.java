package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.EnunciateContext;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface EnunciateModule {

  String getName();

  Set<String> getModuleDependencies();

  boolean isEnabled();

  void init(EnunciateContext context);

  void call(EnunciateContext context);

}
