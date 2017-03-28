package com.webcohesion.enunciate.modules.jaxrs.model.util;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.ParamDocComment;

import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class RSParamDocComment extends ParamDocComment {

  public RSParamDocComment(DecoratedExecutableElement executableElement, String paramName) {
    super(executableElement, paramName);
  }

  @Override
  protected HashMap<String, String> loadParamsComments(JavaDoc javaDoc) {
    HashMap<String, String> paramComments = super.loadParamsComments("RSParam", javaDoc);
    paramComments.putAll(super.loadParamsComments(javaDoc));
    return paramComments;
  }
}
