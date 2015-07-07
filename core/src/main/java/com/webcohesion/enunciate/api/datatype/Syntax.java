package com.webcohesion.enunciate.api.datatype;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface Syntax {

  String getSlug();

  String getLabel();

  List<Namespace> getNamespaces();

}
