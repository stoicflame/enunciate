package com.webcohesion.enunciate;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Ryan Heaton
 */
public class EnunciateConfigurationTest {

  @Test
  public void binaryMediaTypesByConvention() {
    EnunciateConfiguration config = loadConfiguration("<enunciate/>");
    assertTrue(config.isBinaryMediaType("application/octet-stream"));
    assertTrue(config.isBinaryMediaType("image/png"));
    assertFalse(config.isBinaryMediaType("application/json"));
    assertFalse(config.isBinaryMediaType("application/vnd.acme.thing"));
    assertFalse(config.isBinaryMediaType(null));
  }

  @Test
  public void binaryMediaTypeOverrides() {
    EnunciateConfiguration config = loadConfiguration("<enunciate>" +
      "  <media-types>" +
      "    <media-type name=\"application/octet-stream\" binary=\"false\"/>" +
      "    <media-type name=\"Application/VND.acme.thing\"/>" +
      "    <media-type name=\"application/vnd.acme.other\" binary=\"true\"/>" +
      "  </media-types>" +
      "</enunciate>");

    //the convention can be turned off...
    assertFalse(config.isBinaryMediaType("application/octet-stream"));
    assertFalse(config.isBinaryMediaType("application/octet-stream;charset=UTF-8"));

    //...and turned on, with "binary" defaulting to true.
    assertTrue(config.isBinaryMediaType("application/vnd.acme.thing"));
    assertTrue(config.isBinaryMediaType("application/vnd.acme.other"));

    //media types that aren't mentioned still follow the convention.
    assertTrue(config.isBinaryMediaType("application/pdf"));
    assertFalse(config.isBinaryMediaType("application/json"));
  }

  private EnunciateConfiguration loadConfiguration(String xml) {
    XMLConfiguration source = EnunciateConfiguration.createDefaultConfigurationSource();
    try {
      new FileHandler(source).load(new StringReader(xml));
    }
    catch (ConfigurationException e) {
      throw new IllegalStateException(e);
    }
    return new EnunciateConfiguration(source);
  }
}
