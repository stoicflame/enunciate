/*
 * © 2019 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.javadoc.DefaultJavaDocTagHandler;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

public class SwaggerRegistrationContext implements ApiRegistrationContext {

  private final FacetFilter facetFilter;

  public SwaggerRegistrationContext(FacetFilter facetFilter) {
    this.facetFilter = facetFilter;
  }

  @Override
  public JavaDocTagHandler getTagHandler() {
    return DefaultJavaDocTagHandler.INSTANCE;
  }

  @Override
  public FacetFilter getFacetFilter() {
    return this.facetFilter;
  }
}
