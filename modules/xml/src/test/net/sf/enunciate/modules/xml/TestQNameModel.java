package net.sf.enunciate.modules.xml;

import junit.framework.TestCase;

import javax.xml.namespace.QName;

import freemarker.ext.beans.BeansWrapper;

/**
 * @author Ryan Heaton
 */
public class TestQNameModel extends TestCase {

  /**
   * Tests the string representation of the qname model.
   */
  public void testGetAsString() throws Exception {
    QNameModel model = new QNameModel(new QName("urn:testGetAsString", "element"), new BeansWrapper()) {

      @Override
      protected String lookupPrefix(String namespace) {
        if ("urn:testGetAsString".equals(namespace)) {
          return "tgas";
        }

        throw new IllegalArgumentException();
      }
    };

    assertEquals("tgas:element", model.getAsString());

  }

}
