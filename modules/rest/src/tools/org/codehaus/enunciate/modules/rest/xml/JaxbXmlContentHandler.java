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

package org.codehaus.enunciate.modules.rest.xml;

import org.codehaus.enunciate.modules.rest.*;
import org.codehaus.enunciate.rest.annotations.ContentTypeHandler;

import javax.xml.bind.*;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

/**
 * Content type handler for JAXB XML.
 *
 * @author Ryan Heaton
 */
@ContentTypeHandler (
  contentTypes = { "text/xml", "application/xml" }
)
public class JaxbXmlContentHandler implements RESTRequestContentTypeHandler, RESTResourceAware, NamespacePrefixesAware, ContentTypeAware {

  private JAXBContext context;
  private Map<String, String> ns2prefix;
  private Object prefixMapper;
  private String contentType;

  public Object read(RESTRequest request) throws Exception {
    if (this.context == null) {
      throw new UnsupportedOperationException();
    }

    Unmarshaller unmarshaller = this.context.createUnmarshaller();
    unmarshaller.setAttachmentUnmarshaller(RESTAttachmentUnmarshaller.INSTANCE);
    return unmarshal(unmarshaller, request);
  }

  public void write(Object data, RESTRequest request, HttpServletResponse response) throws Exception {
    JAXBContext context = this.context;
    if (context == null) {
      //if a context wasn't specified, we'll just attempt to create one dynamically.
      context = JAXBContext.newInstance(data.getClass());
    }

    Marshaller marshaller = context.createMarshaller();
    marshaller.setAttachmentMarshaller(RESTAttachmentMarshaller.INSTANCE);
    if (this.prefixMapper != null) {
      try {
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", this.prefixMapper);
      }
      catch (PropertyException e) {
        //fall through...
      }
    }
    marshal(data, marshaller, request, response);
  }

  protected Object unmarshal(Unmarshaller unmarshaller, RESTRequest request) throws Exception {
    return unmarshaller.unmarshal(request.getInputStream());
  }

  protected void marshal(Object data, Marshaller marshaller, RESTRequest request, HttpServletResponse response) throws Exception {
    marshaller.marshal(data, response.getOutputStream());
  }

  /**
   * The JAXB context.
   *
   * @return The JAXB context.
   */
  public JAXBContext getContext() {
    return context;
  }

  /**
   * The JAXB context.
   *
   * @param context The JAXB context.
   */
  public void setContext(JAXBContext context) {
    this.context = context;
  }

  /**
   * Set the REST resource for this handler.
   *
   * @param resource The REST resource for this handler.
   */
  public void setRESTResource(RESTResource resource) {
    Set<Class> classes = new HashSet<Class>();
    for (RESTOperation operation : resource.getOperations()) {
      classes.addAll(operation.getContextClasses());
    }
    setContextClasses(classes.toArray(new Class[classes.size()]));
  }

  /**
   * Set the context classes for this handler.
   *
   * @param contextClasses The context classes.
   */
  public void setContextClasses(Class... contextClasses) {
    try {
      this.context = JAXBContext.newInstance(contextClasses);
    }
    catch (JAXBException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * The map of namespaces to prefixes.
   *
   * @return The map of namespaces to prefixes.
   */
  public Map<String, String> getNamespacesToPrefixes() {
    return this.ns2prefix;
  }

  /**
   * The map of namespaces to prefixes.
   *
   * @param ns2prefix The map of namespaces to prefixes.
   */
  public void setNamespacesToPrefixes(Map<String, String> ns2prefix) {
    this.ns2prefix = ns2prefix;

    Object prefixMapper;
    try {
      //we want to support a prefix mapper, but don't want to break those on JDK 6 that don't have the prefix mapper on the classpath.
      prefixMapper = Class.forName("org.codehaus.enunciate.modules.rest.xml.PrefixMapper").getConstructor(Map.class).newInstance(ns2prefix);
    }
    catch (Throwable e) {
      prefixMapper = null;
    }

    this.prefixMapper = prefixMapper;
  }

  /**
   * The content type assigned to this handler.
   *
   * @return The content type assigned to this handler.
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * The content type assigned to this handler.
   *
   * @param contentType The content type assigned to this handler.
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }
}
