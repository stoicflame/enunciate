package org.codehaus.enunciate.modules.xml;

import junit.framework.TestCase;

import java.util.Arrays;

import freemarker.template.TemplateModelException;

/**
 * @author Ryan Heaton
 */
public class TestPrefixMethod extends TestCase {

  /**
   * Tests looking up the namespace.
   */
  public void testLookupNamespace() throws Exception {
    PrefixMethod prefixMethod = new PrefixMethod() {
      @Override
      protected String lookupPrefix(String namespace) {
        if ("urn:testLookupNamespace".equals(namespace)) {
          return "tln";
        }
        else {
          return null;
        }
      }
    };

    assertEquals("tln", prefixMethod.exec(Arrays.asList("urn:testLookupNamespace")));
    try {
      prefixMethod.exec(Arrays.asList("unknown"));
      fail("Should have thrown a template model exception for an unknown namespace.");
    }
    catch (TemplateModelException e) {

    }
  }
}
