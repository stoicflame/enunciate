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

import org.codehaus.enunciate.rest.annotations.RESTError;
import org.codehaus.enunciate.rest.annotations.RESTErrorBody;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Has the response send the appropriate error, according to the error code of the (possibly annotated) exception class.
 *
 * @author Ryan Heaton
 */
public class RESTExceptionHandler implements HandlerExceptionResolver {

  private final Map<Class, Method> errorBodies = new HashMap<Class, Method>();

  public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object object, Exception exception) {
    int errorCode = 500;
    RESTError errorInfo = exception.getClass().getAnnotation(RESTError.class);
    if (errorInfo != null) {
      errorCode = errorInfo.errorCode();
    }

    String message = exception.getMessage();
    if ((message == null) && (errorCode == 404)) {
      message = request.getRequestURI();
    }

    Method body = errorBodies.get(exception.getClass());
    if (!errorBodies.containsKey(exception.getClass())) {
      body = null;
      for (Method method : exception.getClass().getMethods()) {
        if (method.isAnnotationPresent(RESTErrorBody.class)) {
          body = method;
          break;
        }
      }

      errorBodies.put(exception.getClass(), body);
    }

    if (body != null) {
      try {
        Marshaller marshaller = JAXBContext.newInstance(body.getReturnType()).createMarshaller();
        response.setStatus(errorCode, message);
        marshaller.marshal(body.invoke(exception), response.getOutputStream());
      }
      catch (Exception e) {
        //fall through...
      }
    }

    try {
      response.sendError(errorCode, message);
    }
    catch (IOException e) {
      //fall through...
    }
    return null;
  }
}
