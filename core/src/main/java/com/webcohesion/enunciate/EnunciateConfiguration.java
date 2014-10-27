package com.webcohesion.enunciate;

import org.apache.commons.configuration.XMLConfiguration;

import java.io.File;

/**
 * @author Ryan Heaton
 */
public class EnunciateConfiguration {

  final XMLConfiguration source;
  private File base;

  public EnunciateConfiguration() {
    this(new XMLConfiguration());
  }

  public EnunciateConfiguration(XMLConfiguration source) {
    this.source = source;
  }

  public void setBase(File base) {
    this.base = base;
  }
}
