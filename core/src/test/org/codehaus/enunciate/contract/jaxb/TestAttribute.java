package org.codehaus.enunciate.contract.jaxb;

import org.codehaus.enunciate.InAPTTestCase;
import com.sun.mirror.declaration.ClassDeclaration;

import javax.xml.namespace.QName;
import java.util.HashMap;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestAttribute extends InAPTTestCase {

  /**
   * tests the general funcionality of the attribute.
   */
  public void testAttribute() throws Exception {
    SimpleTypeDefinition typeDef = new SimpleTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.AccessorFilterBean"));

    HashMap<String, String> attributeNames = new HashMap<String, String>();
    attributeNames.put("property1", "property1");
    attributeNames.put("property2", "dummyname");
    attributeNames.put("property3", "property3");

    HashMap<String, String> attributeNamespaces = new HashMap<String, String>();
    attributeNamespaces.put("property1", "urn:other");
    attributeNamespaces.put("property2", "urn:attributebean");
    attributeNamespaces.put("property3", "urn:attributebean");

    HashMap<String, QName> attributeRefs = new HashMap<String, QName>();
    attributeRefs.put("property1", new QName("urn:other", "property1"));
    attributeRefs.put("property2", null);
    attributeRefs.put("property3", null);

    HashMap<String, Boolean> attributeRequireds = new HashMap<String, Boolean>();
    attributeRequireds.put("property1", false);
    attributeRequireds.put("property2", false);
    attributeRequireds.put("property3", true);

    for (Attribute attribute : typeDef.getAttributes()) {
      assertEquals("Wrong name for attribute " + attribute.getSimpleName(), attributeNames.get(attribute.getSimpleName()), attribute.getName());
      assertEquals("Wrong namespace for attribute " + attribute.getSimpleName(), attributeNamespaces.get(attribute.getSimpleName()), attribute.getNamespace());
      assertEquals("Wrong ref for attribute " + attribute.getSimpleName(), attributeRefs.get(attribute.getSimpleName()), attribute.getRef());
      assertEquals("Wrong required for attribute " + attribute.getSimpleName(), attributeRequireds.get(attribute.getSimpleName()).booleanValue(), attribute.isRequired());
    }
  }

  public static Test suite() {
    return createSuite(TestAttribute.class);
  }
}
