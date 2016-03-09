package com.webcohesion.enunciate.metadata.rs;

/**
 * Documents an expected request header for a resource or resource method.
 *
 * @author Ryan Heaton
 */
public @interface RequestHeader {

  /**
   * The name of the request header.
   *
   * @return The name of the request header.
   */
  String name();

  /**
   * The description of the request header.
   *
   * @return The description of the request header.
   */
  String description();
}
