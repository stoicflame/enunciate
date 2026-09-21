package com.webcohesion.enunciate;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;
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

  /**
   * The markdown description is rendered with flexmark's core parser plus exactly two extensions.
   * Enunciate depends on those three artifacts individually rather than on flexmark-all, so these
   * assertions are what keeps that dependency set honest: drop one and the rendering silently
   * degrades to literal text rather than failing the build.
   */
  @Test
  public void markdownDescriptionUsesCoreParser() {
    EnunciateConfiguration config = loadConfiguration("<enunciate>" +
      "  <description format=\"markdown\">A **bold** and *italic* [link](http://example.com).</description>" +
      "</enunciate>");

    String description = config.readDescription(null, false, null);
    assertTrue(description, description.contains("<strong>bold</strong>"));
    assertTrue(description, description.contains("<em>italic</em>"));
    assertTrue(description, description.contains("<a href=\"http://example.com\">link</a>"));
  }

  @Test
  public void markdownDescriptionUsesTablesExtension() {
    EnunciateConfiguration config = loadConfiguration("<enunciate>" +
      "  <description format=\"markdown\">| a | b |\n| --- | --- |\n| 1 | 2 |\n</description>" +
      "</enunciate>");

    String description = config.readDescription(null, false, null);
    //without flexmark-ext-tables the pipes render as literal text in a paragraph.
    assertTrue(description, description.contains("<table>"));
    assertTrue(description, description.contains("<th>a</th>"));
    assertTrue(description, description.contains("<td>1</td>"));
  }

  @Test
  public void markdownDescriptionUsesStrikethroughExtension() {
    EnunciateConfiguration config = loadConfiguration("<enunciate>" +
      "  <description format=\"markdown\">This is ~~struck~~.</description>" +
      "</enunciate>");

    String description = config.readDescription(null, false, null);
    //without flexmark-ext-gfm-strikethrough the tildes render literally.
    assertTrue(description, description.contains("<del>struck</del>"));
  }

  @Test
  public void descriptionIsNotRenderedWhenRawOrNotMarkdown() {
    EnunciateConfiguration markdown = loadConfiguration("<enunciate>" +
      "  <description format=\"markdown\">A **bold** thing.</description>" +
      "</enunciate>");
    assertEquals("A **bold** thing.", markdown.readDescription(null, true, null));

    //html is the default format, and is passed through untouched.
    EnunciateConfiguration html = loadConfiguration("<enunciate>" +
      "  <description>A &lt;b&gt;bold&lt;/b&gt; thing.</description>" +
      "</enunciate>");
    assertEquals("A <b>bold</b> thing.", html.readDescription(null, false, null));
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
