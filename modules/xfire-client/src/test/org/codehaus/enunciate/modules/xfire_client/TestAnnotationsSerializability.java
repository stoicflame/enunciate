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

package org.codehaus.enunciate.modules.xfire_client;

import junit.framework.TestCase;
import org.codehaus.enunciate.modules.xfire_client.annotations.RequestWrapperAnnotation;
import org.codehaus.enunciate.modules.xfire_client.annotations.SerializableWebServiceAnnotation;
import org.codehaus.xfire.annotations.WebServiceAnnotation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;

/**
 * Just a simple class to test the serializability of the classes.
 *
 * @author Ryan Heaton
 */
public class TestAnnotationsSerializability extends TestCase {

  /**
   * Make sure all things are serialized and deserialized correctly.
   */
  public void testGoodEnough() throws Exception {
    ExplicitWebAnnotations annotations = new ExplicitWebAnnotations();
    annotations.method2RequestWrapper.put(getClass().getName() + ".testGoodEnough", new RequestWrapperAnnotation("req", "urn:req", "com.nothing.Test"));
    WebServiceAnnotation wsAnn = new SerializableWebServiceAnnotation();
    wsAnn.setEndpointInterface("ei");
    wsAnn.setName("ei.name");
    wsAnn.setPortName("ei.portName");
    wsAnn.setServiceName("ei.sn");
    wsAnn.setTargetNamespace("urn:ei.tn");
    wsAnn.setWsdlLocation("wsdlLocation");
    annotations.class2WebService.put(getClass().getName(), wsAnn);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    annotations.writeTo(out);
    out.close();
    annotations = ExplicitWebAnnotations.readFrom(new ByteArrayInputStream(out.toByteArray()));
    wsAnn = annotations.getWebServiceAnnotation(getClass());
    Method method = getClass().getMethod(getName());
    RequestWrapperAnnotation wrapperAnn = annotations.getRequestWrapperAnnotation(method);

    assertNotNull(wsAnn);
    assertNotNull(wrapperAnn);
    assertEquals("ei", wsAnn.getEndpointInterface());
    assertEquals("ei.name", wsAnn.getName());
    assertEquals("ei.portName", wsAnn.getPortName());
    assertEquals("ei.sn", wsAnn.getServiceName());
    assertEquals("urn:ei.tn", wsAnn.getTargetNamespace());
    assertEquals("wsdlLocation", wsAnn.getWsdlLocation());
    assertEquals("req", wrapperAnn.localName());
    assertEquals("urn:req", wrapperAnn.targetNamespace());
    assertEquals("com.nothing.Test", wrapperAnn.className());
  }
}
