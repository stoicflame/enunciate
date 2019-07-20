package com.webcohesion.enunciate.api;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.javadoc.DefaultJavaDocTagHandler;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

/**
 * @author Ryan Heaton
 */
public class DefaultRegistrationContext implements ApiRegistrationContext {

  private final EnunciateContext context;

  public DefaultRegistrationContext(EnunciateContext context) {
    this.context = context;
  }

  @Override
  public JavaDocTagHandler getTagHandler() {
    return DefaultJavaDocTagHandler.INSTANCE;
  }

  @Override
  public FacetFilter getFacetFilter() {
    return this.context.getConfiguration().getFacetFilter();
  }
}
