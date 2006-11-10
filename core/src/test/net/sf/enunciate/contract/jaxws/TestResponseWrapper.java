package net.sf.enunciate.contract.jaxws;

import net.sf.enunciate.InAPTTestCase;

import javax.xml.namespace.QName;
import java.util.Collection;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestResponseWrapper extends InAPTTestCase {

  /**
   * tests the names and properties of the request wrapper.
   */
  public void testNamesAndProperties() throws Exception {
    EndpointInterface ei = new EndpointInterface(getDeclaration("net.sf.enunciate.samples.services.ResponseWrapperExamples"));
    WebMethod fullyAnnotated = null;
    WebMethod defaultAnnotated = null;
    WebMethod withInOut = null;
    for (WebMethod webMethod : ei.getWebMethods()) {
      if ("fullyAnnotated".equals(webMethod.getSimpleName())) {
        fullyAnnotated = webMethod;
      }
      else if ("defaultAnnotated".equals(webMethod.getSimpleName())) {
        defaultAnnotated = webMethod;
      }
      else if ("withInOut".equals(webMethod.getSimpleName())) {
        withInOut = webMethod;
      }
    }

    ResponseWrapper wrapper = new ResponseWrapper(fullyAnnotated);
    assertEquals("net.sf.enunciate.samples.services.FullyAnnotatedMethod", wrapper.getResponseBeanName());
    assertEquals("fully-annotated", wrapper.getElementName());
    assertEquals("urn:fully-annotated", wrapper.getElementNamespace());
    assertTrue(wrapper.isImplicitSchemaElement());
    assertEquals(WebMessagePart.ParticleType.ELEMENT, wrapper.getParticleType());
    assertEquals(new QName("urn:fully-annotated", "fully-annotated"), wrapper.getParticleQName());
    assertNull("A response wrapper should always be anonymous.", wrapper.getTypeQName());
    Collection<ImplicitChildElement> implicitChildElements = wrapper.getChildElements();
    assertEquals(1, implicitChildElements.size());
    assertTrue(implicitChildElements.contains(fullyAnnotated.getWebResult()));
    assertFalse(wrapper.isInput());
    assertTrue(wrapper.isOutput());
    assertFalse(wrapper.isHeader());
    assertFalse(wrapper.isFault());
    Collection<WebMessagePart> parts = wrapper.getParts();
    assertEquals(1, parts.size());
    assertSame(wrapper, parts.iterator().next());
    assertEquals(ei.getSimpleName() + ".fullyAnnotatedResponse", wrapper.getMessageName());
    assertEquals("fullyAnnotatedResponse", wrapper.getPartName());

    wrapper = new ResponseWrapper(defaultAnnotated);
    assertEquals(ei.getPackage().getQualifiedName() + ".jaxws.DefaultAnnotatedResponse", wrapper.getResponseBeanName());
    assertEquals("defaultAnnotatedResponse", wrapper.getElementName());
    assertEquals(ei.getTargetNamespace(), wrapper.getElementNamespace());
    assertTrue(wrapper.isImplicitSchemaElement());
    assertEquals(WebMessagePart.ParticleType.ELEMENT, wrapper.getParticleType());
    assertEquals(new QName(ei.getTargetNamespace() , "defaultAnnotatedResponse"), wrapper.getParticleQName());
    assertNull("A response wrapper should always be anonymous.", wrapper.getTypeQName());
    implicitChildElements = wrapper.getChildElements();
    assertEquals(1, implicitChildElements.size());
    assertTrue(implicitChildElements.contains(defaultAnnotated.getWebResult()));
    assertFalse(wrapper.isInput());
    assertTrue(wrapper.isOutput());
    assertFalse(wrapper.isHeader());
    assertFalse(wrapper.isFault());
    parts = wrapper.getParts();
    assertEquals(1, parts.size());
    assertSame(wrapper, parts.iterator().next());
    assertEquals(ei.getSimpleName() + ".defaultAnnotatedResponse", wrapper.getMessageName());
    assertEquals("defaultAnnotatedResponse", wrapper.getPartName());

    wrapper = new ResponseWrapper(withInOut);
    assertEquals(ei.getPackage().getQualifiedName() + ".jaxws.WithInOutResponse", wrapper.getResponseBeanName());
    assertEquals("withInOutResponse", wrapper.getElementName());
    assertEquals(ei.getTargetNamespace(), wrapper.getElementNamespace());
    assertTrue(wrapper.isImplicitSchemaElement());
    assertEquals(WebMessagePart.ParticleType.ELEMENT, wrapper.getParticleType());
    assertEquals(new QName(ei.getTargetNamespace() , "withInOutResponse"), wrapper.getParticleQName());
    assertNull("A response wrapper should always be anonymous.", wrapper.getTypeQName());
    implicitChildElements = wrapper.getChildElements();
    assertEquals(2, implicitChildElements.size());
    assertTrue(implicitChildElements.contains(withInOut.getWebResult()));
    assertTrue("The implicit child elements should include the first web parameter (expected to be an IN/OUT parameter).",
               implicitChildElements.contains(withInOut.getWebParameters().iterator().next()));
    assertFalse(wrapper.isInput());
    assertTrue(wrapper.isOutput());
    assertFalse(wrapper.isHeader());
    assertFalse(wrapper.isFault());
    parts = wrapper.getParts();
    assertEquals(1, parts.size());
    assertSame(wrapper, parts.iterator().next());
    assertEquals(ei.getSimpleName() + ".withInOutResponse", wrapper.getMessageName());
    assertEquals("withInOutResponse", wrapper.getPartName());

  }

  public static Test suite() {
    return createSuite(TestResponseWrapper.class);
  }
}
