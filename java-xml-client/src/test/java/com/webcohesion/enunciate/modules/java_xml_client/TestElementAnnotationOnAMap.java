/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.java_xml_client;

import junit.framework.TestCase;

import jakarta.xml.bind.JAXBContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class TestElementAnnotationOnAMap extends TestCase {

  /**
   * tests the element annotation on a map.
   */
  public void testElementAnnotationOnAMap() throws Exception {
    ElementWithMapProperty el = new ElementWithMapProperty();
    HashMap<String, String> stuff = new HashMap<String, String>();
    stuff.put("a", "b");
    stuff.put("c", "d");
    el.setStuff(stuff);
    JAXBContext context = JAXBContext.newInstance(ElementWithMapProperty.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    context.createMarshaller().marshal(el, out);
    byte[] bytes = out.toByteArray();
    //System.out.println(new String(bytes, "utf-8"));
    el = (ElementWithMapProperty) context.createUnmarshaller().unmarshal(new ByteArrayInputStream(bytes));
    assertEquals("b", el.getStuff().get("a"));
    assertEquals("d", el.getStuff().get("c"));
  }
}
