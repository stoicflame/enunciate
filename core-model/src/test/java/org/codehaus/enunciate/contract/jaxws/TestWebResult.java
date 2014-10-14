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
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;

import javax.xml.namespace.QName;
import java.util.Collection;

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
    assertEquals(new QName("urn:web-method-examples", "docLitBareMethodResponse"), webResult.getParticleQName());
    assertTrue(webResult.isImplicitSchemaElement());
    assertEquals(1, webResult.getParts().size());
    assertTrue(webResult.getParts().contains(webResult));
    assertEquals(new QName(null, "beanOne"), webResult.getTypeQName());
    assertEquals(0, webResult.getMinOccurs());
    assertEquals("1", webResult.getMaxOccurs());
    assertEquals("docLitBareMethodResponse", webResult.getElementName());

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
    assertEquals(new QName("urn:docLitWrapped", "doc-lit-wrapped-return"), webResult.getParticleQName());
    assertTrue(webResult.isImplicitSchemaElement());
    assertEquals(1, webResult.getParts().size());
    assertEquals(new QName(null, "beanThree"), webResult.getTypeQName());
    assertEquals(0, webResult.getMinOccurs());
    assertEquals("1", webResult.getMaxOccurs());
    assertEquals("doc-lit-wrapped-return", webResult.getElementName());

    webResult = rpcLitWrappedMethod.getWebResult();
    assertEquals("return", webResult.getName());
    assertEquals("", webResult.getTargetNamespace());
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
    assertEquals(0, webResult.getMinOccurs());
    assertEquals("1", webResult.getMaxOccurs());
    assertEquals("return", webResult.getElementName());
  }
  
  public static Test suite() {
    return createSuite(TestWebResult.class);
  }
}
