package com.webcohesion.enunciate.api.datatype;

import com.webcohesion.enunciate.api.HasStyles;

/**
 * @author Ryan Heaton
 */
public interface Value extends HasStyles {

  String getValue();

  String getDescription();
}
