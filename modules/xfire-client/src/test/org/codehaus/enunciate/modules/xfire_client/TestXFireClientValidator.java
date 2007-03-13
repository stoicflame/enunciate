package org.codehaus.enunciate.modules.xfire_client;

import com.sun.mirror.declaration.ClassDeclaration;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;

/**
 * @author Ryan Heaton
 */
public class TestXFireClientValidator extends InAPTTestCase {

  /**
   * Tests that we don't support an XML list of IDREFs yet.
   */
  public void testValidateXMLListofIDREFs() throws Exception {
    XFireClientValidator validator = new XFireClientValidator();
    ClassDeclaration declaration = (ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.xfire_client.InvalidComplexType");
    assertTrue("XmlLists of IDREFs shouldn't be supported yet.", validator.validateComplexType(new ComplexTypeDefinition(declaration)).hasErrors());
  }
}
