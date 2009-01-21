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

import junit.framework.Test;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.contract.jaxb.ImplicitChildElement;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class TestResponseWrapper extends InAPTTestCase {

  /**
   * tests the names and properties of the request wrapper.
   */
  public void testNamesAndProperties() throws Exception {
    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.services.ResponseWrapperExamples"));
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
    assertEquals("org.codehaus.enunciate.samples.services.FullyAnnotatedMethod", wrapper.getResponseBeanName());
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
    assertEquals("parameters", wrapper.getPartName());

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
    assertEquals("parameters", wrapper.getPartName());

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
    assertEquals("parameters", wrapper.getPartName());

  }

  public static Test suite() {
    return createSuite(TestResponseWrapper.class);
  }
}
