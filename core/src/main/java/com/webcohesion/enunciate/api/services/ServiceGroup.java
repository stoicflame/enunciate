package com.webcohesion.enunciate.api.services;

import java.io.File;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface ServiceGroup {

  String getNamespace();

  File getWsdlFile();

  List<? extends Service> getServices();
}
