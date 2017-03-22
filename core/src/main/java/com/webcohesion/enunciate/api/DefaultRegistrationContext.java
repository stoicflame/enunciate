package com.webcohesion.enunciate.api;

import com.webcohesion.enunciate.javac.javadoc.DefaultJavaDocTagHandler;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

/**
 * @author Ryan Heaton
 */
public class DefaultRegistrationContext implements ApiRegistrationContext {

  @Override
  public JavaDocTagHandler getTagHandler() {
    return DefaultJavaDocTagHandler.INSTANCE;
  }
}
