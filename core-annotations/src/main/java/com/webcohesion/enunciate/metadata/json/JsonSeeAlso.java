package com.webcohesion.enunciate.metadata.json;

/**
 * Used to indicate other classes that Enunciate should consider as JSON types.
 *
 * @author Ryan Heaton
 */
public @interface JsonSeeAlso {

  Class[] value();
}
