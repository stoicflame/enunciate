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

import org.codehaus.jettison.badgerfish.BadgerFishXMLInputFactory;
import org.codehaus.jettison.mapped.MappedXMLInputFactory;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.Map;

/**
 * Utility class for unmarshalling a JSON request body.
 *
 * @author Ryan Heaton
 */
public class JsonUnmarshaller {

  public static Object unmarshal(HttpServletRequest request, Unmarshaller unmarshaller, Map<String, String> namespaces2prefix) throws JAXBException, IOException, XMLStreamException {
    XMLStreamReader reader = (request.getParameter("badgerfish") == null) ?
      new MappedXMLInputFactory(namespaces2prefix).createXMLStreamReader(request.getInputStream()) :
      new BadgerFishXMLInputFactory().createXMLStreamReader(request.getInputStream());
    return unmarshaller.unmarshal(reader);
  }
}
