package com.webcohesion.enunciate.metadata.rs;

/**
 * Documents an expected response header for a resource or resource method.
 *
 * @author Ryan Heaton
 */
public @interface ResponseHeader {

  /**
   * The name of the response header.
   *
   * @return The name of the response header.
   */
  String name();

  /**
   * The description of the response header.
   *
   * @return The description of the response header.
   */
  String description();
}
