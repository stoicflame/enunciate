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

import org.codehaus.enunciate.modules.rest.NamespacePrefixLookup;
import org.codehaus.enunciate.modules.rest.RESTOperation;
import org.codehaus.enunciate.modules.rest.RESTRequestContentTypeHandler;
import org.codehaus.enunciate.modules.rest.RESTResource;
import org.codehaus.enunciate.rest.annotations.ContentTypeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.io.IOException;

/**
 * Content type handler for JAXB XML.
 *
 * @author Ryan Heaton
 */
@ContentTypeHandler (
  contentTypes = { "text/xml", "application/xml" }
)
public class JaxbXmlContentHandler extends ApplicationObjectSupport implements RESTRequestContentTypeHandler {

  protected final Map<RESTResource, JAXBContext> resourcesToContexts = new TreeMap<RESTResource, JAXBContext>();
  private NamespacePrefixLookup namespaceLookup;
  private Object prefixMapper;

  /**
   * Read the object from the request.
   *
   * @param request The request.
   * @return The object.
   */
  public Object read(HttpServletRequest request) throws Exception {
    RESTResource resource = (RESTResource) request.getAttribute(RESTResource.class.getName());
    JAXBContext context = loadContext(resource);
    if (context == null) {
      throw new UnsupportedOperationException();
    }

    Unmarshaller unmarshaller = context.createUnmarshaller();
    unmarshaller.setAttachmentUnmarshaller(RESTAttachmentUnmarshaller.INSTANCE);
    return unmarshal(unmarshaller, request);
  }

  /**
   * Write the object to the response.
   *
   * @param data The data to write.
   * @param request The request.
   * @param response The response.
   */
  public void write(Object data, HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (data != null) {
      RESTResource resource = (RESTResource) request.getAttribute(RESTResource.class.getName());
      JAXBContext context = loadContext(resource);
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
  }

  /**
   * Load the JAXB context for the specified resource.
   *
   * @param resource the resource.
   * @return The JAXB context.
   */
  protected JAXBContext loadContext(RESTResource resource) throws JAXBException {
    if (resource == null) {
      return null;
    }
    
    JAXBContext context = this.resourcesToContexts.get(resource);
    if (context == null) {
      Set<Class> classes = new HashSet<Class>();
      for (RESTOperation operation : resource.getOperations()) {
        classes.addAll(operation.getContextClasses());
      }
      context = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
      this.resourcesToContexts.put(resource, context);
    }
    return context;
  }

  /**
   * Unmarshal the data from the request.
   *
   * @param unmarshaller The unmarshaller.
   * @param request The request.
   * @return The unmarshalled data.
   */
  protected Object unmarshal(Unmarshaller unmarshaller, HttpServletRequest request) throws Exception {
    Class typeConstraint = null;
    RESTOperation operation = (RESTOperation) request.getAttribute(RESTOperation.class.getName());
    if (operation != null) {
      typeConstraint = operation.getNounValueType();
    }

    return unmarshal(unmarshaller, request, typeConstraint);
  }

  /**
   * Unmarshal the data from the request.
   *
   * @param unmarshaller The unmarshaller.
   * @param request The request
   * @param typeConstraint The type constraint
   * @return The object.
   */
  protected Object unmarshal(Unmarshaller unmarshaller, HttpServletRequest request, Class typeConstraint) throws Exception {
    if (typeConstraint != null) {
      //if a type constraint is applied, specify it when unmarshalling.
      return unmarshaller.unmarshal(new StreamSource(request.getInputStream()), typeConstraint).getValue();
    }
    else {
      return unmarshaller.unmarshal(request.getInputStream());
    }
  }

  /**
   * Marshal the data to the response.
   *
   * @param data The data.
   * @param marshaller The marshaller.
   * @param request The request.
   * @param response The response.
   */
  protected void marshal(Object data, Marshaller marshaller, HttpServletRequest request, HttpServletResponse response) throws Exception {
    marshaller.marshal(data, response.getOutputStream());
  }

  /**
   * The map of namespaces to prefixes.
   *
   * @return The map of namespaces to prefixes.
   */
  public Map<String, String> getNamespacesToPrefixes() {
    return this.namespaceLookup != null ? this.namespaceLookup.getNamespacesToPrefixes() : null;
  }

  /**
   * The namespace lookup.
   *
   * @return The namespace lookup.
   */
  public NamespacePrefixLookup getNamespaceLookup() {
    return namespaceLookup;
  }

  /**
   * The namespace lookup.
   *
   * @param namespaceLookup The namespace lookup.
   */
  @Autowired (required = false)
  public void setNamespaceLookup(NamespacePrefixLookup namespaceLookup) {
    this.namespaceLookup = namespaceLookup;

    Object prefixMapper;
    try {
      //we want to support a prefix mapper, but don't want to break those on JDK 6 that don't have the prefix mapper on the classpath.
      prefixMapper = Class.forName("org.codehaus.enunciate.modules.rest.xml.PrefixMapper").getConstructor(Map.class).newInstance(getNamespacesToPrefixes());
    }
    catch (Throwable e) {
      prefixMapper = null;
    }

    this.prefixMapper = prefixMapper;
  }

}
