package com.webcohesion.enunciate.module;

/**
 * @author Ryan Heaton
 */
public interface DependencySpec {

  boolean accept(EnunciateModule module);

  boolean isFulfilled();

}
