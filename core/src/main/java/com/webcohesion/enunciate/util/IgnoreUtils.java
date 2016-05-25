package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.metadata.Ignore;

import javax.lang.model.element.Element;

/**
 * @author Ryan Heaton
 */
public class IgnoreUtils {

  private IgnoreUtils() {}

  public static boolean isIgnored(Element element) {
    return element.getAnnotation(Ignore.class) != null;
  }
}
