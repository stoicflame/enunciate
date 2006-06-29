package net.sf.enunciate.apt;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.Types;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.jaxb.*;
import net.sf.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.enunciate.contract.jaxb.types.XmlTypeDecorator;
import net.sf.enunciate.contract.jaxb.types.XmlTypeException;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.enunciate.contract.validation.ValidationResult;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.util.ClassDeclarationComparator;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class EnunciateFreemarkerModel extends FreemarkerModel {

  private static final Comparator<ClassDeclaration> CLASS_COMPARATOR = new ClassDeclarationComparator();

  private int prefixIndex = 0;
  private final Map<String, String> namespacesToPrefixes;
  private final Map<String, SchemaInfo> namespacesToSchemas;
  private final Map<String, WsdlInfo> namespacesToWsdls;
  private final Map<String, XmlTypeMirror> knownTypes;
  private final List<TypeDefinition> typeDefinitions = new ArrayList<TypeDefinition>();
  private final List<RootElementDeclaration> rootElements = new ArrayList<RootElementDeclaration>();
  private final Validator validator;

  public EnunciateFreemarkerModel(Validator validator) {
    this.validator = validator;
    this.namespacesToPrefixes = loadKnownNamespaces();
    this.knownTypes = loadKnownTypes();
    this.namespacesToSchemas = new HashMap<String, SchemaInfo>();
    this.namespacesToWsdls = new HashMap<String, WsdlInfo>();

    setVariable("ns2prefix", this.namespacesToPrefixes);
    setVariable("ns2schema", this.namespacesToSchemas);
    setVariable("ns2wsdl", this.namespacesToWsdls);
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
  protected Map<String, XmlTypeMirror> loadKnownTypes() {
    HashMap<String, XmlTypeMirror> knownTypes = new HashMap<String, XmlTypeMirror>();

    knownTypes.put(Boolean.class.getName(), KnownXmlType.BOOLEAN);
    knownTypes.put(Byte.class.getName(), KnownXmlType.BYTE);
    knownTypes.put(Double.class.getName(), KnownXmlType.DOUBLE);
    knownTypes.put(Float.class.getName(), KnownXmlType.FLOAT);
    knownTypes.put(Integer.class.getName(), KnownXmlType.INT);
    knownTypes.put(Long.class.getName(), KnownXmlType.LONG);
    knownTypes.put(Short.class.getName(), KnownXmlType.SHORT);
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
   * Add an endpoint interface to the model.
   *
   * @param ei The endpoint interface to add to the model.
   * @return The results of validating the endpoint interface.
   */
  public ValidationResult add(EndpointInterface ei) {
    //todo: validate the ei;
    String namespace = ei.getTargetNamespace();

    String prefix = addNamespace(namespace);
    for (String reference : ei.getReferencedNamespaces()) {
      addNamespace(reference);
    }

    WsdlInfo wsdlInfo = namespacesToWsdls.get(namespace);
    if (wsdlInfo == null) {
      wsdlInfo = new WsdlInfo();
      namespacesToWsdls.put(namespace, wsdlInfo);
      wsdlInfo.setTargetNamespace(namespace);
      wsdlInfo.setFile(prefix + ".wsdl");

      //todo: configure the schema info.
      //wsdlInfo.setSchemaInfo();

      //todo: configure whether to generate.
      //wsdlInfo.setGenerate();
    }

    wsdlInfo.getEndpointInterfaces().add(ei);

    return validator.validateEndpointInterface(ei);
  }

  /**
   * Add a type definition to the model.
   *
   * @param typeDef The type definition to add to the model.
   */
  public ValidationResult add(TypeDefinition typeDef) {
    //todo: validate the typeDef;
    this.namespacesToPrefixes.putAll(typeDef.getSchema().getSpecifiedNamespacePrefixes());

    String namespace = typeDef.getTargetNamespace();
    String prefix = addNamespace(namespace);

    SchemaInfo schemaInfo = namespacesToSchemas.get(namespace);
    if (schemaInfo == null) {
      schemaInfo = new SchemaInfo();
      namespacesToSchemas.put(namespace, schemaInfo);
      schemaInfo.setNamespace(namespace);
      schemaInfo.setFile(prefix + ".xsd");
      schemaInfo.setLocation(prefix + ".xsd");
    }
    schemaInfo.getTypeDefinitions().add(typeDef);

    int position = Collections.binarySearch(this.typeDefinitions, typeDef, CLASS_COMPARATOR);
    if (position < 0) {
      this.typeDefinitions.add(-position - 1, typeDef);
    }

    return typeDef.accept(this.validator);
  }

  /**
   * Add a root element to the model.
   *
   * @param rootElement The root element to add.
   */
  public ValidationResult add(RootElementDeclaration rootElement) {
    //todo: validate the root element.
    this.namespacesToPrefixes.putAll(rootElement.getSchema().getSpecifiedNamespacePrefixes());

    String namespace = rootElement.getTargetNamespace();
    String prefix = addNamespace(namespace);

    SchemaInfo schemaInfo = namespacesToSchemas.get(namespace);
    if (schemaInfo == null) {
      schemaInfo = new SchemaInfo();
      namespacesToSchemas.put(namespace, schemaInfo);
      schemaInfo.setNamespace(namespace);
      schemaInfo.setFile(prefix + ".xsd");
      schemaInfo.setLocation(prefix + ".xsd");
    }
    schemaInfo.getGlobalElements().add(rootElement);

    int position = Collections.binarySearch(this.rootElements, rootElement, CLASS_COMPARATOR);
    if (position < 0) {
      this.rootElements.add(-position - 1, rootElement);
    }

    return this.validator.validateRootElement(rootElement);
  }

  /**
   * Add a namespace.
   *
   * @param namespace The namespace to add.
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
   * Get the xml type for the specified type.
   *
   * @param type The type.
   * @return The xml type for the specified type.
   */
  public XmlTypeMirror getXmlType(TypeMirror type) throws XmlTypeException {
    //first make sure it's a known type.
    if (type instanceof DeclaredType) {
      TypeDeclaration declaration = ((DeclaredType) type).getDeclaration();
      if (declaration != null) {
        if (knownTypes.containsKey(declaration.getQualifiedName())) {
          return knownTypes.get(declaration.getQualifiedName());
        }
        else {
          Map<String, XmlTypeMirror> specifiedTypes = new Schema(declaration.getPackage()).getSpecifiedTypes();
          if (specifiedTypes.containsKey(declaration.getQualifiedName())) {
            return specifiedTypes.get(declaration.getQualifiedName());
          }
        }
      }
    }

    return XmlTypeDecorator.decorate(type);
  }

  /**
   * Get the xml type for a specific class.
   *
   * @param clazz The class.
   * @return The xml type for a specific class.
   * @throws XmlTypeException If there was an error getting the xml type for the specified class.
   */
  public XmlTypeMirror getXmlType(Class clazz) throws XmlTypeException {
    if (knownTypes.containsKey(clazz.getName())) {
      return knownTypes.get(clazz.getName());
    }

    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    Types types = env.getTypeUtils();
    TypeDeclaration declaration = env.getTypeDeclaration(clazz.getName());
    return getXmlType(types.getDeclaredType(declaration));
  }

  /**
   * Find the type definition for a class given the class's declaration, or null if the class is xml transient.
   *
   * @param declaration The declaration.
   * @return The type definition.
   */
  public TypeDefinition findOrCreateTypeDefinition(ClassDeclaration declaration) {
    int index = Collections.binarySearch(this.typeDefinitions, declaration, CLASS_COMPARATOR);
    if (index >= 0) {
      return this.typeDefinitions.get(index);
    }
    else if (isXmlTransient(declaration)) {
      return null;
    }
    else if (isEnumType(declaration)) {
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
   * Find or create the root element declaration for the specified type definition.
   *
   * @param declaration    The class declaration
   * @param typeDefinition The specified type definition.
   * @return The root element declaration.
   */
  public RootElementDeclaration findOrCreateRootElementDeclaration(ClassDeclaration declaration, TypeDefinition typeDefinition) {
    int index = Collections.binarySearch(this.rootElements, declaration, CLASS_COMPARATOR);
    if (index >= 0) {
      return this.rootElements.get(index);
    }
    else if (isXmlTransient(declaration)) {
      return null;
    }
    else if (!isRootSchemaElement(declaration)) {
      return null;
    }
    else {
      return new RootElementDeclaration(declaration, typeDefinition);
    }
  }

  /**
   * A quick check to see if a declaration defines a complex schema type.
   */
  protected boolean isComplexType(TypeDeclaration declaration) {
    return !(declaration instanceof InterfaceDeclaration) && !isEnumType(declaration) && !isSimpleType(declaration);
  }

  /**
   * A quick check to see if a declaration defines a enum schema type.
   */
  protected boolean isEnumType(TypeDeclaration declaration) {
    return (declaration instanceof EnumDeclaration);
  }

  /**
   * A quick check to see if a declaration defines a simple schema type.
   */
  protected boolean isSimpleType(TypeDeclaration declaration) {
    if (declaration instanceof InterfaceDeclaration) {
      if (declaration.getAnnotation(XmlType.class) != null) {
        throw new ValidationException(declaration.getPosition(), "An interface must not be annotated with @XmlType.");
      }

      return false;
    }

    if (isEnumType(declaration)) {
      return false;
    }

    GenericTypeDefinition typeDef = new GenericTypeDefinition((ClassDeclaration) declaration);
    return ((typeDef.getValue() != null) && (typeDef.getAttributes().isEmpty()) && (typeDef.getElements().isEmpty()));
  }

  /**
   * A quick check to see if a declaration defines a root schema element.
   */
  protected boolean isRootSchemaElement(TypeDeclaration declaration) {
    return declaration.getAnnotation(XmlRootElement.class) != null;
  }

  /**
   * Whether a declaration is xml transient.
   *
   * @param declaration The declaration on which to determine xml transience.
   * @return Whether a declaration is xml transient.
   */
  protected boolean isXmlTransient(Declaration declaration) {
    return (declaration.getAnnotation(XmlTransient.class) != null);
  }

  /**
   * Internal class used to inherit some functionality for determining whether a declaration is a simple type
   * or a complex type.
   */
  protected static class GenericTypeDefinition extends TypeDefinition {

    protected GenericTypeDefinition(ClassDeclaration delegate) {
      super(delegate);
    }

    public ValidationResult accept(Validator validator) {
      return new ValidationResult();
    }

    public XmlTypeMirror getBaseType() {
      return KnownXmlType.ANY_TYPE;
    }
  }
}
