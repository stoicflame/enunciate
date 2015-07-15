package com.webcohesion.enunciate.api.services;

import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface Operation {

  String getName();

  String getSlug();

  String getDescription();

  String getDeprecated();

  String getSince();

  String getVersion();

  List<? extends Parameter> getInputParameters();

  List<? extends Parameter> getOutputParameters();

  DataTypeReference getReturnType();

  String getReturnDescription();

  List<? extends Fault> getFaults();
}
