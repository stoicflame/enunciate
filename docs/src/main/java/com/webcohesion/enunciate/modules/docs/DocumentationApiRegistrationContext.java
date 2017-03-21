package com.webcohesion.enunciate.modules.docs;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandlerFactory;

/**
 * @author Ryan Heaton
 */
public class DocumentationApiRegistrationContext implements ApiRegistrationContext {

  @Override
  public JavaDocTagHandler getTagHandler() {
    return JavaDocTagHandlerFactory.getTagHandler();
  }
}
