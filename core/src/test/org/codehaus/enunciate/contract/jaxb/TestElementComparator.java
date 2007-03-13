package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;
import org.codehaus.enunciate.InAPTTestCase;

import javax.xml.bind.annotation.XmlAccessOrder;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Test;

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
