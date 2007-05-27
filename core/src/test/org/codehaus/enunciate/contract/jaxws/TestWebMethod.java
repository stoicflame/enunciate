/*
 * Copyright 2006 Web Cohesion
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

import javax.jws.soap.SOAPBinding;
import java.util.Collection;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class TestWebMethod extends InAPTTestCase {

  public void testName() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanOne")));
    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanTwo")));
    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("org.codehaus.enunciate.samples.schema.BeanThree")));
    FreemarkerModel.set(model);
    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.services.WebMethodExamples"));

    WebMethod specialNameMethod = null;
    WebMethod docBareVoidMethod = null;
    WebMethod docLitBareMethod = null;
    WebMethod docLitWrappedMethod = null;
    WebMethod rpcLitWrappedMethod = null;
    Collection<WebMethod> webMethods = ei.getWebMethods();
    for (WebMethod webMethod : webMethods) {
      String simpleName = webMethod.getSimpleName();
      if ("specialNameMethod".equals(simpleName)) {
        specialNameMethod = webMethod;
      }
      else if ("docBareVoidMethod".equals(simpleName)) {
        docBareVoidMethod = webMethod;
      }
      else if ("docLitBareMethod".equals(simpleName)) {
        docLitBareMethod = webMethod;
      }
      else if ("docLitWrappedMethod".equals(simpleName)) {
        docLitWrappedMethod = webMethod;
      }
      else if ("rpcLitWrappedMethod".equals(simpleName)) {
        rpcLitWrappedMethod = webMethod;
      }
    }

    assertNotNull(specialNameMethod);
    assertEquals("The operation name should be able to be customized with the annotation.", "special-operation-name", specialNameMethod.getOperationName());
    assertNotNull(specialNameMethod.getWebResult());
    assertEquals(0, specialNameMethod.getWebParameters().size());
    assertEquals(0, specialNameMethod.getWebFaults().size());
    assertEquals(2, specialNameMethod.getMessages().size());
    Set<String> referencedNamespaces = specialNameMethod.getReferencedNamespaces();
    assertTrue(referencedNamespaces.remove("urn:web-method-examples"));
    assertTrue(referencedNamespaces.isEmpty());
    assertEquals("urn:specialNameMethod", specialNameMethod.getAction());
    assertFalse(specialNameMethod.isOneWay());
    assertEquals(SOAPBinding.Style.DOCUMENT, specialNameMethod.getSoapBindingStyle());
    assertEquals(SOAPBinding.Use.LITERAL, specialNameMethod.getSoapUse());
    assertEquals(SOAPBinding.ParameterStyle.WRAPPED, specialNameMethod.getSoapParameterStyle());
    assertTrue(specialNameMethod.isDocLitWrapped());

    assertNotNull(docBareVoidMethod);
    assertEquals("The operation name should default to the simple name.", "docBareVoidMethod", docBareVoidMethod.getOperationName());
    assertNotNull(docBareVoidMethod.getWebResult());
    assertEquals(0, docBareVoidMethod.getWebParameters().size());
    assertEquals(0, docBareVoidMethod.getWebFaults().size());
    assertEquals(0, docBareVoidMethod.getMessages().size());
    referencedNamespaces = docBareVoidMethod.getReferencedNamespaces();
    assertTrue(referencedNamespaces.isEmpty());
    assertEquals("", docBareVoidMethod.getAction());
    assertFalse(docBareVoidMethod.isOneWay());
    assertEquals(SOAPBinding.Style.DOCUMENT, docBareVoidMethod.getSoapBindingStyle());
    assertEquals(SOAPBinding.Use.LITERAL, docBareVoidMethod.getSoapUse());
    assertEquals(SOAPBinding.ParameterStyle.BARE, docBareVoidMethod.getSoapParameterStyle());
    assertFalse(docBareVoidMethod.isDocLitWrapped());

    assertNotNull(docLitWrappedMethod);
    assertEquals("The operation name should default to the simple name.", "docLitWrappedMethod", docLitWrappedMethod.getOperationName());
    assertNotNull(docLitWrappedMethod.getWebResult());
    assertEquals(4, docLitWrappedMethod.getWebParameters().size());
    assertEquals(2, docLitWrappedMethod.getWebFaults().size());
    assertEquals("There should be 6 web messages: 1 for in, 1 for out, 2 for faults, 2 for header.", 6, docLitWrappedMethod.getMessages().size());
    referencedNamespaces = docLitWrappedMethod.getReferencedNamespaces();
    assertTrue(referencedNamespaces.remove("urn:web-method-examples"));
    assertTrue(referencedNamespaces.remove("")); //empty namespace for the return type header
    assertTrue(referencedNamespaces.isEmpty());
    assertEquals("", docLitWrappedMethod.getAction());
    assertFalse(docLitWrappedMethod.isOneWay());
    assertEquals(SOAPBinding.Style.DOCUMENT, docLitWrappedMethod.getSoapBindingStyle());
    assertEquals(SOAPBinding.Use.LITERAL, docLitWrappedMethod.getSoapUse());
    assertEquals(SOAPBinding.ParameterStyle.WRAPPED, docLitWrappedMethod.getSoapParameterStyle());
    assertTrue(docLitWrappedMethod.isDocLitWrapped());

    assertNotNull(docLitBareMethod);
    assertEquals("The operation name should default to the simple name.", "docLitBareMethod", docLitBareMethod.getOperationName());
    assertNotNull(docLitBareMethod.getWebResult());
    assertEquals(2, docLitBareMethod.getWebParameters().size());
    assertEquals(2, docLitBareMethod.getWebFaults().size());
    assertEquals("There should be 5 web messages: 1 for in, 1 for out, 2 for faults, 1 for header.", 5, docLitBareMethod.getMessages().size());
    referencedNamespaces = docLitBareMethod.getReferencedNamespaces();
    assertTrue(referencedNamespaces.remove("urn:web-method-examples"));
    assertTrue(referencedNamespaces.isEmpty());
    assertEquals("", docLitBareMethod.getAction());
    assertFalse(docLitBareMethod.isOneWay());
    assertEquals(SOAPBinding.Style.DOCUMENT, docLitBareMethod.getSoapBindingStyle());
    assertEquals(SOAPBinding.Use.LITERAL, docLitBareMethod.getSoapUse());
    assertEquals(SOAPBinding.ParameterStyle.BARE, docLitBareMethod.getSoapParameterStyle());
    assertFalse(docLitBareMethod.isDocLitWrapped());

    assertNotNull(rpcLitWrappedMethod);
    assertEquals("The operation name should default to the simple name.", "rpcLitWrappedMethod", rpcLitWrappedMethod.getOperationName());
    assertNotNull(rpcLitWrappedMethod.getWebResult());
    assertEquals(4, rpcLitWrappedMethod.getWebParameters().size());
    assertEquals(2, rpcLitWrappedMethod.getWebFaults().size());
    assertEquals("There should be 5 web messages: 1 for in, 1 for out, 2 for faults, 1 for header.", 5, rpcLitWrappedMethod.getMessages().size());
    referencedNamespaces = rpcLitWrappedMethod.getReferencedNamespaces();
    assertTrue(referencedNamespaces.remove("urn:web-method-examples"));
    assertTrue(referencedNamespaces.remove("http://www.w3.org/2001/XMLSchema"));
    assertTrue(referencedNamespaces.remove(""));
    assertTrue(referencedNamespaces.isEmpty());
    assertEquals("", rpcLitWrappedMethod.getAction());
    assertFalse(rpcLitWrappedMethod.isOneWay());
    assertEquals(SOAPBinding.Style.RPC, rpcLitWrappedMethod.getSoapBindingStyle());
    assertEquals(SOAPBinding.Use.LITERAL, rpcLitWrappedMethod.getSoapUse());
    assertEquals(SOAPBinding.ParameterStyle.WRAPPED, rpcLitWrappedMethod.getSoapParameterStyle());
    assertFalse(rpcLitWrappedMethod.isDocLitWrapped());
  }

  public static Test suite() {
    return createSuite(TestWebMethod.class);
  }

}
