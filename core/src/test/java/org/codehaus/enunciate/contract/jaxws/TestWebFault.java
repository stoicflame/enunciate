/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.contract.jaxws;

import com.sun.mirror.declaration.ClassDeclaration;
import junit.framework.Test;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.jaxb.ImplicitChildElement;
import org.codehaus.enunciate.contract.validation.ValidationException;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

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
    assertNull(webFault.getExplicitFaultBeanType());
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
    assertFalse(webFault.isOutput());
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
    assertNull(webFault.getExplicitFaultBeanType());
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
    assertFalse(webFault.isOutput());
    assertFalse(webFault.isHeader());
    assertTrue(webFault.isFault());
  }

  /**
   * tests the explicit web fault.
   */
  public void testExplicitWebFault() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    model.add(new RootElementDeclaration((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanThree"), null));
    FreemarkerModel.set(model);
    WebFault webFault = new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.ExplicitFaultBean"));
    assertNotNull(webFault.getExplicitFaultBeanType());
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
    assertFalse(webFault.isOutput());
    assertFalse(webFault.isHeader());
    assertTrue(webFault.isFault());
  }

// todo: look into whether we should really be this strict on the validation...
//  /**
//   * tests that a web fault without the right contructor signature doesn't have an explicit bean.
//   */
//  public void testAlmostExplicitWebFault1() throws Exception {
//    FreemarkerModel.set(new EnunciateFreemarkerModel());
//    WebFault webFault = new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.AlmostExplicitFaultBeanOne"));
//    assertNull("A web fault should not have an explicit fault bean if the constructor doesn't have Throwable in its signature. (Exception isn't enough).",
//               webFault.getExplicitFaultBean());
//  }

  /**
   * tests that a web fault without both construtors doesn't have an explicit bean.
   */
  public void testAlmostExplicitWebFault2() throws Exception {
    FreemarkerModel.set(new EnunciateFreemarkerModel());
    try {
      new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.AlmostExplicitFaultBeanTwo"));
      fail("A web fault without both constructors shouldn't have an explicit fault bean.");
    }
    catch (ValidationException e) {
      //fall through...
    }
  }

  /**
   * tests that a web fault with one of the constructors public doesn't have an explicit fault bean.
   */
  public void testAlmostExplicitWebFault3() throws Exception {
    FreemarkerModel.set(new EnunciateFreemarkerModel());
    try {
      new WebFault((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.services.AlmostExplicitFaultBeanThree"));
      fail("A web fault without both PUBLIC constructors shouldn't have an explicit fault bean.");
    }
    catch (ValidationException e) {
      //fall through...
    }
  }

  public static Test suite() {
    return createSuite(TestWebFault.class);
  }
}
