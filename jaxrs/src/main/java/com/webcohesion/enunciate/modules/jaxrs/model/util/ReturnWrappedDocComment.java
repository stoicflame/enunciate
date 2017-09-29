package com.webcohesion.enunciate.modules.jaxrs.model.util;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;
import com.webcohesion.enunciate.javac.javadoc.ReturnDocComment;

/**
 * @author Ryan Heaton
 */
public class ReturnWrappedDocComment extends ReturnDocComment {

  public ReturnWrappedDocComment(DecoratedExecutableElement executableElement) {
    super(executableElement);
  }

  @Override
  public String get(JavaDocTagHandler tagHandler) {
    JavaDoc.JavaDocTagList tagList = this.executableElement.getJavaDoc(tagHandler).get("returnWrapped");
    String returnWrapped = tagList == null || tagList.isEmpty() ? null : tagList.get(0);
    if (returnWrapped != null) {
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

    return super.get(tagHandler);
  }
}
