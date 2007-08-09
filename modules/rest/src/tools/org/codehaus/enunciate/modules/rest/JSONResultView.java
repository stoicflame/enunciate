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

import org.codehaus.jettison.badgerfish.BadgerFishXMLOutputFactory;
import org.codehaus.jettison.mapped.MappedXMLOutputFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLStreamWriter;
import java.util.Map;

/**
 * A JSON view of a REST result.
 *
 * @author Ryan Heaton
 */
public class JSONResultView extends RESTResultView {

  /**
   * Construct a view for the result of a REST operation.
   *
   * @param operation The operation.
   * @param result    The result.
   * @param ns2prefix The map of namespaces to prefixes.
   */
  public JSONResultView(RESTOperation operation, Object result, Map<String, String> ns2prefix) {
    super(operation, result, ns2prefix);
  }

  /**
   * Marshals the result as a JSON response.
   *
   * @param marshaller The marshaller.
   * @param request The request.
   * @param response The response.
   */
  @Override
  protected void marshal(Marshaller marshaller, HttpServletRequest request, HttpServletResponse response) throws Exception {
    XMLStreamWriter streamWriter = (request.getParameter("badgerfish") == null) ?
      new MappedXMLOutputFactory(getNamespaces2Prefixes()).createXMLStreamWriter(response.getOutputStream()) :
      new BadgerFishXMLOutputFactory().createXMLStreamWriter(response.getOutputStream());
    response.setContentType("application/json");
    marshaller.marshal(getResult(), streamWriter);
  }
}
