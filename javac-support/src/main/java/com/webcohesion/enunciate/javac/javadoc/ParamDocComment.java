package com.webcohesion.enunciate.javac.javadoc;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * @author Ryan Heaton
 */
public class ParamDocComment implements DocComment {

  private final DecoratedExecutableElement executableElement;
  private final String paramName;
  private final TreeMap<String, String> values;

  public ParamDocComment(DecoratedExecutableElement executableElement, String paramName) {
    this.executableElement = executableElement;
    this.paramName = paramName;
    this.values = new TreeMap<String, String>();
  }

  @Override
  public String get(JavaDocTagHandler tagHandler) {
    String value = this.values.get(tagHandler.getTypeId());
    if (value == null) {
      JavaDoc javaDoc = this.executableElement.getJavaDoc(tagHandler);
      HashMap<String, String> paramsComments = loadParamsComments(javaDoc);
      String comment = paramsComments.get(this.paramName);
      if (comment != null) {
        value = comment;
      }
      else {
        value = "";
      }
      this.values.put(tagHandler.getTypeId(), value);
    }
    return value;
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
