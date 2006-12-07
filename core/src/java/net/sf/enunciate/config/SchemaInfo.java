package net.sf.enunciate.config;

import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.*;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Configuration information about a schema.
 *
 * @author Ryan Heaton
 */
public class SchemaInfo {

  private String namespace;
  private final Collection<TypeDefinition> typeDefinitions = new ArrayList<TypeDefinition>();
  private final Collection<RootElementDeclaration> globalElements = new ArrayList<RootElementDeclaration>();
  private final TreeSet<Schema> packages = new TreeSet<Schema>();
  private final HashMap<String, Object> properties = new HashMap<String, Object>();

  /**
   * Whether this is the schema for the empty namespace.
   *
   * @return Whether this is the schema for the empty namespace.
   */
  public boolean isEmptyNamespace() {
    return ((getNamespace() == null) || "".equals(getNamespace()));
  }

  /**
   * The target namespace.
   *
   * @return The target namespace.
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * The target namespace.
   *
   * @param namespace The target namespace.
   */
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   * The collection of types defined in this schema.
   *
   * @return The collection of types defined in this schema.
   */
  public Collection<TypeDefinition> getTypeDefinitions() {
    return typeDefinitions;
  }

  /**
   * The collection of global elements defined in this schema.
   *
   * @return The collection of global elements defined in this schema.
   */
  public Collection<RootElementDeclaration> getGlobalElements() {
    return globalElements;
  }

  /**
   * The set of packages that make up this schema info.
   *
   * @return The set of packages that make up this schema info.
   */
  public Set<Schema> getPackages() {
    return this.packages;
  }

  /**
   * The elementFormDefault for this schema.
   *
   * @return The elementFormDefault for this schema.
   */
  public String getElementFormDefault() {
    for (Schema pckg : getPackages()) {
      if (pckg.getElementFormDefault() != null) {
        return pckg.getElementFormDefault().toString().toLowerCase();
      }
    }
    
    return null;
  }

  /**
   * The attributeFormDefault for this schema.
   *
   * @return The attributeFormDefault for this schema.
   */
  public String getAttributeFormDefault() {
    for (Schema pckg : getPackages()) {
      if (pckg.getAttributeFormDefault() != null) {
        return pckg.getAttributeFormDefault().toString().toLowerCase();
      }
    }

    return null;
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
   * The properties for the schema.
   *
   * @return The properties for the schema.
   */
  public HashMap<String, Object> getProperties() {
    return properties;
  }

  /**
   * The imported namespace of a specific schema.
   *
   * @return The imported namespace of a specific schema.
   */
  public Set<String> getReferencedNamespaces() {
    Set<String> referencedNamespaces = new HashSet<String>();

    for (TypeDefinition typeDefinition : getTypeDefinitions()) {
      for (Attribute attribute : typeDefinition.getAttributes()) {
        QName ref = attribute.getRef();
        if (ref != null) {
          referencedNamespaces.add(ref.getNamespaceURI());
        }
        else {
          referencedNamespaces.add(attribute.getBaseType().getNamespace());
        }
      }

      for (Element element : typeDefinition.getElements()) {
        QName ref = element.getRef();
        if (ref != null) {
          referencedNamespaces.add(ref.getNamespaceURI());
        }
        else {
          referencedNamespaces.add(element.getBaseType().getNamespace());
        }
      }

      Value value = typeDefinition.getValue();
      if (value != null) {
        referencedNamespaces.add(value.getBaseType().getNamespace());
      }

      referencedNamespaces.add(typeDefinition.getBaseType().getNamespace());
    }

    for (RootElementDeclaration rootElement : getGlobalElements()) {
      referencedNamespaces.add(rootElement.getNamespace());
      referencedNamespaces.add(rootElement.getTypeDefinition().getNamespace());
    }

    //remove the obvious referenced namespace.
    referencedNamespaces.remove("http://www.w3.org/2001/XMLSchema");

    return referencedNamespaces;
  }

  /**
   * The list of imported schemas.
   *
   * @return The list of imported schemas.
   */
  public List<SchemaInfo> getImportedSchemas() {
    Set<String> importedNamespaces = getReferencedNamespaces();
    importedNamespaces.remove(getNamespace());
    List<SchemaInfo> schemas = new ArrayList<SchemaInfo>();
    for (String ns : importedNamespaces) {
      SchemaInfo schema = lookupSchema(ns);
      if (schema != null) {
        schemas.add(schema);
      }
      else {
        SchemaInfo schemaInfo = new SchemaInfo();
        schemaInfo.setNamespace(ns);
        schemas.add(schemaInfo);
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
