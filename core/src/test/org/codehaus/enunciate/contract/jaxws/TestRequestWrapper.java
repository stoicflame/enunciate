package org.codehaus.enunciate.contract.jaxws;

import org.codehaus.enunciate.InAPTTestCase;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestRequestWrapper extends InAPTTestCase {

  /**
   * tests getting the names and properties of a request wrapper.
   */
  public void testNamesAndProperties() throws Exception {
    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.services.RequestWrapperExamples"));
    WebMethod fullyAnnotated = null;
    WebMethod defaultAnnotated = null;
    WebMethod withHeader = null;
    for (WebMethod webMethod : ei.getWebMethods()) {
      if ("fullyAnnotated".equals(webMethod.getSimpleName())) {
        fullyAnnotated = webMethod;
      }
      else if ("defaultAnnotated".equals(webMethod.getSimpleName())) {
        defaultAnnotated = webMethod;
      }
      else if ("withHeader".equals(webMethod.getSimpleName())) {
        withHeader = webMethod;
      }
    }

    RequestWrapper defaultAnnotatedWrapper = new RequestWrapper(defaultAnnotated);
    assertEquals("defaultAnnotated", defaultAnnotatedWrapper.getElementName());
    assertEquals(ei.getTargetNamespace(), defaultAnnotatedWrapper.getElementNamespace());
    assertEquals(ei.getPackage().getQualifiedName() + ".jaxws.DefaultAnnotated", defaultAnnotatedWrapper.getRequestBeanName());
    assertTrue(defaultAnnotatedWrapper.isImplicitSchemaElement());
    assertEquals(WebMessagePart.ParticleType.ELEMENT, defaultAnnotatedWrapper.getParticleType());
    assertEquals(new QName(ei.getTargetNamespace(), "defaultAnnotated"), defaultAnnotatedWrapper.getParticleQName());
    assertNull("A request wrapper should always be anonymous.", defaultAnnotatedWrapper.getTypeQName());
    Collection<ImplicitChildElement> implicitChildElements = defaultAnnotatedWrapper.getChildElements();
    assertEquals(2, implicitChildElements.size());
    Iterator<WebParam> paramIt = defaultAnnotated.getWebParameters().iterator();
    assertTrue(implicitChildElements.contains(paramIt.next()));
    assertTrue(implicitChildElements.contains(paramIt.next()));
    assertTrue(defaultAnnotatedWrapper.isInput());
    assertFalse(defaultAnnotatedWrapper.isOutput());
    assertFalse(defaultAnnotatedWrapper.isHeader());
    assertFalse(defaultAnnotatedWrapper.isFault());
    Collection<WebMessagePart> parts = defaultAnnotatedWrapper.getParts();
    assertEquals(1, parts.size());
    assertSame(defaultAnnotatedWrapper, parts.iterator().next());
    assertEquals(ei.getSimpleName() + ".defaultAnnotated", defaultAnnotatedWrapper.getMessageName());
    assertEquals("defaultAnnotated", defaultAnnotatedWrapper.getPartName());

    RequestWrapper fullyAnnotatedWrapper = new RequestWrapper(fullyAnnotated);
    assertEquals("fully-annotated", fullyAnnotatedWrapper.getElementName());
    assertEquals("urn:fully-annotated", fullyAnnotatedWrapper.getElementNamespace());
    assertEquals("org.codehaus.enunciate.samples.services.FullyAnnotatedMethod", fullyAnnotatedWrapper.getRequestBeanName());
    assertTrue(fullyAnnotatedWrapper.isImplicitSchemaElement());
    assertEquals(WebMessagePart.ParticleType.ELEMENT, fullyAnnotatedWrapper.getParticleType());
    assertEquals(new QName("urn:fully-annotated", "fully-annotated"), fullyAnnotatedWrapper.getParticleQName());
    assertNull("A request wrapper should always be anonymous.", fullyAnnotatedWrapper.getTypeQName());
    implicitChildElements = fullyAnnotatedWrapper.getChildElements();
    assertEquals(2, implicitChildElements.size());
    paramIt = fullyAnnotated.getWebParameters().iterator();
    assertTrue(implicitChildElements.contains(paramIt.next()));
    assertTrue(implicitChildElements.contains(paramIt.next()));
    assertTrue(fullyAnnotatedWrapper.isInput());
    assertFalse(fullyAnnotatedWrapper.isOutput());
    assertFalse(fullyAnnotatedWrapper.isHeader());
    assertFalse(fullyAnnotatedWrapper.isFault());
    parts = fullyAnnotatedWrapper.getParts();
    assertEquals(1, parts.size());
    assertSame(fullyAnnotatedWrapper, parts.iterator().next());
    assertEquals(ei.getSimpleName() + ".fullyAnnotated", fullyAnnotatedWrapper.getMessageName());
    assertEquals("fullyAnnotated", fullyAnnotatedWrapper.getPartName());

    RequestWrapper withHeaderWrapper = new RequestWrapper(withHeader);
    assertEquals("withHeader", withHeaderWrapper.getElementName());
    assertEquals(ei.getTargetNamespace(), withHeaderWrapper.getElementNamespace());
    assertEquals(ei.getPackage().getQualifiedName() + ".jaxws.WithHeader", withHeaderWrapper.getRequestBeanName());
    assertTrue(withHeaderWrapper.isImplicitSchemaElement());
    assertEquals(WebMessagePart.ParticleType.ELEMENT, withHeaderWrapper.getParticleType());
    assertEquals(new QName(ei.getTargetNamespace(), "withHeader"), withHeaderWrapper.getParticleQName());
    assertNull("A request wrapper should always be anonymous.", withHeaderWrapper.getTypeQName());
    implicitChildElements = withHeaderWrapper.getChildElements();
    assertEquals(1, implicitChildElements.size());
    paramIt = withHeader.getWebParameters().iterator();
    assertFalse(implicitChildElements.contains(paramIt.next()));
    assertTrue(implicitChildElements.contains(paramIt.next()));
    assertTrue(withHeaderWrapper.isInput());
    assertFalse(withHeaderWrapper.isOutput());
    assertFalse(withHeaderWrapper.isHeader());
    assertFalse(withHeaderWrapper.isFault());
    parts = withHeaderWrapper.getParts();
    assertEquals(1, parts.size());
    assertSame(withHeaderWrapper, parts.iterator().next());
    assertEquals(ei.getSimpleName() + ".withHeader", withHeaderWrapper.getMessageName());
    assertEquals("withHeader", withHeaderWrapper.getPartName());
  }

  public static Test suite() {
    return createSuite(TestRequestWrapper.class);
  }
}
