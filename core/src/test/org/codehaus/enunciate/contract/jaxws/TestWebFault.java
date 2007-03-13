package org.codehaus.enunciate.contract.jaxws;

import com.sun.mirror.declaration.ClassDeclaration;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.HashSet;
import java.util.Arrays;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestWebFault extends InAPTTestCase {

  /**
   * Tests the default implicit web fault.
   */
  public void testDefaultImplicitWebFault() throws Exception {
    FreemarkerModel.set(new EnunciateFreemarkerModel());
    WebFault webFault = new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.ImplicitWebFault"));
    assertEquals("ImplicitWebFault", webFault.getMessageName());
    assertEquals("ImplicitWebFault", webFault.getElementName());
    assertEquals("org.codehaus.enunciate.samples.services.jaxws.ImplicitWebFaultBean", webFault.getImplicitFaultBeanQualifiedName());
    assertEquals("http://services.samples.enunciate.codehaus.org/", webFault.getTargetNamespace());
    assertEquals("ImplicitWebFault", webFault.getPartName());
    assertNull(webFault.getExplicitFaultBean());
    assertEquals(WebMessagePart.ParticleType.ELEMENT, webFault.getParticleType());
    assertEquals(new QName("http://services.samples.enunciate.codehaus.org/", "ImplicitWebFault"), webFault.getParticleQName());
    assertNull("The type of a web fault is always implicit.", webFault.getTypeQName());
    assertTrue(webFault.isImplicitSchemaElement());
    Collection<ImplicitChildElement> implicitChildElements = webFault.getChildElements();
    HashSet<String> elements = new HashSet<String>(Arrays.asList("property1", "property2", "property3"));
    for (ImplicitChildElement implicitChildElement : implicitChildElements) {
      if ("property1".equals(implicitChildElement.getElementName())) {
        elements.remove("property1");
        assertEquals("1", implicitChildElement.getMaxOccurs());
        assertEquals(1, implicitChildElement.getMinOccurs());
        assertEquals(KnownXmlType.BOOLEAN.getQname(), implicitChildElement.getTypeQName());
      }
      else if ("property2".equals(implicitChildElement.getElementName())) {
        elements.remove("property2");
        assertEquals("1", implicitChildElement.getMaxOccurs());
        assertEquals(1, implicitChildElement.getMinOccurs());
        assertEquals(KnownXmlType.INT.getQname(), implicitChildElement.getTypeQName());
      }
      else if ("property3".equals(implicitChildElement.getElementName())) {
        elements.remove("property3");
        assertEquals("unbounded", implicitChildElement.getMaxOccurs());
        assertEquals(0, implicitChildElement.getMinOccurs());
        assertEquals(KnownXmlType.STRING.getQname(), implicitChildElement.getTypeQName());
      }
    }
    assertTrue(elements.isEmpty());
    Collection<WebMessagePart> parts = webFault.getParts();
    assertEquals(1, parts.size());
    assertSame(webFault, parts.iterator().next());
    assertFalse(webFault.isInput());
    assertTrue(webFault.isOutput());
    assertFalse(webFault.isHeader());
    assertTrue(webFault.isFault());
  }

  /**
   * Tests the implicit web fault.
   */
  public void testImplicitWebFault() throws Exception {
    FreemarkerModel.set(new EnunciateFreemarkerModel());
    WebFault webFault = new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.ImplicitWebFaultTwo"));
    assertEquals("ImplicitWebFaultTwo", webFault.getMessageName());
    assertEquals("implicit-fault", webFault.getElementName());
    assertEquals("urn:implicit-fault", webFault.getTargetNamespace());
    assertEquals("ImplicitWebFaultTwo", webFault.getPartName());
    assertNull(webFault.getExplicitFaultBean());
    assertEquals(WebMessagePart.ParticleType.ELEMENT, webFault.getParticleType());
    assertEquals(new QName("urn:implicit-fault", "implicit-fault"), webFault.getParticleQName());
    assertNull("The type of a web fault is always implicit.", webFault.getTypeQName());
    assertTrue(webFault.isImplicitSchemaElement());
    Collection<ImplicitChildElement> implicitChildElements = webFault.getChildElements();
    HashSet<String> elements = new HashSet<String>(Arrays.asList("property1", "property2", "property3", "property4"));
    for (ImplicitChildElement implicitChildElement : implicitChildElements) {
      if ("property1".equals(implicitChildElement.getElementName())) {
        elements.remove("property1");
        assertEquals("1", implicitChildElement.getMaxOccurs());
        assertEquals(1, implicitChildElement.getMinOccurs());
        assertEquals(KnownXmlType.BOOLEAN.getQname(), implicitChildElement.getTypeQName());
      }
      else if ("property2".equals(implicitChildElement.getElementName())) {
        elements.remove("property2");
        assertEquals("1", implicitChildElement.getMaxOccurs());
        assertEquals(1, implicitChildElement.getMinOccurs());
        assertEquals(KnownXmlType.INT.getQname(), implicitChildElement.getTypeQName());
      }
      else if ("property3".equals(implicitChildElement.getElementName())) {
        elements.remove("property3");
        assertEquals("unbounded", implicitChildElement.getMaxOccurs());
        assertEquals(0, implicitChildElement.getMinOccurs());
        assertEquals(KnownXmlType.STRING.getQname(), implicitChildElement.getTypeQName());
      }
      else if ("property4".equals(implicitChildElement.getElementName())) {
        elements.remove("property4");
        assertEquals("1", implicitChildElement.getMaxOccurs());
        assertEquals(1, implicitChildElement.getMinOccurs());
        assertEquals(KnownXmlType.DOUBLE.getQname(), implicitChildElement.getTypeQName());
      }
    }
    assertTrue(elements.isEmpty());
    Collection<WebMessagePart> parts = webFault.getParts();
    assertEquals(1, parts.size());
    assertSame(webFault, parts.iterator().next());
    assertFalse(webFault.isInput());
    assertTrue(webFault.isOutput());
    assertFalse(webFault.isHeader());
    assertTrue(webFault.isFault());
  }

  /**
   * tests the explicit web fault.
   */
  public void testExplicitWebFault() throws Exception {
    FreemarkerModel.set(new EnunciateFreemarkerModel());
    WebFault webFault = new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.ExplicitFaultBean"));
    assertNotNull(webFault.getExplicitFaultBean());
    assertEquals("ExplicitFaultBean", webFault.getMessageName());
    assertNull(webFault.getElementName());
    assertNull(webFault.getTargetNamespace());
    assertEquals("ExplicitFaultBean", webFault.getPartName());
    assertEquals(WebMessagePart.ParticleType.ELEMENT, webFault.getParticleType());
    assertEquals(new QName(null, "beanThree"), webFault.getParticleQName());
    assertNull("The type of a web fault is always implicit.", webFault.getTypeQName());
    assertFalse(webFault.isImplicitSchemaElement());
    assertTrue(webFault.getChildElements().isEmpty());
    Collection<WebMessagePart> parts = webFault.getParts();
    assertEquals(1, parts.size());
    assertSame(webFault, parts.iterator().next());
    assertFalse(webFault.isInput());
    assertTrue(webFault.isOutput());
    assertFalse(webFault.isHeader());
    assertTrue(webFault.isFault());
  }
  
  /**
   * tests that a web fault without the right contructor signature doesn't have an explicit bean.
   */
  public void testAlmostExplicitWebFault1() throws Exception {
    FreemarkerModel.set(new EnunciateFreemarkerModel());
    WebFault webFault = new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.AlmostExplicitFaultBeanOne"));
    assertNull("A web fault should not have an explicit fault bean if the constructor doesn't have Throwable in its signature. (Exception isn't enough).",
               webFault.getExplicitFaultBean());
  }

  /**
   * tests that a web fault without both construtors doesn't have an explicit bean.
   */
  public void testAlmostExplicitWebFault2() throws Exception {
    FreemarkerModel.set(new EnunciateFreemarkerModel());
    WebFault webFault = new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.AlmostExplicitFaultBeanTwo"));
    assertNull("A web fault without both constructors shouldn't have an explicit fault bean.",
               webFault.getExplicitFaultBean());
  }

  /**
   * tests that a web fault with one of the constructors public doesn't have an explicit fault bean.
   */
  public void testAlmostExplicitWebFault3() throws Exception {
    FreemarkerModel.set(new EnunciateFreemarkerModel());
    WebFault webFault = new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.AlmostExplicitFaultBeanThree"));
    assertNull("A web fault without both PUBLIC constructors shouldn't have an explicit fault bean.",
               webFault.getExplicitFaultBean());
  }

  public static Test suite() {
    return createSuite(TestWebFault.class);
  }
}
