package com.webcohesion.enunciate.javac.javadoc;

/**
 * Basic tag handler that does no replacement.
 *
 * @author Ryan Heaton
 */
public class NoOpTagHandler implements JavaDocTagHandler {

  public Object onInlineTag(String tagName, String tagText) {
    return null;
  }

  public Object onMarkupTag(String tagName, String tagBody) {
    return null;
  }
}
