package com.webcohesion.enunciate.modules.docs;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

/**
 * @author Ryan Heaton
 */
public class ApiDocsRegistrationContext implements ApiRegistrationContext {

  private final ApiDocsJavaDocTagHandler tagHandler;

  public ApiDocsRegistrationContext(ApiRegistry registry) {
    this.tagHandler = new ApiDocsJavaDocTagHandler(registry, this);
  }

  @Override
  public JavaDocTagHandler getTagHandler() {
    return this.tagHandler;
  }
}
