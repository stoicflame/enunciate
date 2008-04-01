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
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Handles exception that occurred when invoking a RESTOperation. Marshals the body of the exception
 * via JAXB.
 *
 * @author Ryan Heaton
 */
public class JaxbXmlExceptionHandler implements HandlerExceptionResolver {

  private final Map<String, String> ns2prefix;
  private final Map<Class, Method> errorBodies = new HashMap<Class, Method>();

  public JaxbXmlExceptionHandler(Map<String, String> ns2prefix) {
    this.ns2prefix = ns2prefix;
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
    model.put(BasicRESTView.MODEL_STATUS, statusCode);
    model.put(BasicRESTView.MODEL_STATUS_MESSAGE, message);
    model.put(RESTOperationView.MODEL_RESULT, result);

    Class errorType = bodyMethod != null ? bodyMethod.getReturnType() : null;
    View view = loadErrorView(errorType, handler);
    return new ModelAndView(view, model);
  }

  /**
   * Load the error view for the specified request.  If the error occurred from a REST resource exporter, it will be provided to
   * the method call.
   *
   * @param errorType The type of the error.
   * @param handler The handler that was executed upon error (possibly null).
   * @return The error view
   */
  protected View loadErrorView(Class errorType, Object handler) {
    if ((handler instanceof RESTOperation) && (errorType != null)) {
      return newJaxbView(errorType, (RESTOperation) handler);
    }
    else {
      return new BasicRESTView();
    }
  }

  /**
   * Create a new jaxb view for the specified error type and operation.
   *
   * @param errorType The error type.
   * @param operation The operation.
   * @return The jaxb view for the error type.
   */
  protected JaxbXmlView newJaxbView(final Class errorType, final RESTOperation operation) {
    return new JaxbXmlView(operation, getNamespaces2Prefixes()) {
      @Override
      protected Marshaller newMarshaller() throws JAXBException {
        return JAXBContext.newInstance(errorType).createMarshaller();
      }
    };
  }

  /**
   * The map of namespaces to prefixes.
   *
   * @return The map of namespaces to prefixes.
   */
  public Map<String, String> getNamespaces2Prefixes() {
    return this.ns2prefix;
  }
}
