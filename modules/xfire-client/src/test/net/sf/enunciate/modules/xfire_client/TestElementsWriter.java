package net.sf.enunciate.modules.xfire_client;

import junit.framework.TestCase;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.jdom.JDOMWriter;
import org.codehaus.xfire.aegis.type.DefaultTypeMappingRegistry;
import org.codehaus.xfire.aegis.type.TypeMapping;
import org.jdom.Element;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Arrays;

/**
 * @author Ryan Heaton
 */
public class TestElementsWriter extends TestCase {

  /**
   * Tests writing stuff.
   */
  public void testWriteStuff() throws Exception {
    QName itemName = new QName("urn:TestElementsWriter", "item");
    TypeMapping typeMapping = new DefaultTypeMappingRegistry(true).createTypeMapping(true);

    Element parentElement = new Element("parent");
    JDOMWriter parentWriter = new JDOMWriter(parentElement);
    ElementsWriter.writeElements(new boolean[] {true, true, false}, itemName, parentWriter, typeMapping, new MessageContext());
    List childElements = parentElement.getChildren();
    assertEquals(3, childElements.size());
    for (int i = 0; i < childElements.size(); i++) {
      Element childElement = (Element) childElements.get(i);
      assertEquals("item", childElement.getName());
      assertEquals("urn:TestElementsWriter", childElement.getNamespaceURI());
      if (i < 2) {
        assertEquals("true", childElement.getText());
      }
      else {
        assertEquals("false", childElement.getText());
      }
    }

    parentElement = new Element("parent");
    parentWriter = new JDOMWriter(parentElement);
    ElementsWriter.writeElements(new Object[] {"item1", "item2", "item3"}, itemName, parentWriter, typeMapping, new MessageContext());
    childElements = parentElement.getChildren();
    assertEquals(3, childElements.size());
    for (int i = 0; i < childElements.size(); i++) {
      Element childElement = (Element) childElements.get(i);
      assertEquals("item", childElement.getName());
      assertEquals("urn:TestElementsWriter", childElement.getNamespaceURI());
      assertEquals(String.format("item%s", i + 1), childElement.getText());
    }

    parentElement = new Element("parent");
    parentWriter = new JDOMWriter(parentElement);
    ElementsWriter.writeElements(Arrays.asList(new Object[] {"item1", "item2", "item3"}), itemName, parentWriter, typeMapping, new MessageContext());
    childElements = parentElement.getChildren();
    assertEquals(3, childElements.size());
    for (int i = 0; i < childElements.size(); i++) {
      Element childElement = (Element) childElements.get(i);
      assertEquals("item", childElement.getName());
      assertEquals("urn:TestElementsWriter", childElement.getNamespaceURI());
      assertEquals(String.format("item%s", i + 1), childElement.getText());
    }
  }
}
