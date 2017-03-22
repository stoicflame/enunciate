package com.webcohesion.enunciate.modules.docs;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

/**
 * @author Ryan Heaton
 */
public class ApiDocsRegistrationContext implements ApiRegistrationContext {

  private final ApiRegistry registry;

  public ApiDocsRegistrationContext(ApiRegistry registry) {
    this.registry = registry;
  }

  @Override
  public JavaDocTagHandler getTagHandler() {
    return new ApiDocsJavaDocTagHandler(this.registry, this);
  }
}
