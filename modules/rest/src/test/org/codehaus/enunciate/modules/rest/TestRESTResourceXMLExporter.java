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
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class TestRESTResourceXMLExporter extends TestCase {

  /**
   * Tests handling that the noun and proper noun are property extracted from the request.
   */
  public void testHandleRequestInternal() throws Exception {
    RESTResource restResource = new RESTResource("mynoun") {

      @Override
      public Set<VerbType> getSupportedVerbs() {
        return new TreeSet(Arrays.asList(VerbType.values()));
      }
    };
    RESTResourceXMLExporter exporter = new RESTResourceXMLExporter(restResource) {
      @Override
      protected ModelAndView handleRESTOperation(String properNoun, VerbType verb, HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setAttribute("properNoun", properNoun);
        request.setAttribute("verb", verb);
        return null;
      }


    };
    exporter.setApplicationContext(new GenericApplicationContext());

    HttpServletRequest request = createMock(HttpServletRequest.class);
    HttpServletResponse response = createMock(HttpServletResponse.class);
    expect(request.getRequestURI()).andReturn("http://localhost:8080/context/subcontext/mynoun/mypropernoun");
    expect(request.getMethod()).andReturn("GET");
    request.setAttribute("properNoun", "mypropernoun");
    request.setAttribute("verb", VerbType.read);
    replay(request, response);
    exporter.handleRequestInternal(request, response);
    verify(request, response);

    reset(request, response);
    expect(request.getRequestURI()).andReturn("http://localhost:8080/some/nested/weird/context/subcontext/mynoun/mypropernoun");
    expect(request.getMethod()).andReturn("PUT");
    request.setAttribute("properNoun", "mypropernoun");
    request.setAttribute("verb", VerbType.create);
    replay(request, response);
    exporter.handleRequestInternal(request, response);
    verify(request, response);

    reset(request, response);
    expect(request.getRequestURI()).andReturn("http://localhost:8080/context/subcontext/mynoun/");
    expect(request.getMethod()).andReturn("POST");
    request.setAttribute("properNoun", null);
    request.setAttribute("verb", VerbType.update);
    replay(request, response);
    exporter.handleRequestInternal(request, response);
    verify(request, response);

    reset(request, response);
    expect(request.getRequestURI()).andReturn("http://localhost:8080/context/subcontext/mynoun");
    expect(request.getMethod()).andReturn("DELETE");
    request.setAttribute("properNoun", null);
    request.setAttribute("verb", VerbType.delete);
    replay(request, response);
    exporter.handleRequestInternal(request, response);
    verify(request, response);

    reset(request, response);
    expect(request.getRequestURI()).andReturn("http://localhost:8080/context/subcontext/");
    expect(request.getRequestURI()).andReturn("http://localhost:8080/context/subcontext/");
    response.sendError(HttpServletResponse.SC_NOT_FOUND, "http://localhost:8080/context/subcontext/");
    replay(request, response);
    exporter.handleRequestInternal(request, response);
    verify(request, response);

    reset(request, response);
    expect(request.getRequestURI()).andReturn("http://localhost:8080/context/some/really/strange/requst");
    expect(request.getRequestURI()).andReturn("http://localhost:8080/context/some/really/strange/requst");
    response.sendError(HttpServletResponse.SC_NOT_FOUND, "http://localhost:8080/context/some/really/strange/requst");
    replay(request, response);
    exporter.handleRequestInternal(request, response);
    verify(request, response);
  }

  /**
   * tests the handleRESTOperation method.
   */
  public void testHandleRESTOperation() throws Exception {
    RESTResource resource = new RESTResource("example") {

      @Override
      public Set<VerbType> getSupportedVerbs() {
        return new TreeSet(Arrays.asList(VerbType.values()));
      }
    };
    resource.addOperation(VerbType.update, new MockRESTEndpoint(), MockRESTEndpoint.class.getMethod("updateExample", String.class, RootElementExample.class, Integer.TYPE, String[].class));
    RESTResourceXMLExporter controller = new RESTResourceXMLExporter(resource);
    controller.setApplicationContext(new GenericApplicationContext());

    HttpServletRequest request = createMock(HttpServletRequest.class);
    HttpServletResponse response = createMock(HttpServletResponse.class);
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Unsupported verb: " + VerbType.create);
    replay(request, response);
    controller.handleRESTOperation("example", VerbType.create, request, response);
    verify(request, response);
    reset(request, response);

    JAXBContext context = JAXBContext.newInstance(RootElementExample.class);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    context.createMarshaller().marshal(new RootElementExample(), bytes);
    expect(request.getParameterValues("arg2")).andReturn(new String[] {"9999"});
    expect(request.getParameterValues("arg3")).andReturn(new String[] {"value1", "value2"});
    expect(request.getInputStream()).andReturn(new ByteArrayServletInputStream(bytes.toByteArray()));
    replay(request, response);
    ModelAndView modelAndView = controller.handleRESTOperation("id", VerbType.update, request, response);
    verify(request, response);
    RESTResultView view = (RESTResultView) modelAndView.getView();
    assertNotNull(view.getResult());
    assertTrue(view.getResult() instanceof RootElementExample);
    reset(request, response);
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
