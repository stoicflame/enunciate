package net.sf.enunciate.modules.xfire_client;

import net.sf.enunciate.InAPTTestCase;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;

/**
 * @author Ryan Heaton
 */
public class TestXFireClientValidator extends InAPTTestCase {

  /**
   * Tests that an EI with an RPC-style web method is not valid.
   */
  public void testValidaiteRPCEI() throws Exception {
    XFireClientValidator clientValidator = new XFireClientValidator();
    TypeDeclaration declaration = getDeclaration("net.sf.enunciate.samples.xfire_client.RPCStyleEI");
    EndpointInterface ei = new EndpointInterface(declaration);
    assertTrue("RPC-style web services aren't supporeted yet, are they?", clientValidator.validateEndpointInterface(ei).hasErrors());
  }

  /**
   * Tests that we don't support an XML list of IDREFs yet.
   */
  public void testValidateXMLListofIDREFs() throws Exception {
    XFireClientValidator validator = new XFireClientValidator();
    ClassDeclaration declaration = (ClassDeclaration) getDeclaration("net.sf.enunciate.samples.xfire_client.InvalidComplexType");
    assertTrue("XmlLists of IDREFs shouldn't be supported yet.", validator.validateComplexType(new ComplexTypeDefinition(declaration)).hasErrors());
  }
}
