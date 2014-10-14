package com.webcohesion.enunciate.module;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public interface EnunciateModule {

  String getName();

  Set<String> getModuleDependencies();

}
