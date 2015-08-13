package org.codehaus.enunciate.modules.jaxws_client;

import junit.framework.TestCase;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
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
