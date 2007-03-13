package org.codehaus.enunciate.contract.jaxws;

import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import com.sun.mirror.declaration.ClassDeclaration;

import javax.xml.namespace.QName;
import java.util.Collection;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestWebResult extends InAPTTestCase {

  /**
   * the names and properties of a web result.
   */
  public void testNamesAndProperties() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanOne")));
    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanTwo")));
    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanThree")));
    FreemarkerModel.set(model);
    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.services.WebMethodExamples"));

    WebMethod docLitBareMethod = null;
    WebMethod docLitWrappedMethod = null;
    WebMethod rpcLitWrappedMethod = null;
    Collection<WebMethod> webMethods = ei.getWebMethods();
    for (WebMethod webMethod : webMethods) {
      String simpleName = webMethod.getSimpleName();
      if ("docLitBareMethod".equals(simpleName)) {
        docLitBareMethod = webMethod;
      }
      else if ("docLitWrappedMethod".equals(simpleName)) {
        docLitWrappedMethod = webMethod;
      }
      else if ("rpcLitWrappedMethod".equals(simpleName)) {
        rpcLitWrappedMethod = webMethod;
      }
    }

    WebResult webResult = docLitBareMethod.getWebResult();
    assertEquals("return", webResult.getName());
    assertEquals("urn:web-method-examples", webResult.getTargetNamespace());
    assertEquals("return", webResult.getPartName());
    assertEquals("WebMethodExamples.docLitBareMethodResponse", webResult.getMessageName());
    assertFalse(webResult.isInput());
    assertTrue(webResult.isOutput());
    assertFalse(webResult.isHeader());
    assertFalse(webResult.isFault());
    assertNull(webResult.getPartDocs());
    assertEquals(WebMessagePart.ParticleType.ELEMENT, webResult.getParticleType());
    assertEquals(new QName("urn:web-method-examples", "return"), webResult.getParticleQName());
    assertTrue(webResult.isImplicitSchemaElement());
    assertEquals(1, webResult.getParts().size());
    assertTrue(webResult.getParts().contains(webResult));
    assertEquals(new QName(null, "beanOne"), webResult.getTypeQName());
    assertEquals(1, webResult.getMinOccurs());
    assertEquals("1", webResult.getMaxOccurs());
    assertEquals("return", webResult.getElementName());

    webResult = docLitWrappedMethod.getWebResult();
    assertEquals("doc-lit-wrapped-return", webResult.getName());
    assertEquals("urn:docLitWrapped", webResult.getTargetNamespace());
    assertEquals("doc-lit-wrapped-part", webResult.getPartName());
    assertEquals("WebMethodExamples.docLitWrappedMethod.doc-lit-wrapped-return", webResult.getMessageName());
    assertFalse(webResult.isInput());
    assertTrue(webResult.isOutput());
    assertTrue(webResult.isHeader());
    assertFalse(webResult.isFault());
    assertEquals(WebMessagePart.ParticleType.ELEMENT, webResult.getParticleType());
    assertEquals(new QName(null, "beanThree"), webResult.getParticleQName());
    assertFalse(webResult.isImplicitSchemaElement());
    assertEquals(1, webResult.getParts().size());
    assertEquals(new QName(null, "beanThree"), webResult.getTypeQName());
    assertEquals(1, webResult.getMinOccurs());
    assertEquals("1", webResult.getMaxOccurs());
    assertEquals("doc-lit-wrapped-return", webResult.getElementName());

    webResult = rpcLitWrappedMethod.getWebResult();
    assertEquals("return", webResult.getName());
    assertEquals("urn:web-method-examples", webResult.getTargetNamespace());
    assertEquals("return", webResult.getPartName());
    assertNull(webResult.getMessageName());
    assertFalse(webResult.isInput());
    assertTrue(webResult.isOutput());
    assertFalse(webResult.isHeader());
    assertFalse(webResult.isFault());
    assertEquals(WebMessagePart.ParticleType.TYPE, webResult.getParticleType());
    assertEquals(new QName(null, "beanThree"), webResult.getParticleQName());
    assertFalse(webResult.isImplicitSchemaElement());
    try {
      webResult.getParts();
      fail("A web result that isn't pare shouldn't have parts!");
    }
    catch (UnsupportedOperationException e) {
    }
    assertEquals(new QName(null, "beanThree"), webResult.getTypeQName());
    assertEquals(1, webResult.getMinOccurs());
    assertEquals("1", webResult.getMaxOccurs());
    assertEquals("return", webResult.getElementName());
  }
  
  public static Test suite() {
    return createSuite(TestWebResult.class);
  }
}
