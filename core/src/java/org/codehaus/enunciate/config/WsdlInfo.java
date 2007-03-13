package org.codehaus.enunciate.config;

import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.util.*;

/**
 * Configuration information about a WSDL.
 *
 * @author Ryan Heaton
 */
public class WsdlInfo {

  private String id;
  private String targetNamespace;
  private final Collection<EndpointInterface> endpointInterfaces = new ArrayList<EndpointInterface>();
  private final HashMap<String, Object> properties = new HashMap<String, Object>();

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
  public Collection<EndpointInterface> getEndpointInterfaces() {
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
    Collection<EndpointInterface> endpointInterfaces = getEndpointInterfaces();
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
   * Convenience method to lookup a namespace schema given a namespace.
   *
   * @param namespace The namespace for which to lookup the schema.
   * @return The schema info.
   */
  protected SchemaInfo lookupSchema(String namespace) {
    if ("".equals(namespace)) {
      namespace = null;
    }

    return getNamespacesToSchemas().get(namespace);
  }

  /**
   * The namespace to schema map.
   *
   * @return The namespace to schema map.
   */
  protected Map<String, SchemaInfo> getNamespacesToSchemas() {
    return getModel().getNamespacesToSchemas();
  }

  /**
   * Get the current root model.
   *
   * @return The current root model.
   */
  protected EnunciateFreemarkerModel getModel() {
    return ((EnunciateFreemarkerModel) FreemarkerModel.get());
  }

}
