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

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles exceptions that occur when handling a REST resource.
 *
 * @author Ryan Heaton
 */
public class RESTResourceExceptionHandler implements HandlerExceptionResolver, View {

  public final static String MODEL_EXCEPTION = "org.codehaus.enunciate.modules.rest.RESTResourceExceptionHandler#EXCEPTION";

  private final Map<Class, Method> errorBodies = new HashMap<Class, Method>();

  public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
    RESTRequestContentTypeHandler contentHandler = (RESTRequestContentTypeHandler) request.getAttribute(RESTRequestContentTypeHandler.class.getName());
    if (contentHandler != null) {
      return new ModelAndView(this, MODEL_EXCEPTION, exception);
    }

    return null;
  }

  public String getContentType() {
    return null;
  }

  public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
    Exception exception = (Exception) model.get(MODEL_EXCEPTION);
    RESTRequestContentTypeHandler contentHandler = (RESTRequestContentTypeHandler) request.getAttribute(RESTRequestContentTypeHandler.class.getName());

    int statusCode = 500;
    if (exception != null && contentHandler != null) {
      RESTError errorInfo = exception.getClass().getAnnotation(RESTError.class);
      if (errorInfo != null) {
        statusCode = errorInfo.errorCode();
      }

      String message = getMessage(exception);
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

      if (message != null) {
        response.setStatus(statusCode, message);
      }
      else {
        response.setStatus(statusCode);
      }

      if (result instanceof DataHandler) {
        response.setContentType(((DataHandler) result).getContentType());
        ((DataHandler) result).writeTo(response.getOutputStream());
      }
      else {
        RESTOperation operation = (RESTOperation) request.getAttribute(RESTOperation.class.getName());
        if (operation != null) {
          response.setContentType(String.format("%s;charset=%s", operation.getContentType(), operation.getCharset()));
        }
        contentHandler.write(result, request, response);
      }
    }
    else {
      response.sendError(statusCode);
    }
  }

  /**
   * Allows you to override the message (e.g. so you can localize things).
   *
   * @param exception you want to localize
   * @return a message to display to the user
   */
  protected String getMessage(Exception exception) {
    return exception.getMessage();
  }
}