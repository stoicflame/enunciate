package com.webcohesion.enunciate.javac.javadoc;

/**
 * Models the &#64;link inline tag in javadoc.
 *
 * @see <a href="https://docs.oracle.com/javase/7/docs/technotes/tools/windows/javadoc.html#link">@link documentation</a>
 */
public class JavaDocLink {
  private final String text;
  /**
   * Index of the first not whitespace character.
   */
  private final int classNameStart;
  /**
   * Index of '#' or end of the link target.
   */
  private final int classNameEnd;
  /**
   * Index of the member name start or -1 if link doesn't have '#'.
   */
  private final int memberNameStart;
  /**
   * Index of the member name end (points to '(' in case of a method reference) or -1 if link does't have '#'.
   */
  private final int memberNameEnd;
  /**
   * Index of label start or -1 otherwise.
   */
  private final int labelStart;
  private final int labelEnd;

  private JavaDocLink(String text, int classNameStart, int classNameEnd, int memberNameStart, int memberNameEnd, int labelStart, int labelEnd) {
    this.text = text;
    this.classNameStart = classNameStart;
    this.classNameEnd = classNameEnd;
    this.memberNameStart = memberNameStart;
    this.memberNameEnd = memberNameEnd;
    this.labelStart = labelStart;
    this.labelEnd = labelEnd;
  }

  /**
   * Returns the class name or an empty string if link is a within the current class (starts with `#`).
   */
  public String getClassName() {
    return text.substring(classNameStart, classNameEnd);
  }

  /**
   * Returns the member name from the link or an empty string.
   */
  public String getMemberName() {
    return memberNameStart > 0 ? text.substring(memberNameStart, memberNameEnd) : "";
  }

  /**
   * Answers if this link has an alternative label.
   */
  public boolean hasLabel() {
    return labelStart > 0;
  }

  /**
   * Returns label specified in this tag or {@code null} if omitted.
   */
  public String getLabel() {
    return labelStart > 0 ? text.substring(labelStart, labelEnd) : null;
  }

  @Override
  public String toString() {
    return text;
  }

  /**
   * Parses the specified text as a link.
   */
  public static JavaDocLink parse(String text) {
    int length = text.length();
    while (length > 0 && JavaDoc.isWhitespace(text.charAt(length - 1))) {
      --length;
    }
    int start = 0;
    while (start < length && JavaDoc.isWhitespace(text.charAt(start))) {
      ++start;
    }
    int fragmentStart = text.indexOf('#', start); //the start index of where we need to start looking for the value.
    int memberNameEnd = -1;
    int rightParen = -1;
    if (fragmentStart >= 0) {
      //if there's a '#' char, we have to check for a left-right paren pair before checking for the space.
      memberNameEnd = text.indexOf('(', fragmentStart);
      if (memberNameEnd >= 0) {
        rightParen = text.indexOf(')', memberNameEnd);
      }
    }
    int referenceEnd = length;
    int labelStart = -1;

    OUT:
    for (int i = rightParen < 0 ? start : rightParen; i < length; ++i) {
      if (JavaDoc.isWhitespace(text.charAt(i))) {
        referenceEnd = i;
        do {
          ++i;
          if (i >= length) {
            break OUT;
          }
        } while (JavaDoc.isWhitespace(text.charAt(i)));
        labelStart = i;
        break;
      }
    }
    if (memberNameEnd == -1) {
      memberNameEnd = referenceEnd;
    }
    return new JavaDocLink(text, start, fragmentStart >= 0 ? fragmentStart : referenceEnd, fragmentStart >= 0 ? fragmentStart + 1 : -1, memberNameEnd, labelStart, length);
  }
}