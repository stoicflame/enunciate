package com.webcohesion.enunciate.module;

import com.webcohesion.enunciate.EnunciateOutput;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface EnunciateModule {

  String getName();

  Set<String> getModuleDependencies();

  boolean isEnabled();

  void call(EnunciateOutput output);

}
