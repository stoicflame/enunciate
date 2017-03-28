package com.webcohesion.enunciate.javac.javadoc;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;

/**
 * @author Ryan Heaton
 */
public class ReturnDocComment implements DocComment {

  private final DecoratedExecutableElement executableElement;
  private String value = null;

  public ReturnDocComment(DecoratedExecutableElement executableElement) {
    this.executableElement = executableElement;
  }

  @Override
  public String get() {
    if (this.value == null) {
      JavaDoc.JavaDocTagList tags = this.executableElement.getJavaDoc().get("return");
      this.value = tags == null ? "" : tags.toString();
    }

    return this.value;
  }
}
