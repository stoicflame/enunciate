/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jaxws;

import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.javac.TypeElementComparator;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxws.model.EndpointInterface;
import com.webcohesion.enunciate.modules.jaxws.model.WebFault;
import com.webcohesion.enunciate.modules.jaxws.model.WebMessage;
import com.webcohesion.enunciate.modules.jaxws.model.WebMethod;

import java.util.*;

/**
 * Configuration information about a WSDL.
 *
 * @author Ryan Heaton
 */
public class WsdlInfo {

  private String id;
  private String targetNamespace;
  private String filename;
  private boolean inlineSchema;
  private final Set<EndpointInterface> endpointInterfaces = new TreeSet<EndpointInterface>(new TypeElementComparator());
  private final EnunciateJaxbContext jaxbContext;
  private InterfaceDescriptionFile wsdlFile;

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

  public EnunciateJaxbContext getContext() {
    return jaxbContext;
  }

  public InterfaceDescriptionFile getWsdlFile() {
    return wsdlFile;
  }

  public void setWsdlFile(InterfaceDescriptionFile wsdlFile) {
    this.wsdlFile = wsdlFile;
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

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public boolean isInlineSchema() {
    return inlineSchema;
  }

  public void setInlineSchema(boolean inlineSchema) {
    this.inlineSchema = inlineSchema;
  }

  public List<WebMessage> getWebMessages() {
    ArrayList<WebMessage> messages = new ArrayList<WebMessage>();
    HashSet<String> foundFaults = new HashSet<String>();
    for (EndpointInterface ei : getEndpointInterfaces()) {
      Collection<WebMethod> webMethods = ei.getWebMethods();
      for (WebMethod method : webMethods) {
        for (WebMessage webMessage : method.getMessages()) {
          if (webMessage.isFault() && !foundFaults.add(((WebFault) webMessage).getQualifiedName().toString())) {
            continue;
          }

          messages.add(webMessage);
        }
      }

    }
    return messages;
  }

  /**
   * The endpoint interfaces making up this WSDL.
   *
   * @return The endpoint interfaces making up this WSDL.
   */
  public Set<EndpointInterface> getEndpointInterfaces() {
    return endpointInterfaces;
  }

  /**
   * Get the imported namespaces used by this WSDL.
   *
   * @return The imported namespaces used by this WSDL.
   */
  public Set<String> getImportedNamespaces() {
    Set<EndpointInterface> endpointInterfaces = getEndpointInterfaces();
    if ((endpointInterfaces == null) || (endpointInterfaces.size() == 0)) {
      throw new IllegalStateException("WSDL for " + getTargetNamespace() + " has no endpoint interfaces!");
    }

    HashSet<String> importedNamespaces = new HashSet<String>();
    //always import the list of known namespaces.
    importedNamespaces.add("http://schemas.xmlsoap.org/wsdl/");
    importedNamespaces.add("http://schemas.xmlsoap.org/wsdl/http/");
    importedNamespaces.add("http://schemas.xmlsoap.org/wsdl/mime/");
    importedNamespaces.add("http://schemas.xmlsoap.org/wsdl/soap/");
    importedNamespaces.add("http://schemas.xmlsoap.org/wsdl/soap12/");
    importedNamespaces.add("http://schemas.xmlsoap.org/soap/encoding/");
    importedNamespaces.add("http://www.w3.org/2001/XMLSchema");

    for (EndpointInterface endpointInterface : endpointInterfaces) {
      importedNamespaces.addAll(endpointInterface.getReferencedNamespaces());
    }

    if (isInlineSchema()) {
      SchemaInfo associatedSchema = getAssociatedSchema();
      if (associatedSchema != null) {
        importedNamespaces.addAll(associatedSchema.getReferencedNamespaces());
      }
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
