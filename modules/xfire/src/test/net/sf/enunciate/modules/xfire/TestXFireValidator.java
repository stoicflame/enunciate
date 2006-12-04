package net.sf.enunciate.modules.xfire;

import net.sf.enunciate.InAPTTestCase;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.validation.ValidationResult;

/**
 * @author Ryan Heaton
 */
public class TestXFireValidator extends InAPTTestCase {

  /**
   * Tests duplicate-named services.
   */
  public void testDuplicateNamedServices() throws Exception {
    XFireValidator validator = new XFireValidator();
    EndpointInterface ei = new EndpointInterface(getDeclaration("net.sf.modules.xfire.SimpleEI"));
    ValidationResult result = validator.validateEndpointInterface(ei);
    assertFalse(result.hasErrors());
    result = validator.validateEndpointInterface(ei);
    assertTrue(result.hasErrors());
    String errorText = result.getErrors().get(0).getText();
    EndpointInterface ei2 = new EndpointInterface(getDeclaration("net.sf.modules.xfire.SimpleEIDifferentNS"));
    result = validator.validateEndpointInterface(ei2);
    assertTrue(result.hasErrors());
    assertFalse("should have been a different error for same name, different ns.", errorText.equals(result.getErrors().get(0).getText()));
  }
}
