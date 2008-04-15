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

import org.springframework.beans.BeansException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Map;

/**
 * A controller for the JSON API.
 *
 * @author Ryan Heaton
 */
public class RESTResourceJSONExporter extends RESTResourceXMLExporter {

  private boolean jettisonEnabled = false;
  private boolean xstreamEnabled = false;
  private JsonSerializationMethod defaultSerializationMethod;

  public RESTResourceJSONExporter(RESTResource resource, JsonSerializationMethod defaultSerializationMethod) {
    super(resource);

    this.defaultSerializationMethod = defaultSerializationMethod;
  }

  protected void initApplicationContext() throws BeansException {
    if (getExceptionHandler() == null) {
      setExceptionHandler(new JaxbJsonExceptionHandler(getNamespaces2Prefixes(), getDefaultSerializationMethod()));
    }

    super.initApplicationContext();

    try {
      Class.forName("org.codehaus.jettison.mapped.MappedXMLOutputFactory", true, RESTResourceJSONExporter.class.getClassLoader());
      jettisonEnabled = true;
    }
    catch (Throwable e) {
      jettisonEnabled = false;
    }

    try {
      Class.forName("com.thoughtworks.xstream.XStream", true, RESTResourceJSONExporter.class.getClassLoader());
      xstreamEnabled = true;
    }
    catch (Throwable e) {
      xstreamEnabled = false;
    }
  }

  @Override
  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (jettisonEnabled || xstreamEnabled) {
      return super.handleRequestInternal(request, response);
    }
    else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
      return null;
    }
  }

  @Override
  protected Object unmarshalNounValue(HttpServletRequest request, Unmarshaller unmarshaller) throws JAXBException, IOException, XMLStreamException {
    if (jettisonEnabled) {
      return JsonUnmarshaller.unmarshal(request, unmarshaller, getNamespaces2Prefixes());
    }
    else {
      throw new UnmarshalException("Jettison not enabled: cannot unmarshal the request body.");
    }
  }

  @Override
  protected BasicRESTView createDataHandlerView(RESTOperation operation, HttpServletRequest request) {
    if (jettisonEnabled) {
      return new JsonDataHandlerView(operation, getNamespaces2Prefixes());
    }
    else {
      return super.createDataHandlerView(operation, request);
    }
  }

  @Override
  protected BasicRESTView createPayloadView(RESTOperation operation, HttpServletRequest request) {
    if (jettisonEnabled) {
      return new JsonPayloadView(operation, getNamespaces2Prefixes());
    }
    else {
      return super.createDataHandlerView(operation, request);
    }
  }

  @Override
  protected BasicRESTView createRESTView(RESTOperation operation, HttpServletRequest request) {
    JsonSerializationMethod serializationMethod = loadSerializationMethod(request);
    switch (serializationMethod) {
      case xmlMapped:
      case badgerfish:
        if (!jettisonEnabled) {
          throw new IllegalArgumentException("Cannot support " + serializationMethod + " JSON. (Jettison is disabled.)");
        }
        return new JaxbJsonView(operation, getNamespaces2Prefixes());

      case hierarchical:
        if (!xstreamEnabled) {
          throw new IllegalArgumentException("Cannot support " + serializationMethod + " JSON. (XStream is disabled.)");
        }
        return new JsonHierarchicalView(operation);

      default:
        throw new IllegalArgumentException("Illegal JSON serialization method: " + serializationMethod);
    }
  }

  /**
   * Loads the JSON serialization method from the specified request.
   *
   * @param request The request.
   * @return The serialization method.
   */
  protected JsonSerializationMethod loadSerializationMethod(HttpServletRequest request) {
    return loadSerializationMethod(request, getDefaultSerializationMethod());
  }

  /**
   * Loads the JSON serialization method from the specified request.
   *
   * @param request       The request.
   * @param defaultMethod The default method.
   * @return The serialization method.
   */
  public static JsonSerializationMethod loadSerializationMethod(HttpServletRequest request, JsonSerializationMethod defaultMethod) {
    Map parameterMap = request.getParameterMap();
    for (JsonSerializationMethod method : JsonSerializationMethod.values()) {
      if (parameterMap.containsKey(method.toString())) {
        defaultMethod = method;
        break;
      }
    }

    return defaultMethod;
  }

  /**
   * The default serialization method for this JSON exporter.
   *
   * @return The default serialization method for this JSON exporter.
   */
  public JsonSerializationMethod getDefaultSerializationMethod() {
    return defaultSerializationMethod;
  }
}
