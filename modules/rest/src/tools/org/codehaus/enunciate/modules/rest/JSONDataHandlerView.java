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

import org.codehaus.jettison.mapped.MappedXMLOutputFactory;
import org.codehaus.jettison.badgerfish.BadgerFishXMLOutputFactory;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletOutputStream;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.Namespace;
import javax.xml.bind.Marshaller;
import javax.xml.bind.JAXBException;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

/**
 * JSON view for a data handler. Assumes that the data handler handles XML data, which is subsequently transformed into JSON.
 *
 * @author Ryan Heaton
 */
public class JSONDataHandlerView extends JSONResultView<DataHandler> {

  public JSONDataHandlerView(RESTOperation operation, DataHandler dataHandler, Map<String, String> ns2Prefix) {
    super(operation, dataHandler, ns2Prefix);
  }

  @Override
  protected void marshalToStream(Marshaller marshaller, HttpServletRequest request, ServletOutputStream outStream) throws XMLStreamException, IOException {
    if (getResult() != null) {
      InputStream stream = getResult().getInputStream();
      XMLStreamWriter streamWriter = (request.getParameter("badgerfish") == null) ?
        new MappedXMLOutputFactory(getNamespaces2Prefixes()).createXMLStreamWriter(outStream) :
        new BadgerFishXMLOutputFactory().createXMLStreamWriter(outStream);
      convertXMLStreamToJSON(XMLInputFactory.newInstance().createXMLEventReader(stream), streamWriter);
      streamWriter.flush();
      streamWriter.close();
    }
  }

  @Override
  protected Marshaller getMarshaller() throws JAXBException {
    return null;
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