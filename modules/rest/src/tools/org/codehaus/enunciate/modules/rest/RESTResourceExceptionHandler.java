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

import org.codehaus.enunciate.rest.annotations.RESTError;
import org.codehaus.enunciate.rest.annotations.RESTErrorBody;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Handles exceptions that occur when handling a REST resource.
 *
 * @author Ryan Heaton
 */
public class RESTResourceExceptionHandler implements HandlerExceptionResolver {

  private final Map<Class, Method> errorBodies = new HashMap<Class, Method>();
  private final RESTRequestContentTypeHandler handler;
  private final String contentType;

  public RESTResourceExceptionHandler(RESTRequestContentTypeHandler handler, String contentType) {
    this.handler = handler;
    this.contentType = contentType;
  }

  public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
    int statusCode = 500;
    RESTError errorInfo = exception.getClass().getAnnotation(RESTError.class);
    if (errorInfo != null) {
      statusCode = errorInfo.errorCode();
    }

    String message = exception.getMessage();
    if ((message == null) && (statusCode == 404)) {
      message = request.getRequestURI();
    }

    Method bodyMethod = errorBodies.get(exception.getClass());
    if (!errorBodies.containsKey(exception.getClass())) {
      bodyMethod = null;
      for (Method method : exception.getClass().getMethods()) {
        if (method.isAnnotationPresent(RESTErrorBody.class)) {
          bodyMethod = method;
          break;
        }
      }

      errorBodies.put(exception.getClass(), bodyMethod);
    }

    Object result = null;
    if (bodyMethod != null) {
      try {
        result = bodyMethod.invoke(exception);
      }
      catch (Exception e) {
        //fall through...
      }
    }

    TreeMap<String, Object> model = new TreeMap<String, Object>();
    model.put(RESTResourceView.MODEL_RESULT, result);
    View view;
    if (request instanceof RESTRequest) {
      RESTRequest restRequest = (RESTRequest) request;
      view = new RESTResourceView(restRequest.getOperation(), this.handler, this.contentType);
    }
    else {
    }
    return new ModelAndView(view, model);
  }

}