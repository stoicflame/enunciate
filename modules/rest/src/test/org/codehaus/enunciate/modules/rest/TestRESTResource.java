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

package org.codehaus.enunciate.modules.rest;

import junit.framework.TestCase;

import java.util.Map;
import java.util.Properties;
import java.lang.reflect.Method;

import org.codehaus.enunciate.rest.annotations.VerbType;

/**
 * @author Ryan Heaton
 */
public class TestRESTResource extends TestCase {

  /**
   * tests getting the values of the context parameters and noun values.
   */
  public void testContextParameterAndProperNounValues() throws Exception {
    RESTResource restResource = new RESTResource("mynoun");
    Map<String,String> values = restResource.getContextParameterAndProperNounValues("/context/subcontext/mynoun/mypropernoun");
    assertEquals(1, values.size());
    assertEquals("mypropernoun", values.get(null));

    values = restResource.getContextParameterAndProperNounValues("/some/nested/weird/context/subcontext/mynoun/mypropernoun");
    assertEquals(1, values.size());
    assertEquals("mypropernoun", values.get(null));

    values = restResource.getContextParameterAndProperNounValues("/context/subcontext/mynoun/");
    assertEquals(1, values.size());
    assertNull(values.get(null));

    values = restResource.getContextParameterAndProperNounValues("/context/subcontext/mynoun");
    assertEquals(1, values.size());
    assertNull(values.get(null));

    try {
      restResource.getContextParameterAndProperNounValues("/context/subcontext/");
      fail("should have thrown an illegal argument exception");
    }
    catch (IllegalArgumentException e) {
      //fall through...
    }

    try {
      restResource.getContextParameterAndProperNounValues("/context/some/really/strange/requst");
      fail("should have thrown an illegal argument exception");
    }
    catch (IllegalArgumentException e) {
      //fall through...
    }

    restResource = new RESTResource("mynoun", "cool/{stuff}/and/{more}");
    values = restResource.getContextParameterAndProperNounValues("/context/subcontext/cool/1234/and/5432/mynoun/9876");
    assertEquals(3, values.size());
    assertEquals("9876", values.get(null));
    assertEquals("1234", values.get("stuff"));
    assertEquals("5432", values.get("more"));

    try {
      restResource.getContextParameterAndProperNounValues("/context/some/really/strange/requst");
      fail("should have thrown an illegal argument exception");
    }
    catch (IllegalArgumentException e) {
      //fall through...
    }

    try {
      restResource.getContextParameterAndProperNounValues("/context/subcontext/cool/and/mynoun/1234");
      fail("should have thrown an illegal argument exception");
    }
    catch (IllegalArgumentException e) {
      //fall through...
    }

//    restResource = new RESTResource("context") {
//
//      @Override
//      public Set<VerbType> getSupportedVerbs() {
//        return new TreeSet(Arrays.asList(VerbType.values()));
//      }
//    };
//    exporter = new RESTResourceXMLExporter(restResource) {
//      @Override
//      protected ModelAndView handleRESTOperation(VerbType verb, HttpServletRequest request, HttpServletResponse response) throws Exception {
//        request.setAttribute("properNoun", properNoun);
//        request.setAttribute("verb", verb);
//        return null;
//      }
//
//
//    };
//    exporter.setApplicationContext(new GenericApplicationContext());
//    reset(request, response);
//    expect(request.getRequestURI()).andReturn("/context/subcontext/context/mypropernoun");
//    expect(request.getContextPath()).andReturn("");
//    expect(request.getMethod()).andReturn("GET");
//    request.setAttribute("properNoun", "subcontext/context/mypropernoun");
//    request.setAttribute("verb", VerbType.read);
//    replay(request, response);
//    exporter.handleRequestInternal(request, response);
//    verify(request, response);
//
//    reset(request, response);
//    expect(request.getRequestURI()).andReturn("/context/subcontext/context/mypropernoun");
//    expect(request.getContextPath()).andReturn("/context");
//    expect(request.getMethod()).andReturn("GET");
//    request.setAttribute("properNoun", "mypropernoun");
//    request.setAttribute("verb", VerbType.read);
//    replay(request, response);
//    exporter.handleRequestInternal(request, response);
//    verify(request, response);

  }

  /**
   * test addOperation
   */
  public void testAddOperation() throws Exception {
    final Properties props = new Properties();
    props.put("mynoun/read", "one,two,three,four");
    props.put("mycontext/mynoun/read", "five,six,seven,eight");

    RESTResource resource = new RESTResource("mynoun", "") {
      @Override
      protected RESTOperation createOperation(VerbType verb, Object endpoint, Method method, String[] parameterNames) {
        assertEquals(4, parameterNames.length);
        assertEquals("one", parameterNames[0]);
        assertEquals("four", parameterNames[3]);
        props.put("1", "done");
        return null;
      }
    };
    resource.setParamterNames(props);
    resource.addOperation(VerbType.read, null, null);
    assertNotNull(props.get("1"));

    resource = new RESTResource("mynoun", "/mycontext/") {
      @Override
      protected RESTOperation createOperation(VerbType verb, Object endpoint, Method method, String[] parameterNames) {
        assertEquals(4, parameterNames.length);
        assertEquals("five", parameterNames[0]);
        assertEquals("eight", parameterNames[3]);
        props.put("2", "done");
        return null;
      }
    };
    resource.setParamterNames(props);
    resource.addOperation(VerbType.read, null, null);
    assertNotNull(props.get("2"));
  }

}
