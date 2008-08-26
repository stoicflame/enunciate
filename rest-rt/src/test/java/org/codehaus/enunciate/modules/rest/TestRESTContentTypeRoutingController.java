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

import static org.easymock.EasyMock.*;
import org.codehaus.enunciate.modules.rest.xml.JaxbXmlContentHandler;

import junit.framework.TestCase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Ryan Heaton
 */
public class TestRESTContentTypeRoutingController extends TestCase {

  /**
   * test handleRequestInternal
   */
  public void testHandleRequestInternal() throws Exception {
    HashMap<String, String> contentTypes2Ids = new HashMap<String, String>();
    HashMap<String, RESTRequestContentTypeHandler> contentTypes2Handlers = new HashMap<String, RESTRequestContentTypeHandler>();
    RESTContentTypeRoutingController controller = new RESTContentTypeRoutingController(new RESTResource("noun")) {
      @Override
      protected List<String> getContentTypesByPreference(HttpServletRequest request) {
        return Arrays.asList("application/data+xml");
      }
    };
    contentTypes2Ids.put("application/data+xml", "data");
    contentTypes2Handlers.put("application/data+xml", new JaxbXmlContentHandler());
    controller.setContentTypeSupport(new ContentTypeSupport(contentTypes2Ids, contentTypes2Handlers));

    HttpServletRequest request = createMock(HttpServletRequest.class);
    HttpServletResponse response = createMock(HttpServletResponse.class);
    expect(request.getRequestURI()).andReturn("/context/rest/noun");
    expect(request.getContextPath()).andReturn("/context");
    RequestDispatcher dispatcher = createMock(RequestDispatcher.class);
    expect(request.getRequestDispatcher("/data/noun")).andReturn(dispatcher);
    dispatcher.forward(request, response);
    replay(request, response, dispatcher);
    controller.handleRequestInternal(request, response);
    verify(request, response, dispatcher);
    reset(request, response, dispatcher);

    expect(request.getRequestURI()).andReturn("/context/rest/noun");
    expect(request.getContextPath()).andReturn("/context/");
    expect(request.getRequestDispatcher("/data/noun")).andReturn(dispatcher);
    dispatcher.forward(request, response);
    replay(request, response, dispatcher);
    controller.handleRequestInternal(request, response);
    verify(request, response, dispatcher);
    reset(request, response, dispatcher);

    contentTypes2Ids.clear();
    controller.setContentTypeSupport(new ContentTypeSupport(null, null));
    expect(request.getRequestURI()).andReturn("/context/rest/noun");
    expect(request.getContextPath()).andReturn("/context/");
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
    replay(request, response, dispatcher);
    controller.handleRequestInternal(request, response);
    verify(request, response, dispatcher);
    reset(request, response, dispatcher);
  }

  /**
   * test getContentType
   */
  public void testGetContentType() throws Exception {
    RESTContentTypeRoutingController controller = new RESTContentTypeRoutingController(new RESTResource("noun") {
      @Override
      public String getDefaultContentType() {
        return "application/xml";
      }
    });
    HttpServletRequest request = createMock(HttpServletRequest.class);
    expect(request.getParameter("contentType")).andReturn("application/xml");
    replay(request);
    assertEquals("application/xml", controller.getContentTypesByPreference(request).get(0));
    verify(request);
    reset(request);

    List<String> preferenceOrder;

    expect(request.getParameter("contentType")).andReturn(null);
    expect(request.getHeaders("Accept")).andReturn(Collections.enumeration(Arrays.asList("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")));
    replay(request);
    preferenceOrder = controller.getContentTypesByPreference(request);
    verify(request);
    reset(request);
    assertEquals(4, preferenceOrder.size());
    assertTrue(preferenceOrder.subList(0, 2).contains("text/html"));
    assertTrue(preferenceOrder.subList(0, 2).contains("application/xhtml+xml"));
    assertEquals("application/xml", preferenceOrder.get(2));
    assertEquals("*/*", preferenceOrder.get(3));

    expect(request.getParameter("contentType")).andReturn(null);
    expect(request.getHeaders("Accept")).andReturn(Collections.enumeration(Arrays.asList("text/html,application/xhtml+xml,*/*;q=0.8,application/*;q=0.9")));
    replay(request);
    preferenceOrder = controller.getContentTypesByPreference(request);
    verify(request);
    reset(request);
    assertEquals(5, preferenceOrder.size());
    assertTrue(preferenceOrder.subList(0, 2).contains("text/html"));
    assertTrue(preferenceOrder.subList(0, 2).contains("application/xhtml+xml"));
    assertEquals("application/xml", preferenceOrder.get(2));
    assertEquals("application/*", preferenceOrder.get(3));
    assertEquals("*/*", preferenceOrder.get(4));


  }

}
