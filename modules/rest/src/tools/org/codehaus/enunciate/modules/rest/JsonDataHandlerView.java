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
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;

/**
 * JSON view for a data handler. Assumes that the data handler handles XML data, which is subsequently transformed into JSON.
 *
 * @author Ryan Heaton
 */
public class JsonDataHandlerView extends DataHandlerView {

  private final Map<String, String> ns2prefix;

  public JsonDataHandlerView(RESTOperation operation, Map<String, String> ns2Prefix) {
    super(operation);

    this.ns2prefix = ns2Prefix;
  }

  @Override
  protected void renderResult(final DataHandler result, final HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (result != null) {
      boolean xml = getOperation().isDeliversXMLPayload() || String.valueOf(result.getContentType()).toLowerCase().contains("xml");
      if (xml) {
        String callbackName = null;
        String jsonpParameter = getOperation().getJSONPParameter();
        if (jsonpParameter != null) {
          callbackName = request.getParameter(jsonpParameter);
          callbackName = ((callbackName != null) && (callbackName.trim().length() > 0)) ? callbackName : null;
        }

        new JsonPHandler(callbackName) {
          public void writeBody(PrintWriter outStream) throws Exception {
            InputStream stream = result.getInputStream();
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
            convertXMLStreamToJSON(XMLInputFactory.newInstance().createXMLEventReader(stream), streamWriter);
            streamWriter.flush();
            streamWriter.close();
          }
        }.writeTo(response.getWriter());
      }
      else {
        // if it's not XML, we can't convert it to JSON, so just write it out.
        result.writeTo(response.getOutputStream());
      }
    }
  }

  /**
   * The map of namespaces to prefixes.
   *
   * @return The map of namespaces to prefixes.
   */
  public Map<String, String> getNamespaces2Prefixes() {
    return this.ns2prefix;
  }

  protected static void convertXMLStreamToJSON(XMLEventReader reader, XMLStreamWriter streamWriter) throws XMLStreamException {
    while (reader.hasNext()) {
      XMLEvent event = (XMLEvent) reader.next();
      switch (event.getEventType()) {
        case XMLStreamConstants.ATTRIBUTE:
          Attribute attribute = (Attribute) event;
          QName attributeName = attribute.getName();
          String attributeValue = attribute.getValue();
          streamWriter.writeAttribute(attributeName.getNamespaceURI(), attributeName.getLocalPart(), attributeValue);
          break;
        case XMLStreamConstants.CDATA:
          streamWriter.writeCData(event.asCharacters().getData());
          break;
        case XMLStreamConstants.CHARACTERS:
          String trimmedData = event.asCharacters().getData().trim();
          if (trimmedData.length() > 0) {
            streamWriter.writeCharacters(trimmedData);
          }
          break;
        case XMLStreamConstants.COMMENT:
          streamWriter.writeComment(((Comment)event).getText());
          break;
        case XMLStreamConstants.DTD:
          break;
        case XMLStreamConstants.END_DOCUMENT:
          streamWriter.writeEndDocument();
          break;
        case XMLStreamConstants.END_ELEMENT:
          streamWriter.writeEndElement();
          break;
        case XMLStreamConstants.ENTITY_DECLARATION:
          break;
        case XMLStreamConstants.NAMESPACE:
          String nsURI = ((Namespace) event).getNamespaceURI();
          String prefix = ((Namespace) event).getPrefix();
          streamWriter.writeNamespace(prefix, nsURI);
          break;
        case XMLStreamConstants.NOTATION_DECLARATION:
          break;
        case XMLStreamConstants.PROCESSING_INSTRUCTION:
          break;
        case XMLStreamConstants.SPACE:
          break;
        case XMLStreamConstants.START_DOCUMENT:
          streamWriter.writeStartDocument();
          break;
        case XMLStreamConstants.START_ELEMENT:
          QName qName = event.asStartElement().getName();
          streamWriter.writeStartElement(qName.getNamespaceURI(), qName.getLocalPart());
          break;
      }
    }
  }
}