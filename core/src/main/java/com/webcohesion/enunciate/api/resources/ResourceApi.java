package com.webcohesion.enunciate.api.resources;

import com.webcohesion.enunciate.api.InterfaceDescriptionFile;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface ResourceApi {

  boolean isIncludeResourceGroupName();

  String getContextPath();

  InterfaceDescriptionFile getWadlFile();

  List<ResourceGroup> getResourceGroups();

}
