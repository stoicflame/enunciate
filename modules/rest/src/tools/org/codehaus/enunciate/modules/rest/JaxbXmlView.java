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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.util.Map;

/**
 * JAXB view for REST results.  Default implementation simply renders the XML.
 *
 * @author Ryan Heaton
 */
public class JaxbXmlView<R> extends RESTOperationView<R> {

  private final Map<String, String> ns2prefix;
  private final Object prefixMapper;

  /**
   * Construct a JAXB view for the result of a REST operation.
   *
   * @param operation The operation.
   * @param ns2prefix The namespace-to-prefix map.
   */
  public JaxbXmlView(RESTOperation operation, Map<String, String> ns2prefix) {
    super(operation);
    this.ns2prefix = ns2prefix;
    Object prefixMapper;
    try {
      //we want to support a prefix mapper, but don't want to break those on JDK 6 that don't have the prefix mapper on the classpath.
      prefixMapper = Class.forName("org.codehaus.enunciate.modules.rest.PrefixMapper").getConstructor(Map.class).newInstance(ns2prefix);
    }
    catch (Throwable e) {
      prefixMapper = null;
    }
    
    this.prefixMapper = prefixMapper;
  }

  /**
   * Marshalls the result via JAXB.
   *
   * @param result The result to marshal.
   * @param request The request.
   * @param response The response.
   */
  protected void renderResult(R result, HttpServletRequest request, HttpServletResponse response) throws Exception {
    marshal(result, loadMarshaller(), request, response);
  }

  /**
   * Loads a marshaller instance.
   *
   * @return The marshaller.
   */
  protected Marshaller loadMarshaller() throws JAXBException {
    Marshaller marshaller = newMarshaller();
    marshaller.setAttachmentMarshaller(RESTAttachmentMarshaller.INSTANCE);
    if (this.prefixMapper != null) {
      try {
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);
      }
      catch (PropertyException e) {
        //fall through...
      }
    }
    return marshaller;
  }

  /**
   * Factory method for a new marshaller.
   *
   * @return The new marshaller.
   */
  protected Marshaller newMarshaller() throws JAXBException {
    return operation.getSerializationContext().createMarshaller();
  }

  /**
   * Does the marshalling operation.
   *
   * @param result The result to marshal.
   * @param marshaller The marshaller to use.
   * @param request The request.
   * @param response The response.
   */
  protected void marshal(R result, Marshaller marshaller, HttpServletRequest request, HttpServletResponse response) throws Exception {
    marshaller.marshal(result, response.getOutputStream());
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
