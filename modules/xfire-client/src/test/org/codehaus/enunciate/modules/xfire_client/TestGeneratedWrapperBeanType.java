/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.modules.xfire_client;

import junit.framework.TestCase;
import org.codehaus.xfire.aegis.stax.ElementReader;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.aegis.jdom.JDOMWriter;
import org.codehaus.xfire.MessageContext;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.TreeSet;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Ryan Heaton
 */
public class TestGeneratedWrapperBeanType extends TestCase {

  /**
   * tests reading the object from xml.
   */
  public void testReadObject() throws Exception {
    GeneratedWrapperBeanType type = new GeneratedWrapperBeanType(GeneratedWrapperBeanExample.class);
    type.setTypeMapping(new DefaultTypeMappingRegistry(true).getDefaultTypeMapping());
    Object obj = type.readObject(new ElementReader(TestGeneratedWrapperBeanType.class.getResourceAsStream("testReadObject.xml")), new MessageContext());
    assertTrue(obj instanceof GeneratedWrapperBeanExample);
    GeneratedWrapperBeanExample ex = (GeneratedWrapperBeanExample) obj;
    assertEquals(1234.5678, ex.getSimple());
    String[] strings = ex.getStrings();
    assertEquals(4, strings.length);
    for (int i = 0; i < strings.length; i++) {
      String string = strings[i];
      assertEquals("string" + (i + 1), string);
    }

    Integer[] integers = (Integer[]) ex.getIntegers().toArray(new Integer[ex.getIntegers().size()]);
    assertEquals(5, integers.length);
    assertEquals(3, integers[0].intValue());
    assertEquals(200, integers[1].intValue());
    assertEquals(400, integers[2].intValue());
    assertEquals(1000, integers[3].intValue());
    assertEquals(9876, integers[4].intValue());
  }

  /**
   * tests writing the object.
   */
  public void testWriteObject() throws Exception {
    GeneratedWrapperBeanType type = new GeneratedWrapperBeanType(GeneratedWrapperBeanExample.class);
    type.setTypeMapping(new DefaultTypeMappingRegistry(true).getDefaultTypeMapping());
    GeneratedWrapperBeanExample ex = new GeneratedWrapperBeanExample();
    ex.setSimple(6789.5432);
    ex.setIntegers(new TreeSet(Arrays.asList(8, 7, 6, 4)));
    String[] strings = new String[]{"hello", "how", "are", "you"};
    ex.setStrings(strings);
    Element element = new Element("bean", "urn:generated-wrapper-bean-example");
    type.writeObject(ex, new JDOMWriter(element), new MessageContext());
    List list = element.getChildren("simple", Namespace.getNamespace("urn:generated-wrapper-bean-example"));
    assertEquals(1, list.size());
    assertEquals("6789.5432", ((Element) list.get(0)).getText());

    list = element.getChildren("strings", Namespace.getNamespace("urn:generated-wrapper-bean-example"));
    assertEquals(4, list.size());
    ArrayList<String> stringList = new ArrayList<String>(Arrays.asList(strings));
    for (Object item : list) {
      assertTrue(stringList.remove(((Element) item).getText()));
    }

    list = element.getChildren("integers", Namespace.getNamespace("urn:generated-wrapper-bean-example"));
    assertEquals(4, list.size());
    assertEquals("4", ((Element) list.get(0)).getText());
    assertEquals("6", ((Element) list.get(1)).getText());
    assertEquals("7", ((Element) list.get(2)).getText());
    assertEquals("8", ((Element) list.get(3)).getText());
  }
}
