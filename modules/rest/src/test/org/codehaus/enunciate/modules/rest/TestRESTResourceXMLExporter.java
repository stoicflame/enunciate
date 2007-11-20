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
import org.codehaus.enunciate.rest.annotations.VerbType;
import static org.easymock.EasyMock.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Ryan Heaton
 */
public class TestRESTResourceXMLExporter extends TestCase {

  /**
   * simple tests for regexps
   */
  public void testRegexp() throws Exception {
    Pattern urlPattern = Pattern.compile("/person/?(.*)$");
    Matcher matcher = urlPattern.matcher("/persons/rest/person/1".substring("/persons".length()));
    if (matcher.find()) {
      assertEquals("1", matcher.group(1));
    }

    Pattern contextParamPattern = Pattern.compile("\\{([^\\}]+)\\}");
    List<String> contextParams = new ArrayList<String>();
    matcher = contextParamPattern.matcher("{context1}/{context2}/something/{special}");
    while (matcher.find()) {
      contextParams.add(matcher.group(1));
    }
    assertEquals(3, contextParams.size());
    assertEquals("context1", contextParams.get(0));
    assertEquals("context2", contextParams.get(1));
    assertEquals("special", contextParams.get(2));

  }

  /**
   * Tests handling that the noun and proper noun are property extracted from the request.
   */
  public void testHandleRequestInternal() throws Exception {
    RESTResource restResource = new RESTResource("mynoun") {
      @Override
      public Set<VerbType> getSupportedVerbs() {
        return EnumSet.allOf(VerbType.class);
      }
    };
    
    RESTResourceXMLExporter exporter = new RESTResourceXMLExporter(restResource) {
      @Override
      protected ModelAndView handleRESTOperation(VerbType verb, HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setAttribute("verb", verb);
        return null;
      }
    };

    exporter.setApplicationContext(new GenericApplicationContext());

    HttpServletRequest request = createMock(HttpServletRequest.class);
    HttpServletResponse response = createMock(HttpServletResponse.class);
    expect(request.getHeader("X-HTTP-Method-Override")).andReturn(null);
    expect(request.getMethod()).andReturn("GET");
    request.setAttribute("verb", VerbType.read);
    replay(request, response);
    exporter.handleRequestInternal(request, response);
    verify(request, response);

    reset(request, response);
    expect(request.getHeader("X-HTTP-Method-Override")).andReturn(null);
    expect(request.getMethod()).andReturn("PUT");
    request.setAttribute("verb", VerbType.create);
    replay(request, response);
    exporter.handleRequestInternal(request, response);
    verify(request, response);

    reset(request, response);
    expect(request.getHeader("X-HTTP-Method-Override")).andReturn(null);
    expect(request.getMethod()).andReturn("POST");
    request.setAttribute("verb", VerbType.update);
    replay(request, response);
    exporter.handleRequestInternal(request, response);
    verify(request, response);

  }

  /**
   * tests the handleRESTOperation method.
   */
  public void testHandleRESTOperation() throws Exception {
    final HashMap<String, String> paramValues = new HashMap<String, String>();
    RESTResource resource = new RESTResource("example") {

      @Override
      public Set<VerbType> getSupportedVerbs() {
        return EnumSet.allOf(VerbType.class);
      }

      @Override
      public Map<String, String> getContextParameterAndProperNounValues(String requestContext) {
        return paramValues;
      }
    };

    resource.addOperation(VerbType.update, new MockRESTEndpoint(), MockRESTEndpoint.class.getMethod("updateExample", String.class, RootElementExample.class, Integer.TYPE, String[].class, String.class, String.class));
    RESTResourceXMLExporter controller = new RESTResourceXMLExporter(resource);
    controller.setApplicationContext(new GenericApplicationContext());

    HttpServletRequest request = createMock(HttpServletRequest.class);
    HttpServletResponse response = createMock(HttpServletResponse.class);
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Unsupported verb: " + VerbType.create);
    replay(request, response);
    controller.handleRESTOperation(VerbType.create, request, response);
    verify(request, response);
    reset(request, response);

    expect(request.getRequestURI()).andReturn("/ctx/is/unimportant");
    expect(request.getContextPath()).andReturn("");
    JAXBContext context = JAXBContext.newInstance(RootElementExample.class);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    context.createMarshaller().marshal(new RootElementExample(), bytes);
    expect(request.getParameterValues("arg2")).andReturn(new String[] {"9999"});
    expect(request.getParameterValues("arg3")).andReturn(new String[] {"value1", "value2"});
    expect(request.getInputStream()).andReturn(new ByteArrayServletInputStream(bytes.toByteArray()));
    replay(request, response);
    paramValues.put(null, "id");
    paramValues.put("uriParam1", "ctxValueOne");
    paramValues.put("otherParam", "otherValue");
    ModelAndView modelAndView = controller.handleRESTOperation(VerbType.update, request, response);
    verify(request, response);
    RESTResultView view = (RESTResultView) modelAndView.getView();
    assertNotNull(view.getResult());
    assertTrue(view.getResult() instanceof RootElementExample);
    reset(request, response);

    //todo: add some tests...
  }

  private static class ByteArrayServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream stream;

    public ByteArrayServletInputStream(byte[] bytes) {
      stream = new ByteArrayInputStream(bytes);
    }

    public int read() throws IOException {
      return stream.read();
    }
  }

}
