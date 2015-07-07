package com.webcohesion.enunciate.api.datatype;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface DataTypeReference {

  enum ContainerType {
    array,

    collection,

    list
  }

  String getLabel();

  String getSlug();

  List<ContainerType> getContainers();

  DataType getValue();

}
