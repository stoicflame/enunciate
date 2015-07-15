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

package com.webcohesion.enunciate.modules.jaxws;

import com.webcohesion.enunciate.api.services.Service;
import com.webcohesion.enunciate.api.services.ServiceGroup;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxws.api.impl.ServiceImpl;
import com.webcohesion.enunciate.modules.jaxws.model.EndpointInterface;

import java.io.File;
import java.util.*;

/**
 * Configuration information about a WSDL.
 *
 * @author Ryan Heaton
 */
public class WsdlInfo implements ServiceGroup {

  private String id;
  private String targetNamespace;
  private final List<EndpointInterface> endpointInterfaces = new ArrayList<EndpointInterface>();
  private final HashMap<String, Object> properties = new HashMap<String, Object>();
  private final EnunciateJaxbContext jaxbContext;
  private File wsdlFile;

  public WsdlInfo(EnunciateJaxbContext jaxbContext) {
    this.jaxbContext = jaxbContext;
  }


  /**
   * A unique id for this wsdl.
   *
   * @return A unique id for this wsdl.
   */
  public String getId() {
    return id;
  }

  /**
   * A unique id for this wsdl.
   *
   * @param id A unique id for this wsdl.
   */
  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getNamespace() {
    return getTargetNamespace();
  }

  @Override
  public File getWsdlFile() {
    return wsdlFile;
  }

  public void setWsdlFile(File wsdlFile) {
    this.wsdlFile = wsdlFile;
  }

  @Override
  public List<? extends Service> getServices() {
    ArrayList<Service> services = new ArrayList<Service>();
    for (EndpointInterface endpointInterface : getEndpointInterfaces()) {
      services.add(new ServiceImpl(endpointInterface));
    }
    Collections.sort(services, new Comparator<Service>() {
      @Override
      public int compare(Service o1, Service o2) {
        return o1.getLabel().compareTo(o2.getLabel());
      }
    });
    return services;
  }

  /**
   * The target namespace.
   *
   * @return The target namespace.
   */
  public String getTargetNamespace() {
    return targetNamespace;
  }

  /**
   * The target namespace.
   *
   * @param targetNamespace The target namespace.
   */
  public void setTargetNamespace(String targetNamespace) {
    this.targetNamespace = targetNamespace;
  }

  /**
   * The endpoint interfaces making up this WSDL.
   *
   * @return The endpoint interfaces making up this WSDL.
   */
  public List<EndpointInterface> getEndpointInterfaces() {
    return endpointInterfaces;
  }

  /**
   * Set a property value.
   *
   * @param property The property.
   * @param value    The value.
   */
  public void setProperty(String property, Object value) {
    this.properties.put(property, value);
  }

  /**
   * Get a property value.
   *
   * @param property The property whose value to retrieve.
   * @return The property value.
   */
  public Object getProperty(String property) {
    return this.properties.get(property);
  }

  /**
   * The properties of the wsdl info.
   *
   * @return The properties of the wsdl info.
   */
  public HashMap<String, Object> getProperties() {
    return properties;
  }

  /**
   * Get the imported namespaces used by this WSDL.
   *
   * @return The imported namespaces used by this WSDL.
   */
  public Set<String> getImportedNamespaces() {
    List<EndpointInterface> endpointInterfaces = getEndpointInterfaces();
    if ((endpointInterfaces == null) || (endpointInterfaces.size() == 0)) {
      throw new IllegalStateException("WSDL for " + getTargetNamespace() + " has no endpoint interfaces!");
    }

    HashSet<String> importedNamespaces = new HashSet<String>();
    //always import the list of known namespaces.
    importedNamespaces.add("http://schemas.xmlsoap.org/wsdl/");
    importedNamespaces.add("http://schemas.xmlsoap.org/wsdl/http/");
    importedNamespaces.add("http://schemas.xmlsoap.org/wsdl/mime/");
    importedNamespaces.add("http://schemas.xmlsoap.org/wsdl/soap/");
    importedNamespaces.add("http://schemas.xmlsoap.org/soap/encoding/");
    importedNamespaces.add("http://www.w3.org/2001/XMLSchema");

    for (EndpointInterface endpointInterface : endpointInterfaces) {
      importedNamespaces.addAll(endpointInterface.getReferencedNamespaces());
    }

    return importedNamespaces;
  }

  /**
   * The list of imported schemas.
   *
   * @return The list of imported schemas.
   */
  public List<SchemaInfo> getImportedSchemas() {
    Set<String> importedNamespaces = getImportedNamespaces();
    importedNamespaces.remove(getTargetNamespace()); //the "associated" schema is either inlined or included, but not imported.
    List<SchemaInfo> schemas = new ArrayList<SchemaInfo>();
    for (String ns : importedNamespaces) {
      SchemaInfo schema = lookupSchema(ns);
      if (schema != null) {
        schemas.add(schema);
      }
    }
    return schemas;
  }

  /**
   * Get the schema associated with this WSDL.
   *
   * @return The schema associated with this WSDL.
   */
  public SchemaInfo getAssociatedSchema() {
    return lookupSchema(getTargetNamespace());
  }

  /**
   * Convenience method to lookup a namespace schema given a namespace.
   *
   * @param namespace The namespace for which to lookup the schema.
   * @return The schema info.
   */
  protected SchemaInfo lookupSchema(String namespace) {
    if ("".equals(namespace)) {
      namespace = null;
    }

    return this.jaxbContext.getSchemas().get(namespace);
  }

}
