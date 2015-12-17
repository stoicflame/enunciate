package com.webcohesion.enunciate.javac.javadoc;

/**
 * Handler to be used to define logic to perform for tags in JavaDoc comments.
 * 
 * @author Ryan Heaton
 */
public interface JavaDocTagHandler {

  /**
   * What to do with an inline JavaDoc tag.
   *
   * @param tagName The tag name.
   * @param tagText The tag text.
   * @return The text to replace the entire tag, or null for no replacement. If the replacement
   * implements {@link JavaDocTagHandler.TextToBeHandled}, it will also
   * be handled (recursively).
   */
  Object onInlineTag(String tagName, String tagText);

  /**
   * Marker interface for text that is also to be handled.
   */
  public static interface TextToBeHandled {}
}
