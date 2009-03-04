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
import org.springframework.web.servlet.ModelAndView;
import org.codehaus.enunciate.rest.annotations.VerbType;

import junit.framework.TestCase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.TreeMap;

/**
 * @author Ryan Heaton
 */
public class TestRESTResourceExceptionHandler extends TestCase {
  
  /**
   * test resolveException
   */
  public void testResolveException() throws Exception {
    HttpServletRequest request = createMock(HttpServletRequest.class);
    HttpServletResponse response = createMock(HttpServletResponse.class);
    RESTResourceExceptionHandler handler = new RESTResourceExceptionHandler();
    expect(request.getAttribute(RESTRequestContentTypeHandler.class.getName())).andReturn(null);
    replay(request, response);
    assertNull(handler.resolveException(request, response, null, null));
    verify(request, response);
    reset(request, response);
    expect(request.getAttribute(RESTRequestContentTypeHandler.class.getName())).andReturn(createNiceMock(RESTRequestContentTypeHandler.class));
    replay(request, response);
    ModelAndView modelAndView = handler.resolveException(request, response, null, null);
    assertSame(handler, modelAndView.getView());
    assertTrue(modelAndView.getModel().containsKey(RESTExceptionHandler.MODEL_EXCEPTION));
    verify(request, response);
    reset(request, response);
  }

  /**
   * test render
   */
  public void testRender() throws Exception {
    HttpServletRequest request = createMock(HttpServletRequest.class);
    HttpServletResponse response = createMock(HttpServletResponse.class);
    RESTResourceExceptionHandler handler = new RESTResourceExceptionHandler();
    TreeMap model = new TreeMap();

    expect(request.getAttribute(RESTRequestContentTypeHandler.class.getName())).andReturn(null);
    response.sendError(500);
    replay(request, response);
    handler.render(model, request, response);
    verify(request, response);
    reset(request, response);

    expect(request.getAttribute(RESTRequestContentTypeHandler.class.getName())).andReturn(createNiceMock(RESTRequestContentTypeHandler.class));
    response.sendError(500);
    replay(request, response);
    handler.render(model, request, response);
    verify(request, response);
    reset(request, response);

    RESTRequestContentTypeHandler contentHandler = createMock(RESTRequestContentTypeHandler.class);
    model.put(RESTExceptionHandler.MODEL_EXCEPTION, new Exception("hello!"));
    expect(request.getAttribute(RESTRequestContentTypeHandler.class.getName())).andReturn(contentHandler);
    response.setStatus(500, "hello!");
    expect(request.getAttribute(RESTOperation.class.getName())).andReturn(null);
    contentHandler.write(null, request, response);
    replay(request, response, contentHandler);
    handler.render(model, request, response);
    verify(request, response, contentHandler);
    reset(request, response, contentHandler);

    RootElementExample ree = new RootElementExample();
    model.put(RESTExceptionHandler.MODEL_EXCEPTION, new ExceptionExample(ree, "wow"));
    expect(request.getAttribute(RESTRequestContentTypeHandler.class.getName())).andReturn(contentHandler);
    response.setStatus(333, "wow");
    final RESTOperation operation = new RESTOperation(null, "content/type", VerbType.read, RESTOperationExamples.class.getMethod("properNoun", String.class), null);
    expect(request.getAttribute(RESTOperation.class.getName())).andReturn(operation);
    response.setContentType("content/type;charset=utf-8");
    contentHandler.write(ree, request, response);
    replay(request, response, contentHandler);
    handler.render(model, request, response);
    verify(request, response, contentHandler);
    reset(request, response, contentHandler);
  }

}
