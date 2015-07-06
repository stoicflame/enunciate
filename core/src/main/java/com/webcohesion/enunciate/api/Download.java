package com.webcohesion.enunciate.api;

import java.util.Date;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface Download {

  String getSlug();

  String getName();

  String getDescription();

  Date getCreated();

  List<? extends DownloadFile> getFiles();
}
