package net.sf.enunciate.config;

import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.*;
import net.sf.enunciate.util.QName;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.util.*;

/**
 * Configuration information about a schema.
 *
 * @author Ryan Heaton
 */
public class SchemaInfo {

  private String namespace;
  private boolean generate;
  private String file;
  private String location;
  private String elementFormDefault;
  private String attributeFormDefault;
  private final Collection<TypeDefinition> typeDefinitions = new ArrayList<TypeDefinition>();
  private final Collection<RootElementDeclaration> globalElements = new ArrayList<RootElementDeclaration>();

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
   * Whether or not to generate this schema.
   *
   * @return Whether or not to generate this schema.
   */
  public boolean isGenerate() {
    return generate;
  }

  /**
   * Whether or not to generate this schema.
   *
   * @param generate Whether or not to generate this schema.
   */
  public void setGenerate(boolean generate) {
    this.generate = generate;
  }

  /**
   * The file to which to write this schema.
   *
   * @return The file to which to write this schema.
   */
  public String getFile() {
    return file;
  }

  /**
   * The file to which to write this schema.
   *
   * @param file The file to which to write this schema.
   */
  public void setFile(String file) {
    this.file = file;
  }

  /**
   * The schema location.
   *
   * @return The schema location.
   */
  public String getLocation() {
    return location;
  }

  /**
   * The schema location.
   *
   * @param location The schema location.
   */
  public void setLocation(String location) {
    this.location = location;
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
   * The elementFormDefault for this schema.
   *
   * @return The elementFormDefault for this schema.
   */
  public String getElementFormDefault() {
    return elementFormDefault;
  }

  /**
   * The elementFormDefault for this schema.
   *
   * @param elementFormDefault The elementFormDefault for this schema.
   */
  public void setElementFormDefault(String elementFormDefault) {
    this.elementFormDefault = elementFormDefault;
  }

  /**
   * The attributeFormDefault for this schema.
   *
   * @return The attributeFormDefault for this schema.
   */
  public String getAttributeFormDefault() {
    return attributeFormDefault;
  }

  /**
   * The attributeFormDefault for this schema.
   *
   * @param attributeFormDefault The attributeFormDefault for this schema.
   */
  public void setAttributeFormDefault(String attributeFormDefault) {
    this.attributeFormDefault = attributeFormDefault;
  }

  /**
   * The imported namespace of a specific schema.
   *
   * @return The imported namespace of a specific schema.
   */
  public Set<String> getReferencedNamespaces() {
    Set<String> referencedNamspaces = new HashSet<String>();

    for (TypeDefinition typeDefinition : getTypeDefinitions()) {
      for (Attribute attribute : typeDefinition.getAttributes()) {
        referencedNamspaces.add(attribute.getNamespace());
        referencedNamspaces.add(attribute.getBaseType().getNamespace());
        QName ref = attribute.getRef();
        if (ref != null) {
          referencedNamspaces.add(ref.getNamespaceURI());
        }
      }

      for (Element element : typeDefinition.getElements()) {
        referencedNamspaces.add(element.getNamespace());
        referencedNamspaces.add(element.getBaseType().getNamespace());
        QName ref = element.getRef();
        if (ref != null) {
          referencedNamspaces.add(ref.getNamespaceURI());
        }
      }

      Value value = typeDefinition.getValue();
      if (value != null) {
        referencedNamspaces.add(value.getBaseType().getNamespace());
      }

      referencedNamspaces.add(typeDefinition.getBaseType().getNamespace());
    }

    for (RootElementDeclaration rootElement : getGlobalElements()) {
      referencedNamspaces.add(rootElement.getTargetNamespace());
      referencedNamspaces.add(rootElement.getTypeDefinition().getTargetNamespace());
    }

    //remove the obvious referenced namespace.
    referencedNamspaces.remove("http://www.w3.org/2001/XMLSchema");

    return referencedNamspaces;
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
        // if it's not a schema we know about, we're going to leave the schemaLocation empty, giving no hints as
        // to where to find the definitions of referenced components.  Not illegal according to XML-Schema, but
        // probably not a good idea.
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
