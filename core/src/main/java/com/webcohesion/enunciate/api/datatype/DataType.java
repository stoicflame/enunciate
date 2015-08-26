package com.webcohesion.enunciate.api.datatype;

import java.util.List;
import java.util.Map;

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

  BaseType getBaseType();

  List<DataTypeReference> getSupertypes();

  String getSince();

  String getVersion();

  Example getExample();

  List<? extends Value> getValues();

  List<? extends Property> getProperties();

  Map<String, String> getPropertyMetadata();
}
