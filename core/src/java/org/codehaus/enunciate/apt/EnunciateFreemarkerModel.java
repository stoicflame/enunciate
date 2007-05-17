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

package org.codehaus.enunciate.apt;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.Schema;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.rest.RESTEndpoint;
import org.codehaus.enunciate.contract.rest.RESTMethod;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.util.ClassDeclarationComparator;

import javax.xml.bind.annotation.XmlNsForm;
import java.io.File;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class EnunciateFreemarkerModel extends FreemarkerModel {

  private static final Comparator<ClassDeclaration> CLASS_COMPARATOR = new ClassDeclarationComparator();

  int prefixIndex = 0;
  final Map<String, String> namespacesToPrefixes;
  final Map<String, SchemaInfo> namespacesToSchemas;
  final Map<String, WsdlInfo> namespacesToWsdls;
  final Map<String, XmlType> knownTypes;
  final Map<String, List<RESTMethod>> nounsToRESTMethods;
  final List<TypeDefinition> typeDefinitions = new ArrayList<TypeDefinition>();
  final List<RootElementDeclaration> rootElements = new ArrayList<RootElementDeclaration>();
  final List<EndpointInterface> endpointInterfaces = new ArrayList<EndpointInterface>();
  final List<RESTEndpoint> restEndpoints = new ArrayList<RESTEndpoint>();
  private File fileOutputDirectory = null;

  public EnunciateFreemarkerModel() {
    this.namespacesToPrefixes = loadKnownNamespaces();
    this.knownTypes = loadKnownTypes();
    this.namespacesToSchemas = new HashMap<String, SchemaInfo>();
    this.namespacesToWsdls = new HashMap<String, WsdlInfo>();
    this.nounsToRESTMethods = new HashMap<String, List<RESTMethod>>();

    setVariable("knownNamespaces", new ArrayList<String>(this.namespacesToPrefixes.keySet()));
    setVariable("ns2prefix", this.namespacesToPrefixes);
    setVariable("ns2schema", this.namespacesToSchemas);
    setVariable("ns2wsdl", this.namespacesToWsdls);
    setVariable("nouns2methods", this.nounsToRESTMethods);
    setVariable("restEndpoints", this.restEndpoints);
  }

  /**
   * Loads a map of known namespaces as keys to their associated prefixes.
   *
   * @return A map of known namespaces.
   */
  protected Map<String, String> loadKnownNamespaces() {
    HashMap<String, String> knownNamespaces = new HashMap<String, String>();

    knownNamespaces.put("http://schemas.xmlsoap.org/wsdl/", "wsdl");
    knownNamespaces.put("http://schemas.xmlsoap.org/wsdl/http/", "http");
    knownNamespaces.put("http://schemas.xmlsoap.org/wsdl/mime/", "mime");
    knownNamespaces.put("http://schemas.xmlsoap.org/wsdl/soap/", "soap");
    knownNamespaces.put("http://schemas.xmlsoap.org/soap/encoding/", "soapenc");
    knownNamespaces.put("http://www.w3.org/2001/XMLSchema", "xs");
    knownNamespaces.put("http://ws-i.org/profiles/basic/1.1/xsd", "wsi");

    return knownNamespaces;
  }

  /**
   * Loads the known types, keyed off the Java fqn.
   *
   * @return The map of known types, keyed off the Java fqn.
   */
  protected Map<String, XmlType> loadKnownTypes() {
    HashMap<String, XmlType> knownTypes = new HashMap<String, XmlType>();

    knownTypes.put(Boolean.class.getName(), KnownXmlType.BOOLEAN);
    knownTypes.put(Byte.class.getName(), KnownXmlType.BYTE);
    knownTypes.put(Double.class.getName(), KnownXmlType.DOUBLE);
    knownTypes.put(Float.class.getName(), KnownXmlType.FLOAT);
    knownTypes.put(Integer.class.getName(), KnownXmlType.INT);
    knownTypes.put(Long.class.getName(), KnownXmlType.LONG);
    knownTypes.put(Short.class.getName(), KnownXmlType.SHORT);
    knownTypes.put(Boolean.TYPE.getName(), KnownXmlType.BOOLEAN);
    knownTypes.put(Byte.TYPE.getName(), KnownXmlType.BYTE);
    knownTypes.put(Double.TYPE.getName(), KnownXmlType.DOUBLE);
    knownTypes.put(Float.TYPE.getName(), KnownXmlType.FLOAT);
    knownTypes.put(Integer.TYPE.getName(), KnownXmlType.INT);
    knownTypes.put(Long.TYPE.getName(), KnownXmlType.LONG);
    knownTypes.put(Short.TYPE.getName(), KnownXmlType.SHORT);
    knownTypes.put(String.class.getName(), KnownXmlType.STRING);
    knownTypes.put(java.math.BigInteger.class.getName(), KnownXmlType.INTEGER);
    knownTypes.put(java.math.BigDecimal.class.getName(), KnownXmlType.DECIMAL);
    knownTypes.put(java.util.Calendar.class.getName(), KnownXmlType.DATE_TIME);
    knownTypes.put(java.util.Date.class.getName(), KnownXmlType.DATE_TIME);
    knownTypes.put(javax.xml.namespace.QName.class.getName(), KnownXmlType.QNAME);
    knownTypes.put(java.net.URI.class.getName(), KnownXmlType.STRING);
    knownTypes.put(javax.xml.datatype.XMLGregorianCalendar.class.getName(), KnownXmlType.ANY_SIMPLE_TYPE);
    knownTypes.put(javax.xml.datatype.Duration.class.getName(), KnownXmlType.DURATION);
    knownTypes.put(java.lang.Object.class.getName(), KnownXmlType.ANY_TYPE);
    knownTypes.put(byte[].class.getName(), KnownXmlType.BASE64_BINARY);
    knownTypes.put(java.awt.Image.class.getName(), KnownXmlType.BASE64_BINARY);
    knownTypes.put("javax.activation.DataHandler", KnownXmlType.BASE64_BINARY);
    knownTypes.put(javax.xml.transform.Source.class.getName(), KnownXmlType.BASE64_BINARY);
    knownTypes.put(java.util.UUID.class.getName(), KnownXmlType.STRING);

    return knownTypes;
  }

  /**
   * A map of namespace URIs to their associated prefixes.
   *
   * @return A map of namespace URIs to their associated prefixes.
   */
  public Map<String, String> getNamespacesToPrefixes() {
    return namespacesToPrefixes;
  }

  /**
   * A map of namespace URIs to their associated schema information.
   *
   * @return A map of namespace URIs to their associated schema information.
   */
  public Map<String, SchemaInfo> getNamespacesToSchemas() {
    return namespacesToSchemas;
  }

  /**
   * A map of namespace URIs to their associated WSDL information.
   *
   * @return A map of namespace URIs to their associated WSDL information.
   */
  public Map<String, WsdlInfo> getNamespacesToWSDLs() {
    return namespacesToWsdls;
  }

  /**
   * The map of nouns to REST endpoints.
   *
   * @return The map of nouns to REST endpoints.
   */
  public Map<String, List<RESTMethod>> getNounsToRESTMethods() {
    return nounsToRESTMethods;
  }

  /**
   * The list of REST endpoints in the model.
   *
   * @return The list of REST endpoints in the model.
   */
  public List<RESTEndpoint> getRESTEndpoints() {
    return restEndpoints;
  }

  /**
   * Add an endpoint interface to the model.
   *
   * @param ei The endpoint interface to add to the model.
   */
  public void add(EndpointInterface ei) {
    String namespace = ei.getTargetNamespace();

    String prefix = addNamespace(namespace);

    WsdlInfo wsdlInfo = namespacesToWsdls.get(namespace);
    if (wsdlInfo == null) {
      wsdlInfo = new WsdlInfo();
      wsdlInfo.setId(prefix);
      namespacesToWsdls.put(namespace, wsdlInfo);
      wsdlInfo.setTargetNamespace(namespace);
    }

    wsdlInfo.getEndpointInterfaces().add(ei);
    this.endpointInterfaces.add(ei);
  }

  /**
   * Add a type definition to the model.
   *
   * @param typeDef The type definition to add to the model.
   */
  public void add(TypeDefinition typeDef) {
    add(typeDef.getSchema());

    String namespace = typeDef.getNamespace();
    String prefix = addNamespace(namespace);

    SchemaInfo schemaInfo = namespacesToSchemas.get(namespace);
    if (schemaInfo == null) {
      schemaInfo = new SchemaInfo();
      schemaInfo.setId(prefix);
      schemaInfo.setNamespace(namespace);
      namespacesToSchemas.put(namespace, schemaInfo);
    }
    schemaInfo.getTypeDefinitions().add(typeDef);

    int position = Collections.binarySearch(this.typeDefinitions, typeDef, CLASS_COMPARATOR);
    if (position < 0) {
      this.typeDefinitions.add(-position - 1, typeDef);
    }
  }

  /**
   * Adds a REST endpoint to the model.
   *
   * @param endpoint The REST endpoint to add.
   */
  public void add(RESTEndpoint endpoint) {
    for (RESTMethod restMethod : endpoint.getRESTMethods()) {
      String noun = restMethod.getNoun();
      List<RESTMethod> restMethods = this.nounsToRESTMethods.get(noun);
      if (restMethods == null) {
        restMethods = new ArrayList<RESTMethod>();
        this.nounsToRESTMethods.put(noun, restMethods);
      }
      restMethods.add(restMethod);
    }

    this.restEndpoints.add(endpoint);
  }

  /**
   * Add a root element to the model.
   *
   * @param rootElement The root element to add.
   */
  public void add(RootElementDeclaration rootElement) {
    add(rootElement.getSchema());

    String namespace = rootElement.getNamespace();
    String prefix = addNamespace(namespace);

    SchemaInfo schemaInfo = namespacesToSchemas.get(namespace);
    if (schemaInfo == null) {
      schemaInfo = new SchemaInfo();
      schemaInfo.setId(prefix);
      schemaInfo.setNamespace(namespace);
      namespacesToSchemas.put(namespace, schemaInfo);
    }
    schemaInfo.getGlobalElements().add(rootElement);

    int position = Collections.binarySearch(this.rootElements, rootElement, CLASS_COMPARATOR);
    if (position < 0) {
      this.rootElements.add(-position - 1, rootElement);
    }
  }

  /**
   * Adds a schema declaration to the model.
   *
   * @param schema The schema declaration to add to the model.
   */
  public void add(Schema schema) {
    String namespace = schema.getNamespace();
    String prefix = addNamespace(namespace);
    this.namespacesToPrefixes.putAll(schema.getSpecifiedNamespacePrefixes());
    SchemaInfo schemaInfo = namespacesToSchemas.get(namespace);
    if (schemaInfo == null) {
      schemaInfo = new SchemaInfo();
      schemaInfo.setId(prefix);
      schemaInfo.setNamespace(namespace);
      namespacesToSchemas.put(namespace, schemaInfo);
    }

    if (schema.getElementFormDefault() != XmlNsForm.UNSET) {
      for (Schema pckg : schemaInfo.getPackages()) {
        if ((pckg.getElementFormDefault() != null) && (schema.getElementFormDefault() != pckg.getElementFormDefault())) {
          throw new ValidationException(schema.getPosition(), "Inconsistent elementFormDefault declarations: " + pckg.getPosition());
        }
      }
    }

    if (schema.getAttributeFormDefault() != XmlNsForm.UNSET) {
      for (Schema pckg : schemaInfo.getPackages()) {
        if ((pckg.getAttributeFormDefault() != null) && (schema.getAttributeFormDefault() != pckg.getAttributeFormDefault())) {
          throw new ValidationException(schema.getPosition(), "Inconsistent attributeFormDefault declarations: " + pckg.getPosition());
        }
      }
    }

    schemaInfo.getPackages().add(schema);
  }

  /**
   * Add a namespace.
   *
   * @param namespace The namespace to add.
   * @return The prefix for the namespace.
   */
  public String addNamespace(String namespace) {
    String prefix = namespacesToPrefixes.get(namespace);
    if (prefix == null) {
      prefix = generatePrefix(namespace);
      namespacesToPrefixes.put(namespace, prefix);
    }
    return prefix;
  }

  /**
   * Generate a prefix for the given namespace.
   *
   * @param namespace The namespace for which to generate a prefix.
   * @return The prefix that was generated.
   */
  protected String generatePrefix(String namespace) {
    String prefix = "ns" + (prefixIndex++);
    while (this.namespacesToPrefixes.values().contains(prefix)) {
      prefix = "ns" + (prefixIndex++);
    }
    return prefix;
  }

  /**
   * Gets the known type for the given declared type.
   *
   * @param declaredType The declared type.
   * @return The known type for the given declared type, or null if the declared type is not known.
   */
  public XmlType getKnownType(DeclaredType declaredType) {
    XmlType knownType = null;
    TypeDeclaration declaration = declaredType.getDeclaration();
    if (declaration != null) {
      if (knownTypes.containsKey(declaration.getQualifiedName())) {
        //first check the known types.
        knownType = getKnownType(declaration);
      }
    }

    return knownType;
  }

  /**
   * Gets the known type for the given declaration.
   *
   * @param declaration The declaration.
   * @return The known type for the given declaration, or null if the XML type of the declaration is not known.
   */
  public XmlType getKnownType(TypeDeclaration declaration) {
    return knownTypes.get(declaration.getQualifiedName());
  }

  /**
   * Find the type definition for a class given the class's declaration, or null if the class hasn't been added to the model.
   *
   * @param declaration The declaration.
   * @return The type definition.
   */
  public TypeDefinition findTypeDefinition(ClassDeclaration declaration) {
    int index = Collections.binarySearch(this.typeDefinitions, declaration, CLASS_COMPARATOR);
    if (index >= 0) {
      return this.typeDefinitions.get(index);
    }

    return null;
  }

  /**
   * Find the root element declaration for the specified class.
   *
   * @param declaration The class declaration
   * @return The root element declaration, or null if the declaration hasn't been added to the model.
   */
  public RootElementDeclaration findRootElementDeclaration(ClassDeclaration declaration) {
    int index = Collections.binarySearch(this.rootElements, declaration, CLASS_COMPARATOR);
    if (index >= 0) {
      return this.rootElements.get(index);
    }
    return null;
  }

  /**
   * The list of root element declarations found in the model.
   *
   * @return The list of root element declarations found in the model.
   */
  public List<RootElementDeclaration> getRootElementDeclarations() {
    return rootElements;
  }

  /**
   * The file output directory.
   *
   * @return The file output directory.
   */
  public File getFileOutputDirectory() {
    return fileOutputDirectory;
  }

  /**
   * The file output directory.
   *
   * @param fileOutputDirectory The file output directory.
   */
  public void setFileOutputDirectory(File fileOutputDirectory) {
    this.fileOutputDirectory = fileOutputDirectory;
  }
}
