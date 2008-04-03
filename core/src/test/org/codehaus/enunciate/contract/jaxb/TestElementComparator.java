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

import javax.xml.bind.annotation.XmlAccessOrder;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class TestElementComparator extends InAPTTestCase {

  /**
   * test the compare method
   */
  public void testCompare() throws Exception {
    ComplexTypeDefinition propertyOrderBean = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.PropertyOrderBean"));

    String[] propertyOrder = new String[]{"propertyE", "propertyC", "propertyA", "propertyB", "propertyD"};
    SortedSet<Element> elements = new TreeSet<Element>(new ElementComparator(propertyOrder, null));
    elements.addAll(propertyOrderBean.getElements());
    int index = 0;
    for (Element element : elements) {
      String name = element.getName();
      assertEquals(name + " is out of order.", propertyOrder[index++], name);
    }
    assertEquals(5, index);

    propertyOrder = new String[] {"propertyA", "propertyB", "propertyC", "propertyD", "propertyE"};
    elements = new TreeSet<Element>(new ElementComparator(null, XmlAccessOrder.ALPHABETICAL));
    elements.addAll(propertyOrderBean.getElements());
    index = 0;
    for (Element element : elements) {
      String name = element.getName();
      assertEquals(name + " is out of order.", propertyOrder[index++], name);
    }
    assertEquals(5, index);

    propertyOrder = new String[] {"propertyB", "propertyA", "propertyD", "propertyE", "propertyC"};
    elements = new TreeSet<Element>(new ElementComparator(null, null));
    elements.addAll(propertyOrderBean.getElements());
    index = 0;
    for (Element element : elements) {
      String name = element.getName();
      assertEquals(name + " is out of order.", propertyOrder[index++], name);
    }
    assertEquals(5, index);

  }

  public static Test suite() {
    return createSuite(TestElementComparator.class);
  }
}
