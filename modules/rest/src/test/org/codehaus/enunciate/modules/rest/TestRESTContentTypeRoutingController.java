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

import junit.framework.TestCase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public class TestRESTContentTypeRoutingController extends TestCase {

  /**
   * test handleRequestInternal
   */
  public void testHandleRequestInternal() throws Exception {
    HashMap<String, String> contentTypes2Ids = new HashMap<String, String>();
    RESTContentTypeRoutingController controller = new RESTContentTypeRoutingController(new RESTResource("noun"), new ContentTypeSupport(contentTypes2Ids, null)) {
      @Override
      protected String getContentType(HttpServletRequest request) {
        return "application/data+xml";
      }
    };
    contentTypes2Ids.put("application/data+xml", "data");

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
    RESTContentTypeRoutingController controller = new RESTContentTypeRoutingController(new RESTResource("noun"), null);
    HttpServletRequest request = createMock(HttpServletRequest.class);
    expect(request.getParameter("contentType")).andReturn("application/xml");
    replay(request);
    assertEquals("application/xml", controller.getContentType(request));
    verify(request);
    reset(request);
  }

}
