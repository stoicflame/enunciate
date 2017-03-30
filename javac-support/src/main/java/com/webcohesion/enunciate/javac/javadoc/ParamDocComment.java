package com.webcohesion.enunciate.javac.javadoc;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;

import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class ParamDocComment implements DocComment {

  private final DecoratedExecutableElement executableElement;
  private final String paramName;
  private String value;

  public ParamDocComment(DecoratedExecutableElement executableElement, String paramName) {
    this.executableElement = executableElement;
    this.paramName = paramName;
  }

  @Override
  public String get() {
    if (this.value == null) {
      JavaDoc javaDoc = this.executableElement.getJavaDoc();
      HashMap<String, String> paramsComments = loadParamsComments(javaDoc);
      String comment = paramsComments.get(this.paramName);
      if (comment != null) {
        this.value = comment;
      }
      else {
        this.value = "";
      }

    }
    return this.value;
  }

  protected HashMap<String, String> loadParamsComments(JavaDoc javaDoc) {
    return loadParamsComments("param", javaDoc);
  }

  protected HashMap<String, String> loadParamsComments(String tagName, JavaDoc jd) {
    HashMap<String, String> paramComments = new HashMap<String, String>();
    if (jd.get(tagName) != null) {
      for (String paramDoc : jd.get(tagName)) {
        paramDoc = paramDoc.trim().replaceFirst("\\s+", " ");
        int spaceIndex = JavaDoc.indexOfFirstWhitespace(paramDoc);
        String param = paramDoc.substring(0, spaceIndex);
        String paramComment = "";
        if ((spaceIndex + 1) < paramDoc.length()) {
          paramComment = paramDoc.substring(spaceIndex + 1);
        }

        paramComments.put(param, paramComment);
      }
    }
    return paramComments;
  }
}
