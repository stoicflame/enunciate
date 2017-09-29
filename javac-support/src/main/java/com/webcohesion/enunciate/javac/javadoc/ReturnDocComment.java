package com.webcohesion.enunciate.javac.javadoc;

import java.util.TreeMap;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;

/**
 * @author Ryan Heaton
 */
public class ReturnDocComment implements DocComment {

  protected final DecoratedExecutableElement executableElement;
  private final TreeMap<String, String> values = new TreeMap<String, String>();

  public ReturnDocComment(DecoratedExecutableElement executableElement) {
    this.executableElement = executableElement;
  }

  @Override
  public String get(JavaDocTagHandler tagHandler) {
    String value = this.values.get(tagHandler.getTypeId());
    if (value == null) {
      JavaDoc.JavaDocTagList tags = this.executableElement.getJavaDoc(tagHandler).get("return");
      value = tags == null ? "" : tags.toString();
      this.values.put(tagHandler.getTypeId(), value);
    }

    return value;
  }
}
