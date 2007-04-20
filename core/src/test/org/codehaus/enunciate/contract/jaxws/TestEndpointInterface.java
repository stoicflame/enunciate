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

import com.sun.mirror.declaration.TypeDeclaration;
import junit.framework.Test;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.InAPTTestCase;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.validation.ValidationException;

import java.util.Collection;
import java.util.Iterator;

/**
 * Test suite for the web service decoration.
 *
 * @author Ryan Heaton
 */
public class TestEndpointInterface extends InAPTTestCase {

  public void testTargetNamespace() throws Exception {
    TypeDeclaration declaration = getDeclaration("org.codehaus.enunciate.samples.services.NoNamespaceWebService");
    assertEquals("calculated namespace doesn't conform to JSR 181: 3.2", "http://services.samples.enunciate.codehaus.org/", new EndpointInterface(declaration).getTargetNamespace()
    );

    declaration = getDeclaration("org.codehaus.enunciate.samples.services.NamespacedWebService");
    assertEquals(new EndpointInterface(declaration).getTargetNamespace(), "http://enunciate.codehaus.org/samples/contract");

    declaration = getDeclaration("NoPackageWebService");
    try {
      new EndpointInterface(declaration).calculateNamespaceURI();
      fail("Shouldn't have been able to calculate the namespace URI.");
    }
    catch (ValidationException e) {
      //fall through.
    }
  }

  public void testGetWebMethods() throws Exception {
    TypeDeclaration declaration = getDeclaration("org.codehaus.enunciate.samples.services.NamespacedWebService");
    Collection<WebMethod> webMethods = new EndpointInterface(declaration).getWebMethods();
    assertEquals(1, webMethods.size());
    assertEquals("myPublicMethod", webMethods.iterator().next().getSimpleName());

    declaration = getDeclaration("org.codehaus.enunciate.samples.services.NoNamespaceWebService");
    webMethods = new EndpointInterface(declaration).getWebMethods();
    assertEquals(2, webMethods.size());
    Iterator<WebMethod> it = webMethods.iterator();
    WebMethod first = it.next();
    WebMethod second = it.next();
    assertTrue("myImplicitlyPublicMethod".equals(first.getSimpleName()) || "myExplicitlyPublicMethod".equals(first.getSimpleName()));
    assertTrue("myImplicitlyPublicMethod".equals(second.getSimpleName()) || "myExplicitlyPublicMethod".equals(second.getSimpleName()));

    declaration = getDeclaration("org.codehaus.enunciate.samples.services.SuperNoNamespaceWebServiceImpl");
    webMethods = new EndpointInterface(declaration).getWebMethods();
    assertEquals(4, webMethods.size());
  }

  /**
   * Tests the attributes of an ei.
   */
  public void testAttributes() throws Exception {
    TypeDeclaration declaration = getDeclaration("org.codehaus.enunciate.samples.services.NamespacedWebService");
    EndpointInterface annotated = new EndpointInterface(declaration);
    declaration = getDeclaration("org.codehaus.enunciate.samples.services.NoNamespaceWebService");
    EndpointInterface notAnnotated = new EndpointInterface(declaration);

    assertEquals("The port type name of the web service should be customized by the annotation (JSR 181: 3.4)", "annotated-web-service", annotated.getPortTypeName());
    assertEquals("The port type name of the web service should be the simple name if not annotated (JSR 181: 3.4)", "NoNamespaceWebService", notAnnotated.getPortTypeName());
  }

  /**
   * tests the list of endpoint implementations for this ei.
   */
  public void testEndpointImplementations() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);

    EndpointInterface ei = new EndpointInterface(getDeclaration("org.codehaus.enunciate.samples.services.NoNamespaceWebService"));
    Collection<EndpointImplementation> impls = ei.getEndpointImplementations();
    assertEquals(3, impls.size());
    for (EndpointImplementation impl : impls) {
      String fqn = impl.getQualifiedName();
      assertTrue(fqn, "org.codehaus.enunciate.samples.services.NoNamespaceWebServiceImpl2".equals(fqn)
        || "org.codehaus.enunciate.samples.services.NoNamespaceWebServiceImpl".equals(fqn)
        || "org.codehaus.enunciate.samples.services.InvalidEIReference".equals(fqn));
    }
  }

  public static Test suite() {
    return createSuite(TestEndpointInterface.class);
  }

}
