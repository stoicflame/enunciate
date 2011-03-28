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

package org.codehaus.enunciate.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;
import com.sun.mirror.util.TypeVisitor;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedClassType;
import net.sf.jelly.apt.decorations.type.DecoratedInterfaceType;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import org.codehaus.enunciate.contract.common.rest.RESTResource;
import org.codehaus.enunciate.contract.common.rest.RESTResourcePayload;
import org.codehaus.enunciate.contract.common.rest.ResourcePayloadTypeAdapter;
import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterType;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterUtil;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxrs.ResourceEntityParameter;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.contract.json.*;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.doc.DocumentationExample;
import org.codehaus.enunciate.json.JsonRootType;
import org.codehaus.enunciate.json.JsonTypeMapping;
import org.codehaus.enunciate.json.JsonTypeMappings;
import org.codehaus.enunciate.rest.MimeType;
import org.codehaus.enunciate.util.MapType;
import org.codehaus.enunciate.util.MapTypeUtil;
import org.codehaus.enunciate.util.TypeDeclarationComparator;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class EnunciateFreemarkerModel extends FreemarkerModel {

  private static final ThreadLocal<LinkedList<String>> REFERENCE_STACK = new ThreadLocal<LinkedList<String>>() {
    @Override
    protected LinkedList<String> initialValue() {
      return new LinkedList<String>();
    }
  };
  
  private static final Comparator<TypeDeclaration> CLASS_COMPARATOR = new TypeDeclarationComparator();

  int prefixIndex = 0;
  final Map<String, String> namespacesToPrefixes;
  final Map<String, String> contentTypesToIds;
  final Map<String, SchemaInfo> namespacesToSchemas;
  final Map<String, WsdlInfo> namespacesToWsdls;
  final Map<String, XmlType> knownTypes;
  final Map<String, JsonType> knownJsonTypes;
  final List<TypeDefinition> typeDefinitions = new ArrayList<TypeDefinition>();
  final List<RootElementDeclaration> rootElements = new ArrayList<RootElementDeclaration>();
  final List<EndpointInterface> endpointInterfaces = new ArrayList<EndpointInterface>();
  final List<RootResource> rootResources = new ArrayList<RootResource>();
  final List<TypeDeclaration> jaxrsProviders = new ArrayList<TypeDeclaration>();
  private File fileOutputDirectory = null;
  private String baseDeploymentAddress = null;
  private EnunciateConfiguration enunciateConfig = null;
  final Map<String, JsonSchemaInfo> idsToJsonSchemas;
  private File wadlFile = null;

  public EnunciateFreemarkerModel() {
    this.namespacesToPrefixes = loadKnownNamespaces();
    this.contentTypesToIds = loadKnownContentTypes();
    this.knownTypes = loadKnownTypes();
    this.knownJsonTypes = loadKnownJsonTypes();
    this.namespacesToSchemas = new HashMap<String, SchemaInfo>();
    this.namespacesToWsdls = new HashMap<String, WsdlInfo>();
    this.idsToJsonSchemas = new HashMap<String, JsonSchemaInfo>();

    setVariable("knownNamespaces", new ArrayList<String>(this.namespacesToPrefixes.keySet()));
    setVariable("ns2prefix", this.namespacesToPrefixes);
    setVariable("ns2schema", this.namespacesToSchemas);
    setVariable("ns2wsdl", this.namespacesToWsdls);
    setVariable("id2JsonSchema", this.idsToJsonSchemas);
    setVariable("contentTypes2Ids", this.contentTypesToIds);
    setVariable("rootResources", this.rootResources);
    setVariable("jaxrsProviders", this.jaxrsProviders);
    setVariable("baseDeploymentAddress", "");
  }

  /**
   * Load the known content types (map of content type to id).
   *
   * @return The known content type.
   */
  protected HashMap<String, String> loadKnownContentTypes() {
    HashMap<String, String> contentTypes = new HashMap<String, String>();
    contentTypes.put("application/xml", "xml");
    return contentTypes;
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
    knownNamespaces.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
    knownNamespaces.put("http://ws-i.org/profiles/basic/1.1/xsd", "wsi");
    knownNamespaces.put("http://research.sun.com/wadl/2006/10", "wadl");
    knownNamespaces.put("http://www.w3.org/XML/1998/namespace", "xml");

    return knownNamespaces;
  }

  /**
   * Loads the known JSON types, keyed off the Java fqn.
   *
   * @return The map of known JSON types, keyed off the Java fqn.
   */
  private HashMap<String, JsonType> loadKnownJsonTypes() {
    HashMap<String, JsonType> knownJsonTypes = new HashMap<String, JsonType>();
    // NOTE Simply account for the primitives and other basic types. There is currently no need for JSON type definitions for them.

    knownJsonTypes.put(Boolean.class.getName(), JsonSimpleTypeDefinition.BOOLEAN);
    knownJsonTypes.put(Boolean.TYPE.getName(), JsonSimpleTypeDefinition.BOOLEAN);
    knownJsonTypes.put(Float.class.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(Float.TYPE.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(Double.class.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(Double.TYPE.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(Character.class.getName(), JsonSimpleTypeDefinition.STRING);
    knownJsonTypes.put(Character.TYPE.getName(), JsonSimpleTypeDefinition.STRING);
    knownJsonTypes.put(Byte.class.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(Byte.TYPE.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(Short.class.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(Short.TYPE.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(Integer.class.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(Integer.TYPE.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(Long.class.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(Long.TYPE.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(String.class.getName(), JsonSimpleTypeDefinition.STRING);
    knownJsonTypes.put(java.math.BigInteger.class.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(java.math.BigDecimal.class.getName(), JsonSimpleTypeDefinition.NUMBER);
    knownJsonTypes.put(java.util.Calendar.class.getName(), JsonSimpleTypeDefinition.STRING);
    knownJsonTypes.put(java.util.Date.class.getName(), JsonSimpleTypeDefinition.STRING);
    knownJsonTypes.put(java.net.URI.class.getName(), JsonSimpleTypeDefinition.STRING);
    knownJsonTypes.put(java.lang.Object.class.getName(), JsonAnyTypeDefinition.INSTANCE);
    knownJsonTypes.put(byte[].class.getName(), JsonSimpleTypeDefinition.STRING);
    knownJsonTypes.put(java.util.UUID.class.getName(), JsonSimpleTypeDefinition.STRING);
    knownJsonTypes.put(java.util.GregorianCalendar.class.getName(), JsonSimpleTypeDefinition.STRING);
    knownJsonTypes.put("javax.activation.DataHandler", JsonSimpleTypeDefinition.STRING);
    return knownJsonTypes;
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
    knownTypes.put(javax.xml.datatype.Duration.class.getName(), KnownXmlType.DURATION);
    knownTypes.put(java.lang.Object.class.getName(), KnownXmlType.ANY_TYPE);
    knownTypes.put(byte[].class.getName(), KnownXmlType.BASE64_BINARY);
    knownTypes.put(java.awt.Image.class.getName(), KnownXmlType.BASE64_BINARY);
    knownTypes.put("javax.activation.DataHandler", KnownXmlType.BASE64_BINARY);
    knownTypes.put(javax.xml.transform.Source.class.getName(), KnownXmlType.BASE64_BINARY);
    knownTypes.put(java.util.UUID.class.getName(), KnownXmlType.STRING);
    knownTypes.put(XMLGregorianCalendar.class.getName(), KnownXmlType.DATE_TIME); //JAXB spec says it maps to anySimpleType, but we can just assume dateTime...
    knownTypes.put(GregorianCalendar.class.getName(), KnownXmlType.DATE_TIME);

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
   * A map of content types to ids.
   *
   * @return A map of content types to ids.
   */
  public Map<String, String> getContentTypesToIds() {
    return contentTypesToIds;
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
   * A map of IDs to their associated JSON schema information.
   *
   * @return A map of IDs to their associated JSON schema information.
   */
  public Map<String, JsonSchemaInfo> getIdsToJsonSchemas() {
    return idsToJsonSchemas;
  }

  /**
   * The list of root resources.
   *
   * @return The list of root resources.
   */
  public List<RootResource> getRootResources() {
    return rootResources;
  }

  /**
   * The list of JAX-RS providers.
   *
   * @return The list of JAX-RS providers.
   */
  public List<TypeDeclaration> getJAXRSProviders() {
    return jaxrsProviders;
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

    for (WebMethod webMethod : ei.getWebMethods()) {
      for (WebMessage webMessage : webMethod.getMessages()) {
        for (WebMessagePart messagePart : webMessage.getParts()) {
          if (messagePart.isImplicitSchemaElement()) {
            ImplicitSchemaElement implicitElement = (ImplicitSchemaElement) messagePart;
            String particleNamespace = messagePart.getParticleQName().getNamespaceURI();
            SchemaInfo schemaInfo = namespacesToSchemas.get(particleNamespace);
            if (schemaInfo == null) {
              schemaInfo = new SchemaInfo();
              schemaInfo.setId(addNamespace(particleNamespace));
              schemaInfo.setNamespace(particleNamespace);
              namespacesToSchemas.put(particleNamespace, schemaInfo);
            }
            schemaInfo.getImplicitSchemaElements().add(implicitElement);
          }
        }
      }
    }

    wsdlInfo.getEndpointInterfaces().add(ei);
    this.endpointInterfaces.add(ei);

    if (includeReferencedClasses()) {
      REFERENCE_STACK.get().addFirst("endpoint interface " + ei.getQualifiedName());
      addReferencedTypeDefinitions(ei);
      REFERENCE_STACK.get().removeFirst();
    }
  }

  /**
   * Whether to include referenced type definitions.
   *
   * @return Whether to include referenced type definitions.
   */
  protected boolean includeReferencedClasses() {
    return this.enunciateConfig == null || this.enunciateConfig.isIncludeReferencedClasses();
  }

  /**
   * Add a json type definition to the model.
   *
   * @param typeDefinition The json type definition to add to the model.
   */
  public void addJsonType(JsonTypeDefinition typeDefinition) {
    if (!knownJsonTypes.containsKey(typeDefinition.getQualifiedName())) {
      JsonSchemaInfo jsonSchemaInfo = schemaForType(typeDefinition.classDeclaration());
      if (!jsonSchemaInfo.getTypesByName().containsKey(typeDefinition.getTypeName())) {
        jsonSchemaInfo.getTypesByName().put(typeDefinition.getTypeName(), typeDefinition);
        knownJsonTypes.put(typeDefinition.getQualifiedName(), typeDefinition);
      }

      if (includeReferencedClasses() && (this.enunciateConfig == null || typeDefinition instanceof JsonObjectTypeDefinition)) {
        JsonObjectTypeDefinition objectTypeDefinition = (JsonObjectTypeDefinition) typeDefinition;
        for (PropertyDeclaration property : objectTypeDefinition.getJsonPropertiesByName().values()) {
          REFERENCE_STACK.get().addFirst("json property " + property.getSimpleName() + " of json object definition " + typeDefinition.getQualifiedName());
          addReferencedJsonTypeDefinitions(property.getPropertyType());
          REFERENCE_STACK.get().removeFirst();
        }
        ClassType superclass = objectTypeDefinition.getSuperclass();
        if (superclass != null) {
          REFERENCE_STACK.get().addFirst("json type definition subclass " + typeDefinition.getQualifiedName());
          addJsonType(JsonTypeDefinition.createTypeDefinition(superclass.getDeclaration()));
          REFERENCE_STACK.get().removeFirst();
        }
      }
    }
  }

  /**
   * Add a type definition to the model.
   *
   * @param typeDef The type definition to add to the model.
   */
  public void add(TypeDefinition typeDef) {
    if (typeDef.getAnnotation(XmlTransient.class) == null) { //make sure we don't add a transient type definition.
      if (typeDef.getAnnotation(XmlRootElement.class) != null && Collections.binarySearch(this.rootElements, typeDef, CLASS_COMPARATOR) < 0) {
        //if the type definition is a root element, we want to make sure it's added to the model.
        add(new RootElementDeclaration((ClassDeclaration) typeDef.getDelegate(), typeDef));
      }

      int position = Collections.binarySearch(this.typeDefinitions, typeDef, CLASS_COMPARATOR);
      if (position < 0 && !isKnownType(typeDef)) {
        if (getEnunciateConfig() != null && getEnunciateConfig().isIncludeReferenceTrailInErrors()) {
          typeDef.getReferencedFrom().add(currentReferenceLocation());
        }
        this.typeDefinitions.add(-position - 1, typeDef);
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

        REFERENCE_STACK.get().addFirst("\"see also\" annotation");
        addSeeAlsoTypeDefinitions(typeDef);
        REFERENCE_STACK.get().removeFirst();

        for (Element element : typeDef.getElements()) {
          if (includeReferencedClasses()) {
            REFERENCE_STACK.get().addFirst("accessor " + element.getSimpleName() + " of type definition " + typeDef.getQualifiedName());
            addReferencedTypeDefinitions(element);
            REFERENCE_STACK.get().removeFirst();
          }

          ImplicitSchemaElement implicitElement = getImplicitElement(element);
          if (implicitElement != null) {
            String implicitElementNamespace = element.isWrapped() ? element.getWrapperNamespace() : element.getNamespace();
            SchemaInfo referencedSchemaInfo = namespacesToSchemas.get(implicitElementNamespace);
            if (referencedSchemaInfo == null) {
              referencedSchemaInfo = new SchemaInfo();
              referencedSchemaInfo.setId(addNamespace(implicitElementNamespace));
              referencedSchemaInfo.setNamespace(implicitElementNamespace);
              namespacesToSchemas.put(implicitElementNamespace, referencedSchemaInfo);
            }
            referencedSchemaInfo.getImplicitSchemaElements().add(implicitElement);
          }
        }

        for (Attribute attribute : typeDef.getAttributes()) {
          if (includeReferencedClasses()) {
            REFERENCE_STACK.get().addFirst("accessor " + attribute.getSimpleName() + " of type definition " + typeDef.getQualifiedName());
            addReferencedTypeDefinitions(attribute);
            REFERENCE_STACK.get().removeFirst();
          }
          ImplicitSchemaAttribute implicitAttribute = getImplicitAttribute(attribute);
          if (implicitAttribute != null) {
            String implicitAttributeNamespace = attribute.getNamespace();
            SchemaInfo referencedSchemaInfo = namespacesToSchemas.get(implicitAttributeNamespace);
            if (referencedSchemaInfo == null) {
              referencedSchemaInfo = new SchemaInfo();
              referencedSchemaInfo.setId(addNamespace(implicitAttributeNamespace));
              referencedSchemaInfo.setNamespace(implicitAttributeNamespace);
              namespacesToSchemas.put(implicitAttributeNamespace, referencedSchemaInfo);
            }
            referencedSchemaInfo.getImplicitSchemaAttributes().add(implicitAttribute);
          }
        }

        if ((includeReferencedClasses())) {
          Value value = typeDef.getValue();
          if (value != null) {
            REFERENCE_STACK.get().addFirst("accessor " + value.getSimpleName() + " of type definition " + typeDef.getQualifiedName());
            addReferencedTypeDefinitions(value);
            REFERENCE_STACK.get().removeFirst();
          }

          ClassType superClass = typeDef.getSuperclass();
          if (!typeDef.isEnum() && superClass != null) {
            REFERENCE_STACK.get().addFirst("type definition subclass " + typeDef.getQualifiedName());
            addReferencedTypeDefinitions(superClass);
            REFERENCE_STACK.get().removeFirst();
          }
        }
      }
    }
  }

  /**
   * A descrition of the current reference location.
   *
   * @return A descrition of the current reference location.
   */
  protected String currentReferenceLocation() {
    StringBuilder builder = new StringBuilder();
    Iterator<String> step = REFERENCE_STACK.get().iterator();
    while (step.hasNext()) {
      String location = step.next();
      builder.append(location);
      if (step.hasNext()) {
        builder.append(" of ");
      }
    }
    return builder.toString();
  }

  /**
   * Whether the specified type is a known type.
   *
   * @param typeDef The type def.
   * @return Whether the specified type is a known type.
   */
  protected boolean isKnownType(TypeDefinition typeDef) {
    return knownTypes.containsKey(typeDef.getQualifiedName())
      || JAXBElement.class.getName().equals(typeDef.getQualifiedName())
      || ((DecoratedTypeMirror) typeDef.getSuperclass()).isInstanceOf(JAXBElement.class.getName());
  }

  /**
   * Add the type definition(s) referenced by the given attribute.
   *
   * @param attribute The attribute.
   */
  protected void addReferencedTypeDefinitions(Attribute attribute) {
    addSeeAlsoTypeDefinitions(attribute);
    addReferencedTypeDefinitions(attribute.isAdapted() ? attribute.getAdapterType() : attribute.getAccessorType());
  }

  /**
   * Add the type definition(s) referenced by the given value.
   *
   * @param value The value.
   */
  protected void addReferencedTypeDefinitions(Value value) {
    addSeeAlsoTypeDefinitions(value);
    addReferencedTypeDefinitions(value.isAdapted() ? value.getAdapterType() : value.getAccessorType());
  }

  /**
   * Add the referenced type definitions for the specified element.
   *
   * @param element The element.
   */
  protected void addReferencedTypeDefinitions(Element element) {
    addSeeAlsoTypeDefinitions(element);
    if (element instanceof ElementRef && element.isCollectionType()) {
      //special case for collections of element refs because the collection is lazy-loaded.
      addReferencedTypeDefinitions(element.getAccessorType());
    }
    else {
      for (Element choice : element.getChoices()) {
        addReferencedTypeDefinitions(choice.isAdapted() ? choice.getAdapterType() : choice.getAccessorType());
      }
    }
  }


  /**
   * Gets the implicit element for the specified element, or null if there is no implicit element.
   *
   * @param element The element.
   * @return The implicit element, or null if none.
   */
  protected ImplicitSchemaElement getImplicitElement(Element element) {
    if (!(element instanceof ElementRef)) {
      boolean qualified = element.getForm() == XmlNsForm.QUALIFIED;
      String typeNamespace = element.getTypeDefinition().getNamespace();
      typeNamespace = typeNamespace == null ? "" : typeNamespace;
      String elementNamespace = element.isWrapped() ? element.getWrapperNamespace() : element.getNamespace();
      elementNamespace = elementNamespace == null ? "" : elementNamespace;

      if ((!elementNamespace.equals(typeNamespace)) && (qualified || !"".equals(elementNamespace))) {
        return element.isWrapped() ? new ImplicitWrappedElementRef(element) : new ImplicitElementRef(element);
      }
    }

    return null;
  }

  /**
   * Gets the implicit attribute for the specified attribute, or null if there is no implicit attribute.
   *
   * @param attribute The attribute.
   * @return The implicit attribute, or null if none.
   */
  protected ImplicitSchemaAttribute getImplicitAttribute(Attribute attribute) {
    boolean qualified = attribute.getForm() == XmlNsForm.QUALIFIED;
    String typeNamespace = attribute.getTypeDefinition().getNamespace();
    typeNamespace = typeNamespace == null ? "" : typeNamespace;
    String attributeNamespace = attribute.getNamespace();
    attributeNamespace = attributeNamespace == null ? "" : attributeNamespace;

    if ((!attributeNamespace.equals(typeNamespace)) && (qualified || !"".equals(attributeNamespace))) {
      return new ImplicitAttributeRef(attribute);
    }
    else {
      return null;
    }
  }

  /**
   * Add a root resource to the model.
   *
   * @param rootResource The root resource to add to the model.
   */
  public void add(RootResource rootResource) {
    if (includeReferencedClasses()) {
      REFERENCE_STACK.get().addFirst("\"see also\" annotation of root resource " + rootResource.getQualifiedName());
      addSeeAlsoTypeDefinitions(rootResource);
      REFERENCE_STACK.get().removeFirst();
      for (ResourceMethod resourceMethod : rootResource.getResourceMethods(true)) {
        REFERENCE_STACK.get().addFirst("resource method " + resourceMethod.getSimpleName() + " of root resource " + rootResource.getQualifiedName());
        addReferencedTypeDefinitions(resourceMethod);
        REFERENCE_STACK.get().removeFirst();
      }
    }

    this.rootResources.add(rootResource);
  }

  /**
   * Add the referenced type definitions for the specified resource method.
   *
   * @param resourceMethod The resource method.
   */
  protected void addReferencedTypeDefinitions(ResourceMethod resourceMethod) {
    REFERENCE_STACK.get().addFirst("\"see also\" annotation");
    addSeeAlsoTypeDefinitions(resourceMethod);
    REFERENCE_STACK.get().removeFirst();
    ResourceEntityParameter ep = resourceMethod.getEntityParameter();
    if (ep != null) {
      REFERENCE_STACK.get().addFirst("entity parameter " + ep.getSimpleName());
      TypeMirror type = ep.getType();
      if (type instanceof ClassType) {
        ClassDeclaration classDeclaration = ((ClassType) type).getDeclaration();
        if (classDeclaration.getAnnotation(XmlRootElement.class) != null) {
          //only add referenced type definitions for root elements.
          final RootElementDeclaration rootElement = new RootElementDeclaration(classDeclaration, createTypeDefinition(classDeclaration));
          add(rootElement);

          // TODO Uncomment when jackson-jaxb detection is corrected or after 1.17 release.
//          if (jacksonAvailable() && contentTypeIncluded(resourceMethod.getConsumesMime(), MediaType.APPLICATION_JSON)) {
//            addJsonRootElement(rootElement);
//          }
        }

        if (classDeclaration.getAnnotation(JsonRootType.class) != null) {
          addJsonRootElement(new JsonRootElementDeclaration(JsonTypeDefinition.createTypeDefinition(classDeclaration)));
        }
      }
      REFERENCE_STACK.get().removeFirst();
    }

    ResourcePayloadTypeAdapter outputPayload = resourceMethod.getOutputPayload();
    if (outputPayload != null) {
      TypeMirror returnType = outputPayload.getDelegate();
      if (returnType instanceof ClassType) {
        REFERENCE_STACK.get().addFirst("return type");
        ClassDeclaration classDeclaration = ((ClassType) returnType).getDeclaration();
        if (classDeclaration != null) {
          if (classDeclaration.getAnnotation(XmlRootElement.class) != null) {
            //only add referenced type definitions for root elements.
            final RootElementDeclaration rootElement = new RootElementDeclaration(classDeclaration, createTypeDefinition(classDeclaration));
            add(rootElement);

            // TODO Uncomment when jackson-jaxb detection is corrected or after 1.17 release.
    //        if (jacksonAvailable() && contentTypeIncluded(resourceMethod.getProducesMime(), MediaType.APPLICATION_JSON)) {
    //          addJsonRootElement(rootElement);
    //        }
          }

          if (classDeclaration.getAnnotation(JsonRootType.class) != null) {
            addJsonRootElement(new JsonRootElementDeclaration(JsonTypeDefinition.createTypeDefinition(classDeclaration)));
          }
        }
        REFERENCE_STACK.get().removeFirst();
      }
    }

    //todo: include referenced type definitions from the errors?
  }

  private static boolean jacksonAvailable() {
    try {
      Class.forName("org.codehaus.jackson.xc.JaxbAnnotationIntrospector");
      return true;
    } catch(ClassNotFoundException e) {
      return false;
    }
  }

  /**
   * Add a JAX-RS provider to the model.
   *
   * @param declaration The declaration of the provider.
   */
  public void addJAXRSProvider(TypeDeclaration declaration) {
    this.jaxrsProviders.add(declaration);

    Produces produces = declaration.getAnnotation(Produces.class);
    if (produces != null) {
      for (String contentType : produces.value()) {
        try {
          addContentType(MimeType.parse(contentType).toString());
        }
        catch (Exception e) {
          addContentType(contentType);
        }
      }
    }

    Consumes consumes = declaration.getAnnotation(Consumes.class);
    if (consumes != null) {
      for (String contentType : consumes.value()) {
        try {
          addContentType(MimeType.parse(contentType).toString());
        }
        catch (Exception e) {
          addContentType(contentType);
        }
      }
    }
  }

  /**
   * Add a JSON root element to the model to reflect an XML root element being serialized as JSON with jackson.
   *
   * @param rootElement The root element to add.
   */
  public void addJsonRootElement(final RootElementDeclaration rootElement) {
    assert rootElement != null : "rootElement must not be null";

    if (rootElement.getAnnotation(XmlTransient.class) == null) {
      addJsonRootElement(new JsonRootElementDeclaration(createJsonTypeDefinition(rootElement.getTypeDefinition())));
    }
  }

  private JsonTypeDefinition createJsonTypeDefinition(final TypeDefinition typeDefinition) {
    assert typeDefinition != null : "typeDefinition must not be null";

    JsonTypeDefinition jsonTypeDefinition = JsonTypeDefinition.createTypeDefinition(typeDefinition);

    REFERENCE_STACK.get().addFirst("\"see also\" annotation");
    addJsonSeeAlsoTypeDefinitions(typeDefinition);
    REFERENCE_STACK.get().removeFirst();

    for (Element element : typeDefinition.getElements()) {
      if (includeReferencedClasses()) {
        REFERENCE_STACK.get().addFirst("accessor " + element.getSimpleName() + " of type definition " + typeDefinition.getQualifiedName());
        addJsonSeeAlsoTypeDefinitions(element);
        for (Element chioce : element.getChoices()) {
          addReferencedJsonTypeDefinitions(chioce.isAdapted() ? chioce.getAdapterType() : chioce.getAccessorType());
        }
        REFERENCE_STACK.get().removeFirst();
      }
    }

    for (Attribute attribute : typeDefinition.getAttributes()) {
      if (includeReferencedClasses()) {
        REFERENCE_STACK.get().addFirst("accessor " + attribute.getSimpleName() + " of type definition " + typeDefinition.getQualifiedName());
        addJsonSeeAlsoTypeDefinitions(attribute);
        addReferencedJsonTypeDefinitions(attribute.isAdapted() ? attribute.getAdapterType() : attribute.getAccessorType());
        REFERENCE_STACK.get().removeFirst();
      }
    }

    if ((includeReferencedClasses())) {
      Value value = typeDefinition.getValue();
      if (value != null) {
        REFERENCE_STACK.get().addFirst("accessor " + value.getSimpleName() + " of type definition " + typeDefinition.getQualifiedName());
        addJsonSeeAlsoTypeDefinitions(value);
        addReferencedJsonTypeDefinitions(value.isAdapted() ? value.getAdapterType() : value.getAccessorType());
        REFERENCE_STACK.get().removeFirst();
      }

      ClassType superClass = typeDefinition.getSuperclass();
      if (!typeDefinition.isEnum() && superClass != null) {
        REFERENCE_STACK.get().addFirst("type definition subclass " + typeDefinition.getQualifiedName());
        addReferencedJsonTypeDefinitions(superClass);
        REFERENCE_STACK.get().removeFirst();
      }
    }

    return jsonTypeDefinition;
  }

  /**
   * Add a JSON root element to the model.
   *
   * @param rootElementDeclaration The root element to add.
   */
  public void addJsonRootElement(JsonRootElementDeclaration rootElementDeclaration) {
    JsonTypeDefinition typeDefinition = rootElementDeclaration.getTypeDefinition();
    addJsonType(typeDefinition);
    JsonSchemaInfo jsonSchemaInfo = schemaForType(typeDefinition.classDeclaration());
    jsonSchemaInfo.getTopLevelTypesByName().put(typeDefinition.getTypeName(), rootElementDeclaration);
  }

  private JsonSchemaInfo schemaForType(final ClassDeclaration delegate) {
    String schemaId = JsonSchemaInfo.schemaIdForType(delegate);
    JsonSchemaInfo jsonSchemaInfo = getIdsToJsonSchemas().get(schemaId);
    if (jsonSchemaInfo == null) {
      PackageDeclaration schemaPackage = delegate.getPackage();
      jsonSchemaInfo = new JsonSchemaInfo(schemaPackage);
      getIdsToJsonSchemas().put(schemaId, jsonSchemaInfo);

      if(schemaPackage.getAnnotation(JsonTypeMapping.class) != null) {
        applyJsonTypeMapping(schemaPackage.getAnnotation(JsonTypeMapping.class));
      } else if(schemaPackage.getAnnotation(JsonTypeMappings.class) != null) {
        for (final JsonTypeMapping jsonTypeMapping : schemaPackage.getAnnotation(JsonTypeMappings.class).value()) {
          applyJsonTypeMapping(jsonTypeMapping);
        }
      }
    }
    return jsonSchemaInfo;
  }

  private void applyJsonTypeMapping(final JsonTypeMapping jsonTypeMapping) {
    assert jsonTypeMapping != null : "jsonTypeMapping must not be null";

    String jsonType = jsonTypeMapping.jsonType();
    if(jsonType.equalsIgnoreCase(JsonSimpleTypeDefinition.BOOLEAN.getTypeName())) {
      knownJsonTypes.put(jsonTypeMapping.javaType(), JsonSimpleTypeDefinition.BOOLEAN);
    } else if(jsonType.equalsIgnoreCase(JsonSimpleTypeDefinition.NUMBER.getTypeName())) {
      knownJsonTypes.put(jsonTypeMapping.javaType(), JsonSimpleTypeDefinition.NUMBER);
    } else if(jsonType.equalsIgnoreCase(JsonSimpleTypeDefinition.STRING.getTypeName())) {
      knownJsonTypes.put(jsonTypeMapping.javaType(), JsonSimpleTypeDefinition.STRING);
    } else if(jsonType.equalsIgnoreCase(JsonAnyTypeDefinition.INSTANCE.getTypeName())) {
        knownJsonTypes.put(jsonTypeMapping.javaType(), JsonAnyTypeDefinition.INSTANCE);
    } else {
      knownJsonTypes.put(jsonTypeMapping.javaType(), findJsonTypeDefinition(jsonType));
    }
  }

  /**
   * Add a root element to the model.
   *
   * @param rootElement The root element to add.
   */
  public void add(RootElementDeclaration rootElement) {
    int position = Collections.binarySearch(this.rootElements, rootElement, CLASS_COMPARATOR);
    if (position < 0) {
      this.rootElements.add(-position - 1, rootElement);
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

      if (includeReferencedClasses()) {
        REFERENCE_STACK.get().addFirst("root element " + rootElement.getQualifiedName());
        addReferencedTypeDefinitions(rootElement);
        REFERENCE_STACK.get().removeFirst();
      }
    }
  }

  /**
   * Add any statically-referenced type definitions to the model.
   *
   * @param rootEl The root element.
   */
  protected void addReferencedTypeDefinitions(RootElementDeclaration rootEl) {
    TypeDefinition typeDefinition = rootEl.getTypeDefinition();
    if (typeDefinition != null) {
      add(typeDefinition);
    }
    else {
      //some root elements don't have a reference to their type definitions.
      add(createTypeDefinition((ClassDeclaration) rootEl.getDelegate()));
    }
  }

  /**
   * Adds any type definitions referenced by an endpoint interface.
   *
   * @param ei The endpoint interface.
   */
  protected void addReferencedTypeDefinitions(EndpointInterface ei) {
    REFERENCE_STACK.get().addFirst("\"see also\" annotation");
    addSeeAlsoTypeDefinitions(ei);
    REFERENCE_STACK.get().removeFirst();
    for (WebMethod webMethod : ei.getWebMethods()) {
      REFERENCE_STACK.get().addFirst("method " + webMethod.getSimpleName());
      addReferencedTypeDefinitions(webMethod);
      REFERENCE_STACK.get().removeFirst();
    }
  }

  /**
   * Adds any type definitions referenced by a web method.
   *
   * @param webMethod The web method.
   */
  protected void addReferencedTypeDefinitions(WebMethod webMethod) {
    REFERENCE_STACK.get().addFirst("\"see also\" annotation");
    addSeeAlsoTypeDefinitions(webMethod);
    REFERENCE_STACK.get().removeFirst();
    WebResult result = webMethod.getWebResult();
    REFERENCE_STACK.get().addFirst("return type");
    addReferencedTypeDefinitions(result.isAdapted() ? result.getAdapterType() : result.getType());
    REFERENCE_STACK.get().removeFirst();
    for (WebParam webParam : webMethod.getWebParameters()) {
      REFERENCE_STACK.get().addFirst("parameter " + webParam.getSimpleName());
      addReferencedTypeDefinitions(webParam.isAdapted() ? webParam.getAdapterType() : webParam.getType());
      REFERENCE_STACK.get().removeFirst();
    }
    for (WebFault webFault : webMethod.getWebFaults()) {
      REFERENCE_STACK.get().addFirst("thrown fault " + webFault.getSimpleName());
      addReferencedTypeDefinitions(webFault);
      REFERENCE_STACK.get().removeFirst();
    }
  }

  /**
   * Adds any type definitions referenced by a web fault.
   *
   * @param webFault The web fault.
   */
  protected void addReferencedTypeDefinitions(WebFault webFault) {
    if (webFault.isImplicitSchemaElement()) {
      for (ImplicitChildElement childElement : webFault.getChildElements()) {
        WebFault.FaultBeanChildElement fbce = (WebFault.FaultBeanChildElement) childElement;
        REFERENCE_STACK.get().addFirst("property " + fbce.getProperty().getSimpleName());    
        addReferencedTypeDefinitions(fbce.isAdapted() ? fbce.getAdapterType() : fbce.getType());
        REFERENCE_STACK.get().removeFirst();
      }
    }
    else {
      REFERENCE_STACK.get().addFirst("explicit fault bean");
      ClassType faultBeanType = webFault.getExplicitFaultBeanType();
      if (faultBeanType != null) {
        addReferencedTypeDefinitions(faultBeanType);
      }
      REFERENCE_STACK.get().removeFirst();
    }
  }

  /**
   * Adds any referenced type definitions for the specified type mirror.
   *
   * @param type The type mirror.
   */
  protected void addReferencedTypeDefinitions(TypeMirror type) {
    type.accept(new ReferencedXmlTypeDefinitionVisitor());
  }

  /**
   * Adds any referenced type definitions for the specified type mirror.
   *
   * @param type The type mirror.
   */
  protected void addReferencedJsonTypeDefinitions(TypeMirror type) {
    type.accept(new ReferencedJsonTypeDefinitionVisitor());
  }

  /**
   * Add any type definitions that are referenced using {@link XmlSeeAlso}.
   *
   * @param declaration The declaration.
   */
  protected void addSeeAlsoTypeDefinitions(Declaration declaration) {
    XmlSeeAlso seeAlso = declaration.getAnnotation(XmlSeeAlso.class);
    if (seeAlso != null) {
      try {
        Class[] classes = seeAlso.value();
        AnnotationProcessorEnvironment ape = Context.getCurrentEnvironment();
        for (Class clazz : classes) {
          ClassType type = (ClassType) ape.getTypeUtils().getDeclaredType(ape.getTypeDeclaration(clazz.getName()));
          ClassDeclaration typeDeclaration = type.getDeclaration();
          if (typeDeclaration != null) {
            addSeeAlsoReference(typeDeclaration);
          }
        }
      }
      catch (MirroredTypesException e) {
        Collection<TypeMirror> mirrors = e.getTypeMirrors();
        for (TypeMirror mirror : mirrors) {
          if (mirror instanceof ClassType) {
            ClassDeclaration typeDeclaration = ((ClassType) mirror).getDeclaration();
            if (typeDeclaration != null) {
              addSeeAlsoReference(typeDeclaration);
            }
          }
        }
      }
    }
  }

  /**
   * Add a "see also" reference.
   *
   * @param typeDeclaration The reference.
   */
  protected void addSeeAlsoReference(ClassDeclaration typeDeclaration) {
    if (typeDeclaration.getAnnotation(XmlRegistry.class) == null) {
      add(createTypeDefinition(typeDeclaration));
    }
  }

  /**
   * Add any type definitions that are referenced using {@link XmlSeeAlso} as JSON serializable types.
   *
   * @param declaration The declaration.
   */
  protected void addJsonSeeAlsoTypeDefinitions(Declaration declaration) {
    assert declaration != null : "declaration must not be null";

    XmlSeeAlso seeAlso = declaration.getAnnotation(XmlSeeAlso.class);
    if (seeAlso != null) {
      try {
        Class[] classes = seeAlso.value();
        AnnotationProcessorEnvironment ape = Context.getCurrentEnvironment();
        for (Class clazz : classes) {
          ClassType type = (ClassType) ape.getTypeUtils().getDeclaredType(ape.getTypeDeclaration(clazz.getName()));
          ClassDeclaration typeDeclaration = type.getDeclaration();
          if (typeDeclaration != null) {
            addJsonType(JsonTypeDefinition.createTypeDefinition(typeDeclaration));
          }
        }
      }
      catch (MirroredTypesException e) {
        Collection<TypeMirror> mirrors = e.getTypeMirrors();
        for (TypeMirror mirror : mirrors) {
          if (mirror instanceof ClassType) {
            ClassDeclaration typeDeclaration = ((ClassType) mirror).getDeclaration();
            if (typeDeclaration != null) {
              addJsonType(JsonTypeDefinition.createTypeDefinition(typeDeclaration));
            }
          }
        }
      }
    }
  }

  /**
   * Find the type definition for a class given the class's declaration.
   *
   * @param declaration The declaration.
   * @return The type definition.
   */
  protected TypeDefinition createTypeDefinition(ClassDeclaration declaration) {
    declaration = narrowToAdaptingType(declaration);
    if (isEnumType(declaration)) {
      return new EnumTypeDefinition((EnumDeclaration) declaration);
    }
    else if (isSimpleType(declaration)) {
      return new SimpleTypeDefinition(declaration);
    }
    else {
      //assume its a complex type.
      return new ComplexTypeDefinition(declaration);
    }
  }

  /**
   * Narrows the existing declaration down to its adapting declaration, if it's being adapted. Otherwise, the original declaration will be returned.
   *
   * @param declaration The declaration to narrow.
   * @return The narrowed declaration.
   */
  protected ClassDeclaration narrowToAdaptingType(ClassDeclaration declaration) {
    AdapterType adapterType = AdapterUtil.findAdapterType(declaration);
    if (adapterType != null) {
      TypeMirror adaptingType = adapterType.getAdaptingType();
      if (!(adaptingType instanceof ClassType)) {
        return declaration;
      }
      else {
        ClassDeclaration adaptingDeclaration = ((ClassType) adaptingType).getDeclaration();
        if (adaptingDeclaration == null) {
          throw new ValidationException(declaration.getPosition(), String.format("Class %s is being adapted by a type (%s) that doesn't seem to be on the classpath.", declaration.getQualifiedName(), adaptingType));
        }
        return adaptingDeclaration;
      }
    }

    return declaration;
  }

  /**
   * A quick check to see if a declaration defines a complex schema type.
   *
   * @param declaration The declaration to check.
   * @return the value of the check.
   */
  protected boolean isComplexType(TypeDeclaration declaration) {
    return !(declaration instanceof InterfaceDeclaration) && !isEnumType(declaration) && !isSimpleType(declaration);
  }

  /**
   * A quick check to see if a declaration defines a enum schema type.
   *
   * @param declaration The declaration to check.
   * @return the value of the check.
   */
  protected boolean isEnumType(TypeDeclaration declaration) {
    return (declaration instanceof EnumDeclaration);
  }

  /**
   * A quick check to see if a declaration defines a simple schema type.
   *
   * @param declaration The declaration to check.
   * @return the value of the check.
   */
  protected boolean isSimpleType(TypeDeclaration declaration) {
    if (declaration instanceof InterfaceDeclaration) {
      if (declaration.getAnnotation(javax.xml.bind.annotation.XmlType.class) != null) {
        throw new ValidationException(declaration.getPosition(), declaration.getQualifiedName() + ": an interface must not be annotated with @XmlType.");
      }

      return false;
    }

    if (isEnumType(declaration)) {
      return false;
    }


    ClassDeclaration classDeclaration = (ClassDeclaration) declaration;
    GenericTypeDefinition typeDef = new GenericTypeDefinition(classDeclaration);
    return ((typeDef.getValue() != null) && (hasNeitherAttributesNorElements(typeDef)));
  }

  /**
   * Whether the specified type definition has neither attributes nor elements.
   *
   * @param typeDef The type def.
   * @return Whether the specified type definition has neither attributes nor elements.
   */
  protected boolean hasNeitherAttributesNorElements(GenericTypeDefinition typeDef) {
    boolean none = (typeDef.getAttributes().isEmpty()) && (typeDef.getElements().isEmpty());
    ClassDeclaration superDeclaration = ((ClassDeclaration) typeDef.getDelegate()).getSuperclass().getDeclaration();
    if (!Object.class.getName().equals(superDeclaration.getQualifiedName())) {
      none &= hasNeitherAttributesNorElements(new GenericTypeDefinition(superDeclaration));
    }
    return none;
  }

  /**
   * Add an XML registry.
   *
   * @param registry The registry to add.
   */
  public void add(Registry registry) {
    add(registry.getSchema());

    String namespace = registry.getSchema().getNamespace();
    String prefix = addNamespace(namespace);

    SchemaInfo schemaInfo = namespacesToSchemas.get(namespace);
    if (schemaInfo == null) {
      schemaInfo = new SchemaInfo();
      schemaInfo.setId(prefix);
      schemaInfo.setNamespace(namespace);
      namespacesToSchemas.put(namespace, schemaInfo);
    }
    schemaInfo.getRegistries().add(registry);
    REFERENCE_STACK.get().addFirst("registry " + registry.getQualifiedName());
    addReferencedTypeDefinitions(registry);
    for (LocalElementDeclaration led : registry.getLocalElementDeclarations()) {
      REFERENCE_STACK.get().addFirst("method " + led.getSimpleName());
      add(led);
      REFERENCE_STACK.get().removeFirst();
    }
    REFERENCE_STACK.get().removeFirst();
  }

  /**
   * Add the referenced type definitions for a registry..
   *
   * @param registry The registry.
   */
  protected void addReferencedTypeDefinitions(Registry registry) {
    REFERENCE_STACK.get().addFirst("\"see also\" annotation");
    addSeeAlsoTypeDefinitions(registry);
    REFERENCE_STACK.get().removeFirst();
    for (MethodDeclaration methodDeclaration : registry.getInstanceFactoryMethods()) {
      REFERENCE_STACK.get().addFirst("method " + methodDeclaration.getSimpleName());
      addReferencedTypeDefinitions(methodDeclaration.getReturnType());
      REFERENCE_STACK.get().removeFirst();
    }
  }

  public void add(LocalElementDeclaration led) {
    String namespace = led.getNamespace();
    String prefix = addNamespace(namespace);

    SchemaInfo schemaInfo = namespacesToSchemas.get(namespace);
    if (schemaInfo == null) {
      schemaInfo = new SchemaInfo();
      schemaInfo.setId(prefix);
      schemaInfo.setNamespace(namespace);
      namespacesToSchemas.put(namespace, schemaInfo);
    }
    schemaInfo.getLocalElementDeclarations().add(led);
    addReferencedTypeDefinitions(led);
  }

  /**
   * Adds the referenced type definitions for the specified local element declaration.
   *
   * @param led The local element declaration.
   */
  protected void addReferencedTypeDefinitions(LocalElementDeclaration led) {
    addSeeAlsoTypeDefinitions(led);
    TypeDeclaration scope = led.getElementScope();
    if (scope instanceof ClassDeclaration) {
      REFERENCE_STACK.get().addFirst("scope");
      add(createTypeDefinition((ClassDeclaration) scope));
      REFERENCE_STACK.get().removeFirst();
    }
    TypeDeclaration typeDeclaration = led.getElementTypeDeclaration();
    if (typeDeclaration instanceof ClassDeclaration) {
      add(createTypeDefinition((ClassDeclaration) typeDeclaration));
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
          throw new ValidationException(schema.getPosition(), schema.getQualifiedName() + ": inconsistent elementFormDefault declarations: " + pckg.getQualifiedName());
        }
      }
    }

    if (schema.getAttributeFormDefault() != XmlNsForm.UNSET) {
      for (Schema pckg : schemaInfo.getPackages()) {
        if ((pckg.getAttributeFormDefault() != null) && (schema.getAttributeFormDefault() != pckg.getAttributeFormDefault())) {
          throw new ValidationException(schema.getPosition(), schema.getQualifiedName() + ": inconsistent attributeFormDefault declarations: " + pckg.getQualifiedName());
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
   * Add a content type.
   *
   * @param contentType The content type to add.
   */
  public void addContentType(String contentType) {
    if (!contentTypesToIds.containsKey(contentType)) {
      String id = getDefaultContentTypeId(contentType);
      if (id != null) {
        contentTypesToIds.put(contentType, id);
      }
    }
  }

  /**
   * Get the default content type id for the specified content type.
   *
   * @param contentType The content type.
   * @return The default content type id, or null if the content type is a wildcard type.
   */
  protected String getDefaultContentTypeId(String contentType) {
    String id = contentType;
    if (id.endsWith("/")) {
      throw new IllegalArgumentException("Illegal content type: " + id);
    }

    int semiColon = id.indexOf(';');
    if (semiColon > -1) {
      id = id.substring(0, semiColon);
    }

    int lastSlash = id.lastIndexOf('/');
    if (lastSlash > -1) {
      id = id.substring(lastSlash + 1);
    }

    int plus = id.indexOf('+');
    if (plus > -1) {
      id = id.substring(0, plus);
    }

    if (id.contains("*")) {
      //wildcard types have no ids.
      return null;
    }
    else {
      return id;
    }
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
    if (declaration.getAnnotation(XmlTransient.class) != null) {
      return KnownXmlType.ANY_TYPE;
    }

    return knownTypes.get(declaration.getQualifiedName());
  }

  /**
   * Find the JSON type definition for a class given the class's qualified name, or null if the class hasn't been added to the model.
   * @param qualifiedName Qualified name of the class to find the JSON type for.
   *
   * @return The JSON type definition.
   */
  public JsonType findJsonTypeDefinition(final String qualifiedName) {
    return knownJsonTypes.get(qualifiedName);
  }

  /**
   * Find the JSON type definition for a class given the class's declaration, or null if the class hasn't been added to the model.
   *
   * @param declaration The declaration.
   * @return The JSON type definition.
   */
  public JsonType findJsonTypeDefinition(ClassDeclaration declaration) {
    assert declaration != null;

    return knownJsonTypes.get(declaration.getQualifiedName());
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
   * Find the root JSON element declaration for the specified class.
   *
   * @param declaration The class declaration
   * @return The JSON root element declaration, or null if the declaration hasn't been added to the model.
   */
  public JsonRootElementDeclaration findJsonRootElementDeclaration(ClassDeclaration declaration) {
    assert declaration != null;

    JsonType jsonTypeDefinition = knownJsonTypes.get(declaration.getQualifiedName());
    if (jsonTypeDefinition == null) {
      return null;
    }
    String schemaId = JsonSchemaInfo.schemaIdForType(declaration);
    return idsToJsonSchemas.get(schemaId).getTopLevelTypesByName().get(jsonTypeDefinition.getTypeName());
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
   * Finds the local element declaration for the specified class declaration.
   *
   * @param declaration The declaration for which to find the local element declaration.
   * @return The local element declaration, or null if none found.
   */
  public LocalElementDeclaration findLocalElementDeclaration(ClassDeclaration declaration) {
    if (declaration.getPackage() != null) {
      String packageName = declaration.getPackage().getQualifiedName();
      for (SchemaInfo schemaInfo : namespacesToSchemas.values()) {
        for (Registry registry : schemaInfo.getRegistries()) {
          if (registry.getSchema().getQualifiedName().equals(packageName)) {
            //find the registry for the declaration.
            for (LocalElementDeclaration localElement : registry.getLocalElementDeclarations()) {
              if (localElement.getElementTypeDeclaration() != null && localElement.getElementTypeDeclaration().getQualifiedName().equals(declaration.getQualifiedName())) {
                return localElement;
              }
            }
          }
        }
      }
    }

    return null;
  }

  /**
   * Find the element declaration (local or global) for the specified declaration.
   *
   * @param declaration The declaration.
   * @return The element declaration, or null if none were found.
   */
  public ElementDeclaration findElementDeclaration(ClassDeclaration declaration) {
    RootElementDeclaration rootDeclaration = findRootElementDeclaration(declaration);
    return rootDeclaration == null ? findLocalElementDeclaration(declaration) : rootDeclaration;
  }

  /**
   * Finds an example resource method, according to the following preference order:
   *
   * <ol>
   * <li>The first method annotated with {@link DocumentationExample}.
   * <li>The first method with BOTH an output payload with a known XML element and an input payload with a known XML element.
   * <li>The first method with an output payload with a known XML element.
   * </ol>
   *
   * @return An example resource method, or if no good examples were found.
   */
  public RESTResource findExampleResource() {

    RESTResource example = null;
    List<RootResource> resources = getRootResources();
    for (RootResource root : resources) {
      List<ResourceMethod> methods = root.getResourceMethods(true);
      for (ResourceMethod method : methods) {
        if (method.getAnnotation(DocumentationExample.class) != null && !method.getAnnotation(DocumentationExample.class).exclude()) {
          return method;
        }
        else if (method.getOutputPayload() != null && method.getOutputPayload().getXmlElement() != null) {
          if (method.getInputPayload() != null && method.getInputPayload().getXmlElement() != null) {
            //we'll prefer one with both an output AND an input.
            return method;
          }
          else {
            //we'll prefer the first one we find with an output.
            example = example == null ? method : example;
          }
        }
      }
    }

    return example;
  }

  /**
   * Finds an example resource method, according to the following preference order:
   *
   * <ol>
   * <li>The first method annotated with {@link DocumentationExample}.
   * <li>The first web method that returns a declared type.
   * <li>The first web method.
   * </ol>
   *
   * @return An example resource method, or if no good examples were found.
   */
  public WebMethod findExampleWebMethod() {
    WebMethod example = null;
    for (EndpointInterface ei : this.endpointInterfaces) {
      for (WebMethod method : ei.getWebMethods()) {
        if (method.getAnnotation(DocumentationExample.class) != null && !method.getAnnotation(DocumentationExample.class).exclude()) {
          return method;
        }
        else if (method.getWebResult() != null && method.getWebResult().getType() instanceof DeclaredType
          && (example == null || example.getWebResult() == null || (!(example.getWebResult().getType() instanceof DeclaredType)))) {
          example = method;
        }
        else {
          //we'll prefer the first one we find with an output.
          example = example == null ? method : example;
        }
      }
    }
    return example;
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

  /**
   * The wadl file for the model.
   *
   * @return The wadl file for the model.
   */
  public File getWadlFile() {
    return wadlFile;
  }

  /**
   * The wadl file for the model.
   *
   * @param wadlFile The wadl file for the model.
   */
  public void setWadlFile(File wadlFile) {
    this.wadlFile = wadlFile;
    setVariable("wadlFile", wadlFile);
    setVariable("wadlFilename", wadlFile.getName());
  }

  /**
   * The base deployment address for the Enunciate application.
   *
   * @return The base deployment address for the Enunciate application.
   */
  public String getBaseDeploymentAddress() {
    return baseDeploymentAddress;
  }

  /**
   * The base deployment address for the Enunciate application.
   *
   * @param baseDeploymentAddress The base deployment address for the Enunciate application.
   */
  public void setBaseDeploymentAddress(String baseDeploymentAddress) {
    this.baseDeploymentAddress = baseDeploymentAddress;
    setVariable("baseDeploymentAddress", baseDeploymentAddress);
  }

  /**
   * The enunciate configuration.
   *
   * @return The enunciate configuration.
   */
  public EnunciateConfiguration getEnunciateConfig() {
    return enunciateConfig;
  }

  /**
   * The enunciate configuration.
   *
   * @param enunciateConfig The enunciate configuration.
   */
  public void setEnunciateConfig(EnunciateConfiguration enunciateConfig) {
    this.enunciateConfig = enunciateConfig;
  }

  /**
   * Base class for visiting referenced types.
   */
  private abstract class DefaultReferencedTypeVisitor implements TypeVisitor {
    public void visitTypeMirror(TypeMirror typeMirror) {
      // no-op
    }

    public void visitPrimitiveType(PrimitiveType primitiveType) {
      // no-op
    }

    public void visitVoidType(VoidType voidType) {
      // no-op
    }

    public void visitReferenceType(ReferenceType referenceType) {
      // no-op
    }

    public void visitDeclaredType(DeclaredType declaredType) {
      // no-op
    }

    public void visitAnnotationType(AnnotationType annotationType) {
      // no-op
    }

    public void visitClassType(ClassType arg0) {
      // no-op
    }

    public void visitEnumType(EnumType arg0) {
      // no-op
    }

    public void visitInterfaceType(InterfaceType interfaceType) {
      MapType mapType = MapTypeUtil.findMapType(interfaceType);
      if (mapType != null) {
        mapType.getKeyType().accept(this);
        mapType.getValueType().accept(this);
      }
      else if (((DecoratedInterfaceType) TypeMirrorDecorator.decorate(interfaceType)).isCollection()) {
        Collection<TypeMirror> typeArgs = interfaceType.getActualTypeArguments();
        if (typeArgs != null) {
          for (TypeMirror typeArg : typeArgs) {
            typeArg.accept(this);
          }
        }
      }
    }

    public void visitArrayType(ArrayType arrayType) {
      arrayType.getComponentType().accept(this);
    }

    public void visitTypeVariable(TypeVariable typeVariable) {
      Iterator<ReferenceType> bounds = typeVariable.getDeclaration().getBounds().iterator();
      if (bounds.hasNext()) {
        bounds.next().accept(this);
      }
    }

    public void visitWildcardType(WildcardType wildcardType) {
      Iterator<ReferenceType> upperBounds = wildcardType.getUpperBounds().iterator();
      if (upperBounds.hasNext()) {
        upperBounds.next().accept(this);
      }
    }
  }

  /**
   * Visitor for JSON-referenced type definitions.
   */
  private class ReferencedJsonTypeDefinitionVisitor extends DefaultReferencedTypeVisitor {
    public void visitClassType(ClassType classType) {
      DecoratedClassType decorated = (DecoratedClassType) TypeMirrorDecorator.decorate(classType);
      if (!decorated.isCollection()) {
        ClassDeclaration declaration = classType.getDeclaration();
        if (declaration != null) {
          addJsonType(JsonTypeDefinition.createTypeDefinition(declaration));
        }
      }

      Collection<TypeMirror> typeArgs = classType.getActualTypeArguments();
      if (typeArgs != null) {
        for (TypeMirror typeArg : typeArgs) {
          typeArg.accept(this);
        }
      }
    }

    public void visitEnumType(EnumType enumType) {
      EnumDeclaration enumDeclaration = enumType.getDeclaration();
      if (enumDeclaration != null) {
        addJsonType(JsonTypeDefinition.createTypeDefinition(enumDeclaration));
      }
    }
  }

  /**
   * Visitor for XML-referenced type definitions.
   */
  private class ReferencedXmlTypeDefinitionVisitor extends DefaultReferencedTypeVisitor {

    public void visitClassType(ClassType classType) {
      if (classType instanceof AdapterType) {
        ((AdapterType) classType).getAdaptingType().accept(this);
      }
      else {
        DecoratedClassType decorated = (DecoratedClassType) TypeMirrorDecorator.decorate(classType);
        if (decorated.getDeclaration() != null && Object.class.getName().equals(decorated.getDeclaration().getQualifiedName())) {
          //skip base object; not a type definition.
          return;
        }

        if (!decorated.isCollection() && !decorated.isInstanceOf(JAXBElement.class.getName())) {
          ClassDeclaration declaration = classType.getDeclaration();
          if (declaration != null) {
            add(createTypeDefinition(declaration));
          }
        }

        Collection<TypeMirror> typeArgs = classType.getActualTypeArguments();
        if (typeArgs != null) {
          for (TypeMirror typeArg : typeArgs) {
            typeArg.accept(this);
          }
        }
      }
    }

    public void visitEnumType(EnumType enumType) {
      EnumDeclaration enumDeclaration = enumType.getDeclaration();
      if (enumDeclaration != null) {
        add(createTypeDefinition(enumDeclaration));
      }
    }
  }
}
