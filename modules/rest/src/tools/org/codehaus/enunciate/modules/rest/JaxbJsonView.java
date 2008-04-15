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

import org.codehaus.jettison.badgerfish.BadgerFishXMLOutputFactory;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

/**
 * A JSON REST view of a JAXB result.
 *
 * @author Ryan Heaton
 */
public class JaxbJsonView<R> extends JaxbXmlView<R> {

  /**
   * Construct a view for the result of a REST operation.
   *
   * @param operation The operation.
   * @param ns2prefix The map of namespaces to prefixes.
   */
  public JaxbJsonView(RESTOperation operation, Map<String, String> ns2prefix) {
    super(operation, ns2prefix);
  }

  /**
   * Marshals the result as a JSON response.
   *
   * @param result The result to marshal.
   * @param marshaller The marshaller.
   * @param request The request.
   * @param response The response.
   */
  @Override
  protected void marshal(final R result, final Marshaller marshaller, final HttpServletRequest request, HttpServletResponse response) throws Exception {
    String callbackName = null;
    String jsonpParameter = getOperation().getJSONPParameter();
    if (jsonpParameter != null) {
      callbackName = request.getParameter(jsonpParameter);
      callbackName = ((callbackName != null) && (callbackName.trim().length() > 0)) ? callbackName : null;
    }

    new JsonPHandler(callbackName) {
      public void writeBody(PrintWriter outStream) throws XMLStreamException, JAXBException, IOException {
        marshalToStream(marshaller, result, request, outStream);
      }
    }.writeTo(response.getWriter());
  }

  protected void marshalToStream(Marshaller marshaller, R result, HttpServletRequest request, Writer outStream) throws XMLStreamException, IOException, JAXBException {
    XMLStreamWriter streamWriter;
    JsonSerializationMethod method = RESTResourceJSONExporter.loadSerializationMethod(request, JsonSerializationMethod.xmlMapped);
    switch (method) {
      case xmlMapped:
        streamWriter = new MappedXMLOutputFactory(getNamespaces2Prefixes()).createXMLStreamWriter(outStream);
        break;
      case badgerfish:
        streamWriter = new BadgerFishXMLOutputFactory().createXMLStreamWriter(outStream);
        break;
      default:
        throw new IllegalArgumentException("Unsupported JSON serialization method: " + method);
    }
    marshaller.marshal(result, streamWriter);
  }


  @Override
  protected String getContentType(R result) {
    return "application/json";
  }
}
