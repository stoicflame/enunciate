/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import junit.framework.Test;
import org.codehaus.enunciate.InAPTTestCase;

import javax.xml.namespace.QName;
import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class TestAttribute extends InAPTTestCase {

  /**
   * tests the general funcionality of the attribute.
   */
  public void testAttribute() throws Exception {
    fail();
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
