package com.webcohesion.enunciate.api.services;

import com.webcohesion.enunciate.api.InterfaceDescriptionFile;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface ServiceGroup {

  String getNamespace();

  InterfaceDescriptionFile getWsdlFile();

  List<? extends Service> getServices();
}
