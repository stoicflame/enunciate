package com.webcohesion.enunciate.modules.spring_web.model.util;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.ReturnDocComment;

/**
 * @author Ryan Heaton
 */
public class ReturnWrappedDocComment extends ReturnDocComment {

  private final String returnWrapped;

  public ReturnWrappedDocComment(DecoratedExecutableElement executableElement, String returnWrapped) {
    super(executableElement);
    this.returnWrapped = returnWrapped;
  }

  @Override
  public String get() {
    if (this.returnWrapped != null) {
      int firstSpace = JavaDoc.indexOfFirstWhitespace(returnWrapped);
      if (firstSpace > 1) {
        if (returnWrapped.length() > firstSpace + 1) {
          String wrappedDoc = returnWrapped.substring(firstSpace + 1).trim();
          if (!wrappedDoc.isEmpty()) {
            return wrappedDoc;
          }
        }
      }
    }

    return super.get();
  }
}
