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

package org.codehaus.enunciate.modules.rest.json;

import org.codehaus.enunciate.modules.rest.xml.JaxbXmlContentHandler;
import org.codehaus.enunciate.modules.rest.RESTOperation;
import org.codehaus.enunciate.rest.annotations.ContentTypeHandler;
import org.codehaus.jettison.mapped.MappedXMLInputFactory;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;
import org.codehaus.jettison.badgerfish.BadgerFishXMLInputFactory;
import org.codehaus.jettison.badgerfish.BadgerFishXMLOutputFactory;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/**
 * Content handler for JSON requests.
 *
 * @author Ryan Heaton
 */
@ContentTypeHandler (
  contentTypes = "application/json"
)
public class JsonContentHandler extends JaxbXmlContentHandler {

  private JsonSerializationMethod defaultSerializationMethod = JsonSerializationMethod.xmlMapped;

  /**
   * Unmarshal data from the request.
   *
   * @param unmarshaller The unmarshaller.
   * @param request The request.
   * @return The data.
   */
  @Override
  protected Object unmarshal(Unmarshaller unmarshaller, HttpServletRequest request) throws Exception {
    loadSerializationMethod(request, getDefaultSerializationMethod());
    XMLStreamReader reader;
    JsonSerializationMethod method = JsonUtil.loadSerializationMethod(request, JsonSerializationMethod.xmlMapped);
    switch (method) {
      case xmlMapped:
        reader = new MappedXMLInputFactory(getNamespacesToPrefixes()).createXMLStreamReader(request.getInputStream());
        break;
      case badgerfish:
        reader = new BadgerFishXMLInputFactory().createXMLStreamReader(request.getInputStream());
        break;
      default:
        throw new UnsupportedOperationException();
    }
    return unmarshaller.unmarshal(reader);
  }

  /**
   * Marshal the JSON data.
   *
   * @param data The data.
   * @param marshaller The marshaller.
   * @param request The request.
   * @param response The response.
   */
  @Override
  protected void marshal(final Object data, final Marshaller marshaller, final HttpServletRequest request, HttpServletResponse response) throws Exception {
    String callbackName = null;
    RESTOperation operation = (RESTOperation) request.getAttribute(RESTOperation.class.getName());

    String jsonpParameter = operation != null ? operation.getJSONPParameter() : null;
    if (jsonpParameter != null) {
      callbackName = request.getParameter(jsonpParameter);
      callbackName = ((callbackName != null) && (callbackName.trim().length() > 0)) ? callbackName : null;
    }

    new JsonPHandler(callbackName) {
      public void writeBody(PrintWriter outStream) throws XMLStreamException, JAXBException, IOException {
        marshalToStream(marshaller, data, request, outStream);
      }
    }.writeTo(response.getWriter());
  }

  protected void marshalToStream(Marshaller marshaller, Object data, HttpServletRequest request, Writer outStream) throws XMLStreamException, IOException, JAXBException {
    JsonSerializationMethod method = JsonUtil.loadSerializationMethod(request, JsonSerializationMethod.xmlMapped);
    switch (method) {
      case hierarchical:
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.autodetectAnnotations(true);
        xstream.toXML(data, outStream);
      case xmlMapped:
        marshaller.marshal(data, new MappedXMLOutputFactory(getNamespacesToPrefixes()).createXMLStreamWriter(outStream));
        break;
      case badgerfish:
        marshaller.marshal(data, new BadgerFishXMLOutputFactory().createXMLStreamWriter(outStream));
        break;
      default:
        throw new IllegalArgumentException("Unsupported JSON serialization method: " + method);
    }
  }

  /**
   * The default serialization method.
   *
   * @return The default serialization method.
   */
  public JsonSerializationMethod getDefaultSerializationMethod() {
    return defaultSerializationMethod;
  }

  /**
   * The default serialization method.
   *
   * @param defaultSerializationMethod The default serialization method.
   */
  public void setDefaultSerializationMethod(JsonSerializationMethod defaultSerializationMethod) {
    this.defaultSerializationMethod = defaultSerializationMethod;
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

}
