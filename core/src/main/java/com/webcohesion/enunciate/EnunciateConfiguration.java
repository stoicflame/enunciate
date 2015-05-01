package com.webcohesion.enunciate;

import org.apache.commons.configuration.XMLConfiguration;

import java.io.File;

/**
 * @author Ryan Heaton
 */
public class EnunciateConfiguration {

  private String defaultLabel;
  private final XMLConfiguration source;
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

  public XMLConfiguration getSource() {
    return source;
  }

  public void setDefaultLabel(String defaultLabel) {
    this.defaultLabel = defaultLabel;
  }

  public String getLabel() {
    return this.source.getString("enunciate[@label]", this.defaultLabel);
  }
}
