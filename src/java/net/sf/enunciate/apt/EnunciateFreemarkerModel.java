package net.sf.enunciate.apt;

import com.sun.mirror.declaration.*;
import net.sf.enunciate.config.SchemaInfo;
import net.sf.enunciate.config.WsdlInfo;
import net.sf.enunciate.contract.ValidationException;
import net.sf.enunciate.contract.jaxb.*;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.util.ClassDeclarationComparator;
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

  private final Map<String, String> namespacesToPrefixes;
  private final Map<String, SchemaInfo> namespacesToSchemas;
  private final Map<String, WsdlInfo> namespacesToWsdls;

  private final List<TypeDefinition> typeDefinitions = new ArrayList<TypeDefinition>();
  private final List<RootElementDeclaration> rootElements = new ArrayList<RootElementDeclaration>();

  public EnunciateFreemarkerModel(Map<String, String> ns2prefix, Map<String, SchemaInfo> ns2schema, Map<String, WsdlInfo> ns2wsdl) {
    //todo: initialize the known types, and add the SchemaType annotations...
    //todo: use the known types in the Element class, if they exist...
    //todo: change all references to DecoratedTypeMirror and TypeMirror to be XmlTypeMirror, and reference the known types.
    this.namespacesToPrefixes = ns2prefix;
    this.namespacesToSchemas = ns2schema;
    this.namespacesToWsdls = ns2wsdl;
    setVariable("ns2prefix", ns2prefix);
    setVariable("ns2schema", ns2schema);
    setVariable("ns2wsdl", ns2wsdl);
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
   */
  public void add(EndpointInterface ei) {
    String namespace = ei.getTargetNamespace();

    WsdlInfo wsdlInfo = namespacesToWsdls.get(namespace);
    if (wsdlInfo == null) {
      wsdlInfo = new WsdlInfo();
      namespacesToWsdls.put(namespace, wsdlInfo);
      wsdlInfo.setTargetNamespace(namespace);

      //todo: configure the schema info.
      //wsdlInfo.setSchemaInfo();

      //todo: configure whether to generate.
      //wsdlInfo.setGenerate();
    }

    wsdlInfo.getEndpointInterfaces().add(ei);
  }

  /**
   * Add a type definition to the model.
   *
   * @param typeDef The type definition to add to the model.
   */
  public void add(TypeDefinition typeDef) {
    String namespace = typeDef.getTargetNamespace();

    SchemaInfo schemaInfo = namespacesToSchemas.get(namespace);
    if (schemaInfo == null) {
      schemaInfo = new SchemaInfo();
      namespacesToSchemas.put(namespace, schemaInfo);
      schemaInfo.setNamespace(namespace);
    }

    schemaInfo.getTypeDefinitions().add(typeDef);

    int position = Collections.binarySearch(this.typeDefinitions, typeDef, CLASS_COMPARATOR);
    if (position < 0) {
      this.typeDefinitions.add(-position - 1, typeDef);
    }
  }

  /**
   * Add a root element to the model.
   *
   * @param rootElement The root element to add.
   */
  public void add(RootElementDeclaration rootElement) {
    String namespace = rootElement.getTargetNamespace();

    SchemaInfo schemaInfo = namespacesToSchemas.get(namespace);
    if (schemaInfo == null) {
      schemaInfo = new SchemaInfo();
      namespacesToSchemas.put(namespace, schemaInfo);
      schemaInfo.setNamespace(namespace);
    }

    schemaInfo.getGlobalElements().add(rootElement);
    int position = Collections.binarySearch(this.rootElements, rootElement, CLASS_COMPARATOR);
    if (position < 0) {
      this.rootElements.add(-position - 1, rootElement);
    }
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
        throw new ValidationException("An interface must not be annotated with @XmlType.");
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
  }
}
