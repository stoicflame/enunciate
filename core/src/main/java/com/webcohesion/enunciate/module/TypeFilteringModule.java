package com.webcohesion.enunciate.module;

import org.reflections.adapters.MetadataAdapter;

/**
 * @author Ryan Heaton
 */
public interface TypeFilteringModule extends EnunciateModule {

  boolean acceptType(Object type, MetadataAdapter metadata);

}
