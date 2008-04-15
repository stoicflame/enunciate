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

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * JSON view for a REST payload. Assumes the payload is for XML data.
 *
 * @author Ryan Heaton
 */
public class JsonPayloadView extends RESTPayloadView {

  private final Map<String, String> ns2prefix;

  public JsonPayloadView(RESTOperation operation, Map<String, String> ns2prefix) {
    super(operation);

    this.ns2prefix = ns2prefix;
  }

  @Override
  protected boolean isXML(Object result) {
    RESTOperation operation = getOperation();
    boolean xml = operation.isDeliversXMLPayload();

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

    if (!xml && operation.getPayloadDeliveryMethod() != null) {
      try {
        Object body = operation.getPayloadDeliveryMethod().invoke(result);
        xml = ((body instanceof DataHandler) && (String.valueOf(((DataHandler) body).getContentType()).toLowerCase().contains("xml")));
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return xml;
  }

  @Override
  protected void marshalPayloadStream(final InputStream payloadStream, final HttpServletRequest request, HttpServletResponse response, boolean xml) throws Exception {
    if (xml) {
      String callbackName = null;
      String jsonpParameter = getOperation().getJSONPParameter();
      if (jsonpParameter != null) {
        callbackName = request.getParameter(jsonpParameter);
        callbackName = ((callbackName != null) && (callbackName.trim().length() > 0)) ? callbackName : null;
      }

      new JsonPHandler(callbackName) {
        public void writeBody(PrintWriter outStream) throws Exception {
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
          JsonDataHandlerView.convertXMLStreamToJSON(XMLInputFactory.newInstance().createXMLEventReader(payloadStream), streamWriter);
          streamWriter.flush();
          streamWriter.close();
        }
      }.writeTo(response.getWriter());
    }
    else {
      super.marshalPayloadStream(payloadStream, request, response, xml);
    }
  }

  @Override
  protected String getContentType(Object result) {
    return "application/json";
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
