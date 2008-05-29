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

package org.codehaus.enunciate.modules.rest;

import junit.framework.TestCase;
import org.codehaus.enunciate.rest.annotations.VerbType;
import org.codehaus.enunciate.modules.rest.xml.JaxbXmlContentHandler;
import static org.easymock.EasyMock.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author Ryan Heaton
 */
public class TestRESTResourceExporter extends TestCase {

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
    final RESTOperation operation = new RESTOperation(null, "content/type", VerbType.read, RESTOperationExamples.class.getMethod("properNoun", String.class), null);
    RESTResource restResource = new RESTResource("mynoun") {

      @Override
      public RESTOperation getOperation(String contentType, VerbType verb) {
        if (("content/type".equals(contentType)) && (verb == VerbType.read)) {
          return operation;
        }

        return null;
      }
    };
    
    final ModelAndView mv = new ModelAndView();
    final HashMap<String, String> contentTypesToIds = new HashMap<String, String>();
    final HashMap<String, RESTRequestContentTypeHandler> contentTypesToHandlers = new HashMap<String, RESTRequestContentTypeHandler>();
    RESTResourceExporter exporter = new RESTResourceExporter(restResource, null) {
      @Override
      protected ModelAndView handleRESTOperation(RESTOperation operation, RESTRequestContentTypeHandler handler, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mv;
      }

      @Override
      protected String findContentTypeId(HttpServletRequest request) {
        return contentTypesToIds.values().iterator().next();
      }

      @Override
      public VerbType getVerb(HttpServletRequest request) throws MethodNotAllowedException {
        return VerbType.read;
      }
    };

    contentTypesToIds.put("content/type", "CONTENT_TYPE_ID");
    JaxbXmlContentHandler handler = new JaxbXmlContentHandler();
    contentTypesToHandlers.put("content/type", handler);
    ContentTypeSupport ctSupport = new ContentTypeSupport(contentTypesToIds, contentTypesToHandlers);
    exporter.setContentTypeSupport(ctSupport);
    exporter.setApplicationContext(new GenericApplicationContext());

    HttpServletRequest request = createMock(HttpServletRequest.class);
    HttpServletResponse response = createMock(HttpServletResponse.class);
    request.setAttribute(RESTResource.class.getName(), restResource);
    request.setAttribute(RESTOperation.class.getName(), operation);
    request.setAttribute(RESTRequestContentTypeHandler.class.getName(), handler);
    replay(request, response);
    assertSame(mv, exporter.handleRequestInternal(request, response));
    verify(request, response);
    reset(request, response);

    contentTypesToIds.clear();
    contentTypesToIds.put("application/test", "test");
    contentTypesToHandlers.clear();
    contentTypesToHandlers.put("application/test", handler);
    ctSupport = new ContentTypeSupport(contentTypesToIds, contentTypesToHandlers);
    exporter.setContentTypeSupport(ctSupport);
    request.setAttribute(RESTResource.class.getName(), restResource);
    replay(request, response);
    try {
      exporter.handleRequestInternal(request, response);
      fail("shouldn't have allowed a method for the specified content type.");
    }
    catch (MethodNotAllowedException e) {
      //fall through...
    }
    verify(request, response);
    reset(request, response);

  }

  /**
   * test getVerb
   */
  public void testGetVerb() throws Exception {
    RESTResourceExporter exporter = new RESTResourceExporter(null, null);
    HttpServletRequest request = createMock(HttpServletRequest.class);

    expect(request.getHeader("X-HTTP-Method-Override")).andReturn(null);
    expect(request.getMethod()).andReturn("GET");
    replay(request);
    assertEquals(VerbType.read, exporter.getVerb(request));
    verify(request);

    reset(request);
    expect(request.getHeader("X-HTTP-Method-Override")).andReturn(null);
    expect(request.getMethod()).andReturn("PUT");
    replay(request);
    assertEquals(VerbType.create, exporter.getVerb(request));
    verify(request);

    reset(request);
    expect(request.getHeader("X-HTTP-Method-Override")).andReturn(null);
    expect(request.getMethod()).andReturn("POST");
    replay(request);
    assertEquals(VerbType.update, exporter.getVerb(request));
    verify(request);
  }

  /**
   * test findContentTypeId
   */
  public void testFindContentTypeId() throws Exception {
    RESTResourceExporter exporter = new RESTResourceExporter(null, null);
    HttpServletRequest request = createMock(HttpServletRequest.class);
    expect(request.getRequestURI()).andReturn("/context/of/my/request");
    expect(request.getContextPath()).andReturn("/context");
    replay(request);
    assertEquals("of", exporter.findContentTypeId(request));
    verify(request);
    reset(request);

    expect(request.getRequestURI()).andReturn("/context/of/my/request");
    expect(request.getContextPath()).andReturn("/context/");
    replay(request);
    assertEquals("of", exporter.findContentTypeId(request));
    verify(request);
    reset(request);
  }

  /**
   * tests the handleRESTOperation method.
   */
  public void testHandleRESTOperation() throws Exception {
    final HashMap<String, String> paramValues = new HashMap<String, String>();
    RESTResource resource = new RESTResource("example") {

      @Override
      public Map<String, String> getContextParameterAndProperNounValues(String requestContext) {
        return paramValues;
      }
    };

    resource.addOperation("text/xml", VerbType.update, MockRESTEndpoint.class.getMethod("updateExample", String.class, RootElementExample.class, Integer.TYPE, String[].class, String.class, String.class));
    RESTResourceExporter controller = new RESTResourceExporter(resource, new MockRESTEndpoint());
    controller.setContentTypeSupport(new ContentTypeSupport(null, null));
    controller.setApplicationContext(new GenericApplicationContext());
    controller.setMultipartRequestHandler(null); //not testing multipart request handling yet...

    HttpServletRequest request = createMock(HttpServletRequest.class);
    HttpServletResponse response = createMock(HttpServletResponse.class);
    RESTOperation operation = resource.getOperation("text/xml", VerbType.update);
    expect(request.getRequestURI()).andReturn("/ctx/is/unimportant");
    expect(request.getContextPath()).andReturn("");
    final JAXBContext context = JAXBContext.newInstance(RootElementExample.class);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    context.createMarshaller().marshal(new RootElementExample(), bytes);
    expect(request.getAttribute(RESTResource.class.getName())).andReturn(resource);
    expect(request.getParameterValues("arg2")).andReturn(new String[] {"9999"});
    expect(request.getParameterValues("arg3")).andReturn(new String[] {"value1", "value2"});
    expect(request.getInputStream()).andReturn(new ByteArrayServletInputStream(bytes.toByteArray()));
    paramValues.put(null, "id");
    paramValues.put("uriParam1", "ctxValueOne");
    paramValues.put("otherParam", "otherValue");
    JaxbXmlContentHandler handler = new JaxbXmlContentHandler() {
      @Override
      protected JAXBContext loadContext(RESTResource resource) throws JAXBException {
        return context;
      }

      @Override
      public void write(Object data, HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setAttribute("result", data.getClass().getName());
      }
    };
    response.setContentType("text/xml;charset=utf-8");
    request.setAttribute("result", RootElementExample.class.getName());
    replay(request, response);
    assertNull(controller.handleRESTOperation(operation, handler, request, response));
    verify(request, response);
    reset(request, response);

    //todo: add some tests...
  }

  public static class ByteArrayServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream stream;

    public ByteArrayServletInputStream(byte[] bytes) {
      stream = new ByteArrayInputStream(bytes);
    }

    public int read() throws IOException {
      return stream.read();
    }
  }

}
