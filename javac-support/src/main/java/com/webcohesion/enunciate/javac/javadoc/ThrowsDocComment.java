package com.webcohesion.enunciate.javac.javadoc;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * @author Ryan Heaton
 */
public class ThrowsDocComment implements DocComment {

  private final DecoratedExecutableElement executableElement;
  private final String fqn;
  private final String simpleName;
  private final TreeMap<String, String> values = new TreeMap<String, String>();

  public ThrowsDocComment(DecoratedExecutableElement executableElement, String fqn) {
    this.executableElement = executableElement;
    this.fqn = fqn;
    this.simpleName = fqn.substring(fqn.lastIndexOf('.') + 1);
  }

  @Override
  public String get(JavaDocTagHandler tagHandler) {
    String value = this.values.get(tagHandler.getTypeId());
    if (value == null) {
      HashMap<String, String> throwsComments = new HashMap<String, String>();
      ArrayList<String> allThrowsComments = new ArrayList<String>();
      JavaDoc javaDoc = this.executableElement.getJavaDoc(tagHandler);
      if (javaDoc.get("throws") != null) {
        allThrowsComments.addAll(javaDoc.get("throws"));
      }

      if (javaDoc.get("exception") != null) {
        allThrowsComments.addAll(javaDoc.get("exception"));
      }

      for (String throwsDoc : allThrowsComments) {
        int spaceIndex = JavaDoc.indexOfFirstWhitespace(throwsDoc);
        String exception = throwsDoc.substring(0, spaceIndex);
        String throwsComment = "";
        if ((spaceIndex + 1) < throwsDoc.length()) {
          throwsComment = throwsDoc.substring(spaceIndex + 1);
        }

        throwsComments.put(exception, throwsComment);
      }

      String throwsComment = throwsComments.get(this.fqn);
      if (throwsComment == null) {
        //try keying off the simple name in case that is how it is referenced in the javadocs.
        throwsComment = throwsComments.get(this.simpleName);
      }

      if (throwsComment == null) {
        throwsComment = "";
      }

      value = throwsComment;
      this.values.put(tagHandler.getTypeId(), value);
    }

    return value;


  }
}
