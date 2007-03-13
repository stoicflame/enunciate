package org.codehaus.enunciate.modules.xfire;

import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.validation.ValidationResult;

/**
 * @author Ryan Heaton
 */
public class TestXFireValidator extends InAPTTestCase {

  /**
   * Tests duplicate-named services.
   */
  public void testDuplicateNamedServices() throws Exception {
    XFireValidator validator = new XFireValidator();
    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.modules.xfire.SimpleEI"));
    ValidationResult result = validator.validateEndpointInterface(ei);
    assertFalse(result.hasErrors());
    result = validator.validateEndpointInterface(ei);
    assertTrue(result.hasErrors());
    String errorText = result.getErrors().get(0).getText();
    EndpointInterface ei2 = new EndpointInterface(getDeclaration("org.codehaus.modules.xfire.SimpleEIDifferentNS"));
    result = validator.validateEndpointInterface(ei2);
    assertTrue(result.hasErrors());
    assertFalse("should have been a different error for same name, different ns.", errorText.equals(result.getErrors().get(0).getText()));
  }
}
