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

package org.codehaus.enunciate.config;

import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.MapXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlClassType;
import org.codehaus.enunciate.contract.jaxb.ImplicitSchemaElement;
import org.codehaus.enunciate.contract.jaxb.ImplicitRootElement;
import org.codehaus.enunciate.contract.jaxb.ImplicitChildElement;
import org.codehaus.enunciate.util.TypeDeclarationComparator;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Configuration information about a schema.
 *
 * @author Ryan Heaton
 */
public class SchemaInfo {

  private String id;
  private String namespace;
  private final Collection<ImplicitSchemaElement> implicitSchemaElements = new TreeSet<ImplicitSchemaElement>(new ImplicitSchemaElementComparator());
  private final Collection<ImplicitSchemaAttribute> implicitSchemaAttributes = new TreeSet<ImplicitSchemaAttribute>(new ImplicitSchemaAttributeComparator());
  private final Collection<TypeDefinition> typeDefinitions = new TreeSet<TypeDefinition>(new TypeDeclarationComparator());
  private final Collection<RootElementDeclaration> globalElements = new TreeSet<RootElementDeclaration>(new TypeDeclarationComparator());
  private final Collection<Registry> registries = new ArrayList<Registry>();
  private final Collection<LocalElementDeclaration> localElementDeclarations = new ArrayList<LocalElementDeclaration>();
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
   * A unique id for this schema.
   *
   * @return A unique id for this schema.
   */
  public String getId() {
    return id;
  }

  /**
   * A unique id for this schema.
   *
   * @param id A unique id for this schema.
   */
  public void setId(String id) {
    this.id = id;
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
   * Get the implicit schema elements to be included in this schema.
   *
   * @return The implicit schema elements to be included in this schema.
   */
  public Collection<ImplicitSchemaElement> getImplicitSchemaElements() {
    return implicitSchemaElements;
  }

  /**
   * Get the implicit schema attributes to be included in this schema.
   *
   * @return The implicit schema attributes to be included in this schema.
   */
  public Collection<ImplicitSchemaAttribute> getImplicitSchemaAttributes() {
    return implicitSchemaAttributes;
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
   * The XML registries.
   *
   * @return The XML registries.
   */
  public Collection<Registry> getRegistries() {
    return registries;
  }

  /**
   * Local element declarations.
   *
   * @return Local element declarations.
   */
  public Collection<LocalElementDeclaration> getLocalElementDeclarations() {
    return localElementDeclarations;
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
      addReferencedNamespaces(typeDefinition, referencedNamespaces);
    }

    for (RootElementDeclaration rootElement : getGlobalElements()) {
      referencedNamespaces.add(rootElement.getNamespace());
      referencedNamespaces.add(rootElement.getTypeDefinition().getNamespace());
    }

    for (ImplicitSchemaElement schemaElement : implicitSchemaElements) {
      QName typeQName = schemaElement.getTypeQName();
      if (typeQName != null) {
        referencedNamespaces.add(typeQName.getNamespaceURI());
      }

      if (schemaElement instanceof ImplicitRootElement) {
        for (ImplicitChildElement childElement : ((ImplicitRootElement) schemaElement).getChildElements()) {
          addReferencedNamespaces(childElement.getXmlType(), referencedNamespaces);
        }
      }
    }

    for (ImplicitSchemaAttribute schemaAttribute : implicitSchemaAttributes) {
      QName typeQName = schemaAttribute.getTypeQName();
      if (typeQName != null) {
        referencedNamespaces.add(typeQName.getNamespaceURI());
      }
    }

    for (LocalElementDeclaration localElementDeclaration : localElementDeclarations) {
      QName typeQName = localElementDeclaration.getElementXmlType().getQname();
      if (typeQName != null) {
        referencedNamespaces.add(typeQName.getNamespaceURI());
      }
    }

    referencedNamespaces.add(getNamespace());
    //remove the obvious referenced namespace.
    referencedNamespaces.remove("http://www.w3.org/2001/XMLSchema");

    //consolidate the "" and the null:
    if (referencedNamespaces.remove(null)) {
      referencedNamespaces.add("");
    }

    return referencedNamespaces;
  }

  /**
   * Adds the referenced namespaces of the given type definition to the given set.
   *
   * @param typeDefinition The type definition.
   * @param referencedNamespaces The set of referenced namespaces.
   */
  private void addReferencedNamespaces(TypeDefinition typeDefinition, Set<String> referencedNamespaces) {
    for (Attribute attribute : typeDefinition.getAttributes()) {
      QName ref = attribute.getRef();
      if (ref != null) {
        referencedNamespaces.add(ref.getNamespaceURI());
      }
      else {
        addReferencedNamespaces(attribute.getBaseType(), referencedNamespaces);
      }
    }

    for (Element element : typeDefinition.getElements()) {
      for (Element choice : element.getChoices()) {
        QName ref = choice.getRef();
        if (ref != null) {
          referencedNamespaces.add(ref.getNamespaceURI());
        }
        else {
          addReferencedNamespaces(choice.getBaseType(), referencedNamespaces);
        }
      }
    }

    Value value = typeDefinition.getValue();
    if (value != null) {
      addReferencedNamespaces(value.getBaseType(), referencedNamespaces);
    }

    addReferencedNamespaces(typeDefinition.getBaseType(), referencedNamespaces);
  }

  /**
   * Adds the referenced namespaces of the given xml type to the given set.
   *
   * @param xmlType The xml type.
   * @param referencedNamespaces The set of referenced namespaces.
   */
  private void addReferencedNamespaces(XmlType xmlType, Set<String> referencedNamespaces) {
    if (!xmlType.isAnonymous()) {
      referencedNamespaces.add(xmlType.getNamespace());
    }
    else if (xmlType instanceof MapXmlType) {
      referencedNamespaces.add(((MapXmlType) xmlType).getKeyType().getNamespace());
      referencedNamespaces.add(((MapXmlType) xmlType).getValueType().getNamespace());
    }
    else if (xmlType instanceof XmlClassType) {
      addReferencedNamespaces(((XmlClassType) xmlType).getTypeDefinition(), referencedNamespaces);
    }
  }

  /**
   * The list of imported schemas.
   *
   * @return The list of imported schemas.
   */
  public List<SchemaInfo> getImportedSchemas() {
    Set<String> importedNamespaces = getReferencedNamespaces();
    importedNamespaces.remove(getNamespace() == null ? "" : getNamespace());
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

  /**
   * Compares implicit elements by element name.
   */
  private static class ImplicitSchemaElementComparator implements Comparator<ImplicitSchemaElement> {

    /**
     * @param element1 The first element.
     * @param element2 The second element.
     * @return The comparison of the element names.
     */
    public int compare(ImplicitSchemaElement element1, ImplicitSchemaElement element2) {
      return element1.getElementName().compareTo(element2.getElementName());
    }
  }

  /**
   * Compares implicit attributes by attribute name.
   */
  private static class ImplicitSchemaAttributeComparator implements Comparator<ImplicitSchemaAttribute> {

    /**
     * @param attribute1 The first attribute.
     * @param attribute2 The second attribute.
     * @return The comparison of the attribute names.
     */
    public int compare(ImplicitSchemaAttribute attribute1, ImplicitSchemaAttribute attribute2) {
      return attribute1.getAttributeName().compareTo(attribute2.getAttributeName());
    }
  }
}
