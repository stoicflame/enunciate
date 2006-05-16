package net.sf.enunciate.decorations.jaxws;

import static org.testng.Assert.*;

import org.testng.annotations.Test;
import net.sf.enunciate.decorations.EnunciateDecorationTestCase;
import com.sun.mirror.declaration.TypeDeclaration;

/**
 * Test suite for the web service decoration.
 *
 * @author Ryan Heaton
 */
public class TestWebService extends EnunciateDecorationTestCase {

  /**
   * Tests the calculation of the target namespace.
   */
  @Test (
    groups = {"jsr181"}
  )
  public void testTargetNamespace() throws Exception {
    TypeDeclaration declaration = this.env.getTypeDeclaration("net.sf.enunciate.samples.services.NoNamespaceWebService");
    assertNotNull(declaration, "No source def found: net.sf.enunciate.samples.services.NoNamespaceWebService");
    assertTrue(WebService.isWebService(declaration));
    assertEquals("http://services.samples.enunciate.sf.net/", new WebService(declaration).getTargetNamespace());
  }

}
