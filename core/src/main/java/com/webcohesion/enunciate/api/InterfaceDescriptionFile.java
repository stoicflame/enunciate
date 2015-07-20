package com.webcohesion.enunciate.api;

import java.io.File;
import java.io.IOException;

/**
 * @author Ryan Heaton
 */
public interface InterfaceDescriptionFile {

  String getHref();

  void writeTo(File directory, String apiRelativePath) throws IOException;
}
