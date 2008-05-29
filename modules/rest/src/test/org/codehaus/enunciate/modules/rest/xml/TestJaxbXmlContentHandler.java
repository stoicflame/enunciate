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

package org.codehaus.enunciate.modules.rest.xml;

import junit.framework.TestCase;
import org.codehaus.enunciate.modules.rest.MockRESTEndpoint;
import org.codehaus.enunciate.modules.rest.RESTResource;
import org.codehaus.enunciate.modules.rest.RootElementExample;
import org.codehaus.enunciate.modules.rest.TestRESTResourceExporter;
import org.codehaus.enunciate.rest.annotations.VerbType;
import static org.easymock.EasyMock.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.xml.bind.JAXBContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Ryan Heaton
 */
public class TestJaxbXmlContentHandler extends TestCase {

  /**
   * test read
   */
  public void testRead() throws Exception {
    HttpServletRequest request = createMock(HttpServletRequest.class);
    JaxbXmlContentHandler handler = new JaxbXmlContentHandler();
    expect(request.getAttribute(RESTResource.class.getName())).andReturn(null);
    replay(request);
    try {
      handler.read(request);
      fail();
    }
    catch (UnsupportedOperationException e) {
      //fall through...
    }
    verify(request);
    reset(request);

    RESTResource resource = new RESTResource("example");
    resource.addOperation("text/xml", VerbType.update, MockRESTEndpoint.class.getMethod("updateExample", String.class, RootElementExample.class, Integer.TYPE, String[].class, String.class, String.class));
    expect(request.getAttribute(RESTResource.class.getName())).andReturn(resource);
    JAXBContext context = JAXBContext.newInstance(RootElementExample.class);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    context.createMarshaller().marshal(new RootElementExample(), bytes);
    expect(request.getInputStream()).andReturn(new TestRESTResourceExporter.ByteArrayServletInputStream(bytes.toByteArray()));
    replay(request);
    assertTrue(handler.read(request) instanceof RootElementExample);
    assertTrue(handler.resourcesToContexts.containsKey(resource));
    verify(request);
    reset(request);
  }

  /**
   * test write
   */
  public void testWrite() throws Exception {
    HttpServletResponse response = createMock(HttpServletResponse.class);
    HttpServletRequest request = createMock(HttpServletRequest.class);
    JaxbXmlContentHandler handler = new JaxbXmlContentHandler();

    replay(request, response);
    handler.write(null, request, response);
    verify(request, response);
    reset(request, response);

    RESTResource resource = new RESTResource("example");
    resource.addOperation("text/xml", VerbType.update, MockRESTEndpoint.class.getMethod("updateExample", String.class, RootElementExample.class, Integer.TYPE, String[].class, String.class, String.class));
    expect(request.getAttribute(RESTResource.class.getName())).andReturn(resource);
    RootElementExample ex = new RootElementExample();
    JAXBContext context = JAXBContext.newInstance(RootElementExample.class);
    ByteArrayOutputStream assertedBytes = new ByteArrayOutputStream();
    context.createMarshaller().marshal(new RootElementExample(), assertedBytes);
    final ByteArrayOutputStream writtenBytes = new ByteArrayOutputStream();
    expect(response.getOutputStream()).andReturn(new ServletOutputStream() {
      public void write(int b) throws IOException {
        writtenBytes.write(b);
      }
    });
    replay(request, response);
    handler.write(ex, request, response);
    verify(request, response);
    reset(request, response);
    assertTrue(Arrays.equals(assertedBytes.toByteArray(), writtenBytes.toByteArray()));
  }

}
