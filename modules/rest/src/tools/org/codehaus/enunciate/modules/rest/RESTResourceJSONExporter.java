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

import org.codehaus.enunciate.rest.annotations.VerbType;
import org.springframework.beans.BeansException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.activation.DataHandler;
import java.lang.reflect.InvocationTargetException;

/**
 * A controller for the JSON API.
 *
 * @author Ryan Heaton
 */
public class RESTResourceJSONExporter extends RESTResourceXMLExporter {

  private boolean enabled = false;

  public RESTResourceJSONExporter(RESTResource resource) {
    super(resource);
  }

  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    try {
      Class.forName("org.codehaus.jettison.mapped.MappedXMLOutputFactory", true, RESTResourceJSONExporter.class.getClassLoader());
      enabled = true;
    }
    catch (ClassNotFoundException e) {
      enabled = false;
    }
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (enabled) {
      return super.handleRequestInternal(request, response);
    }
    else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
      return null;
    }
  }

  @Override
  protected boolean isOperationAllowed(RESTOperation operation) {
    return super.isOperationAllowed(operation) && operation.getVerb() == VerbType.read;
  }

  @Override
  protected RESTResultView createDataHandlerView(RESTOperation operation, DataHandler dataHandler) {
    boolean xml = operation.isWrapsXMLPayload() || (dataHandler != null && String.valueOf(dataHandler.getContentType()).toLowerCase().contains("xml"));
    return xml ? new JSONDataHandlerView(operation, dataHandler, getNamespaces2Prefixes()) : super.createDataHandlerView(operation, dataHandler);
  }

  @Override
  protected RESTResultView createPayloadView(RESTOperation operation, Object result) {
    //todo: you've got to create another annotation @RESTPayloadIsXML
    //todo: you've got to document the new annotation and the xml() value of RESTOperation
    boolean xml = operation.isWrapsXMLPayload();

    if (!xml && operation.getPayloadXmlHintMethod() != null) {
      try {
        Boolean xmlHintResult = (Boolean) operation.getPayloadXmlHintMethod().invoke(result);
        xml = xmlHintResult != null && xmlHintResult;
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    if (!xml && operation.getPayloadContentTypeMethod() != null) {
      try {
        String contentType = (String) operation.getPayloadContentTypeMethod().invoke(result);
        xml = contentType != null && contentType.toLowerCase().contains("xml");
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    if (!xml && operation.getPayloadBodyMethod() != null) {
      try {
        Object body = operation.getPayloadBodyMethod().invoke(result);
        xml = ((body instanceof DataHandler) && (String.valueOf(((DataHandler)body).getContentType()).toLowerCase().contains("xml")));
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    return xml ? new JSONPayloadView(operation, result, getNamespaces2Prefixes()) : super.createPayloadView(operation, result);
  }

  @Override
  protected RESTResultView createRESTView(RESTOperation operation, Object result) {
    return new JSONResultView(operation, result, getNamespaces2Prefixes());
  }

}
