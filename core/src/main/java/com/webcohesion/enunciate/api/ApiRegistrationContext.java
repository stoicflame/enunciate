package com.webcohesion.enunciate.api;

import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

/**
 * @author Ryan Heaton
 */
public interface ApiRegistrationContext {

  JavaDocTagHandler getTagHandler();

  FacetFilter getFacetFilter();
}
