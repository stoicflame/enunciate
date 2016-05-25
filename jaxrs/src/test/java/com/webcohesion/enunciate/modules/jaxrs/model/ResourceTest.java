package com.webcohesion.enunciate.modules.jaxrs.model;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings ( "unchecked" )
public class ResourceTest {

  @Test
  public void testExtractPathComponents() throws Exception {
    List<PathSegment> components = Resource.extractPathComponents("/path/{id}");
    assertEquals(2, components.size());
    assertEquals("path", components.get(0).getValue());
    assertNull(components.get(0).getRegex());
    assertEquals("{id}", components.get(1).getValue());
    assertNull(components.get(1).getRegex());

    components = Resource.extractPathComponents("/path/{id: [0-9]+}");
    assertEquals(2, components.size());
    assertEquals("path", components.get(0).getValue());
    assertNull(components.get(0).getRegex());
    assertEquals("{id}", components.get(1).getValue());
    assertEquals("[0-9]+", components.get(1).getRegex());

    components = Resource.extractPathComponents("/path/{id: [0-9]+}/other");
    assertEquals(3, components.size());
    assertEquals("path", components.get(0).getValue());
    assertNull(components.get(0).getRegex());
    assertEquals("{id}", components.get(1).getValue());
    assertEquals("[0-9]+", components.get(1).getRegex());
    assertEquals("other", components.get(2).getValue());
    assertNull(components.get(2).getRegex());

    components = Resource.extractPathComponents("/path/{id: [0-9]+}/other/");
    assertEquals(3, components.size());
    assertEquals("path", components.get(0).getValue());
    assertNull(components.get(0).getRegex());
    assertEquals("{id}", components.get(1).getValue());
    assertEquals("[0-9]+", components.get(1).getRegex());
    assertEquals("other", components.get(2).getValue());
    assertNull(components.get(2).getRegex());

    components = Resource.extractPathComponents("/path/{file: [\\/A-Za-z0-9_\\-\\.]+.jpg}");
    assertEquals(2, components.size());
    assertEquals("path", components.get(0).getValue());
    assertNull(components.get(0).getRegex());
    assertEquals("{file}", components.get(1).getValue());
    assertEquals("[\\/A-Za-z0-9_\\-\\.]+.jpg", components.get(1).getRegex());
  }

}