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

package org.codehaus.enunciate.contract.rest;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import org.codehaus.enunciate.InAPTTestCase;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Ryan Heaton
 */
public class TestRESTContract extends InAPTTestCase {

  /**
   * tests the REST endpoint
   */
  public void testRESTEndpoint() throws Exception {
    TypeDeclaration declaration = getDeclaration("org.codehaus.enunciate.samples.services.RESTEndpointExamples");
    RESTEndpoint endpoint = new RESTEndpoint((ClassDeclaration) declaration);
    ArrayList<RESTMethod> methods = new ArrayList<RESTMethod>(endpoint.getRESTMethods());
    assertEquals(3, methods.size());

    Iterator<RESTMethod> it = methods.iterator();
    while (it.hasNext()) {
      RESTMethod method = it.next();
      if ("getFirstThing".equals(method.getSimpleName())) {
        assertEquals("my/default/context", method.getNoun().getContext());
        assertEquals("getFirstThing", method.getNoun().getName());
        assertEquals("my/default/context/getFirstThing", method.getNoun().toString());
      }
      else if ("getSecondThing".equals(method.getSimpleName())) {
        assertEquals("my/other/context", method.getNoun().getContext());
        assertEquals("second", method.getNoun().getName());
        assertEquals("my/other/context/second", method.getNoun().toString());
      }
      else if ("getThirdThing".equals(method.getSimpleName())) {
        assertEquals("", method.getNoun().getContext());
        assertEquals("third", method.getNoun().getName());
        assertEquals("third", method.getNoun().toString());
      }
      else {
        fail("Unknown REST method: " + method.getSimpleName());
      }

      it.remove();
    }

  }

//  /**
//   * tests the REST method
//   */
//  public void testRESTMethod() throws Exception {
//    //todo: implement.
//  }
//
//  /**
//   * tests the REST parameter
//   */
//  public void testRESTParameter() throws Exception {
//    //todo: implement.
//  }
//
//  /**
//   * tests the REST error
//   */
//  public void testRESTError() throws Exception {
//    //todo: implement.
//  }

}
