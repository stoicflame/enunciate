package com.webcohesion.enunciate.modules.docs;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

/**
 * @author Ryan Heaton
 */
public class DocsRegistrationContext implements ApiRegistrationContext {

  private final ApiDocsJavaDocTagHandler tagHandler;
  private final FacetFilter facetFilter;

  public DocsRegistrationContext(ApiRegistry registry, FacetFilter facetFilter) {
    this.tagHandler = new ApiDocsJavaDocTagHandler(registry, this);
    this.facetFilter = facetFilter;
  }

  @Override
  public JavaDocTagHandler getTagHandler() {
    return this.tagHandler;
  }

  @Override
  public FacetFilter getFacetFilter() {
    return this.facetFilter;
  }
}
