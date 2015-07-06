package com.webcohesion.enunciate.api.datatype;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface DataType {

  String getLabel();

  String getSlug();

  String getDescription();

  String getDeprecated();

  Namespace getNamespace();

  Syntax getSyntax();

  DataType getSupertype();

  String getSince();

  String getVersion();

  Example getExample();

  List<? extends Value> getValues();

  List<? extends Property> getProperties();

  List<String> getPropertyMetadata();
}
