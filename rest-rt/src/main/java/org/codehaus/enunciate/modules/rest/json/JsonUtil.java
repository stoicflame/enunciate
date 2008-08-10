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

import org.codehaus.enunciate.modules.rest.json.JsonSerializationMethod;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.XMLEvent;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.Namespace;
import javax.xml.namespace.QName;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Utility methods for JSON.
 *
 * @author Ryan Heaton
 */
public class JsonUtil {

  private JsonUtil() {

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

  public static void convertXMLStreamToJSON(XMLEventReader reader, XMLStreamWriter streamWriter) throws XMLStreamException {
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
