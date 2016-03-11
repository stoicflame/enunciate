package com.webcohesion.enunciate.modules.jaxrs.model;

import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;

@SuppressWarnings ( "unchecked" )
public class ResourceTest {

  @Test
  public void testExtractPathComponents() throws Exception {
    LinkedHashMap<String, String> components = Resource.extractPathComponents("/path/{id}");
    assertEquals(2, components.size());
    Iterator<String> keys = components.keySet().iterator();
    assertEquals("path", keys.next());
    assertNull(components.get("path"));
    assertEquals("{id}", keys.next());
    assertNull(components.get("{id}"));

    components = Resource.extractPathComponents("/path/{id: [0-9]+}");
    assertEquals(2, components.size());
    keys = components.keySet().iterator();
    assertEquals("path", keys.next());
    assertNull(components.get("path"));
    assertEquals("{id}", keys.next());
    assertEquals("[0-9]+", components.get("{id}"));

    components = Resource.extractPathComponents("/path/{id: [0-9]+}/other");
    assertEquals(3, components.size());
    keys = components.keySet().iterator();
    assertEquals("path", keys.next());
    assertNull(components.get("path"));
    assertEquals("{id}", keys.next());
    assertEquals("[0-9]+", components.get("{id}"));
    assertEquals("other", keys.next());
    assertNull(components.get("other"));

    components = Resource.extractPathComponents("/path/{id: [0-9]+}/other/");
    assertEquals(3, components.size());
    keys = components.keySet().iterator();
    assertEquals("path", keys.next());
    assertNull(components.get("path"));
    assertEquals("{id}", keys.next());
    assertEquals("[0-9]+", components.get("{id}"));
    assertEquals("other", keys.next());
    assertNull(components.get("other"));

    components = Resource.extractPathComponents("/path/{file: [\\/A-Za-z0-9_\\-\\.]+.jpg}");
    assertEquals(2, components.size());
    keys = components.keySet().iterator();
    assertEquals("path", keys.next());
    assertNull(components.get("path"));
    assertEquals("{file}", keys.next());
    assertEquals("[\\/A-Za-z0-9_\\-\\.]+.jpg", components.get("{file}"));
  }

}