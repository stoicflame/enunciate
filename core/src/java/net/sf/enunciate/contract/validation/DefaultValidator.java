package net.sf.enunciate.contract.validation;

import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;
import net.sf.enunciate.contract.jaxb.*;
import net.sf.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.enunciate.contract.jaxb.types.XmlClassType;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.enunciate.contract.rest.RESTMethod;
import net.sf.enunciate.contract.rest.RESTParameter;
import net.sf.enunciate.rest.annotations.VerbType;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * Default validator.
 *
 * @author Ryan Heaton
 */
public class DefaultValidator implements Validator {

  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = new ValidationResult();

    Declaration delegate = ei.getDelegate();

    WebService ws = delegate.getAnnotation(WebService.class);
    if (ws == null) {
      result.addError(delegate.getPosition(), "Not an endpoint interface: no WebService annotation");
    }
    else {
      if (((ei.getPackage() == null) || ("".equals(ei.getPackage().getQualifiedName()))) && (ei.getTargetNamespace() == null)) {
        result.addError(delegate.getPosition(), "An endpoint interface in no package must specify a target namespace.");
      }

      if ((ws.endpointInterface() != null) && (!"".equals(ws.endpointInterface()))) {
        result.addError(delegate.getPosition(), "Not an endpoint interface (it references another endpoint interface).");
      }
    }

    if (delegate instanceof AnnotationTypeDeclaration) {
      result.addError(delegate.getPosition(), "Annotation types are not valid endpoint interfaces.");
    }

    if (delegate instanceof EnumDeclaration) {
      result.addError(delegate.getPosition(), "Enums cannot be endpoint interfaces.");
    }

    TreeSet<WebMethod> uniquelyNamedWebMethods = new TreeSet<WebMethod>();
    for (WebMethod webMethod : ei.getWebMethods()) {
      if (!uniquelyNamedWebMethods.add(webMethod)) {
        result.addError(webMethod.getPosition(), "Web methods must have unique operation names.  Use annotations to disambiguate.");
      }

      result.aggregate(validateWebMethod(webMethod));
    }

    for (EndpointImplementation implementation : ei.getEndpointImplementations()) {
      result.aggregate(validateEndpointImplementation(implementation));
    }

    if (ei.getSoapUse() == SOAPBinding.Use.ENCODED) {
      result.addError(ei.getPosition(), "Enunciate does not support encoded-use web services.");
    }

    return result;
  }

  public ValidationResult validateEndpointImplementation(EndpointImplementation impl) {
    ValidationResult result = new ValidationResult();
    Declaration delegate = impl.getDelegate();

    WebService ws = delegate.getAnnotation(WebService.class);
    if (ws == null) {
      result.addError(delegate.getPosition(), "Not an endpoint implementation (no WebService annotation).");
    }

    if (delegate instanceof EnumDeclaration) {
      result.addError(delegate.getPosition(), "An enum cannot be an endpoint implementation.");
    }

    if (!isAssignable((TypeDeclaration) delegate, (TypeDeclaration) impl.getEndpointInterface().getDelegate())) {
      result.addError(delegate.getPosition(), "Class does not implement its endpoint interface!");
    }

    return result;
  }

  /**
   * Whether declaration1 is assignable to declaration2.
   *
   * @param declaration1 the first declaration.
   * @param declaration2 the second declaration.
   * @return Whether declaration1 is assignable to declaration2.
   */
  protected boolean isAssignable(TypeDeclaration declaration1, TypeDeclaration declaration2) {
    String iffqn = declaration2.getQualifiedName();
    if (declaration1.getQualifiedName().equals(iffqn)) {
      return true;
    }

    Collection<InterfaceType> superinterfaces = declaration1.getSuperinterfaces();
    for (InterfaceType interfaceType : superinterfaces) {
      InterfaceDeclaration declaration = interfaceType.getDeclaration();
      if ((declaration != null) && (isAssignable(declaration, declaration2))) {
        return true;
      }
    }

    return false;
  }

  /**
   * Does the default validation for a REST endpoint.
   *
   * @param restAPI The REST API
   * @return The result of the validation.
   */
  public ValidationResult validateRESTAPI(Map<String, List<RESTMethod>> restAPI) {
    ValidationResult result = new ValidationResult();

    for (String noun : restAPI.keySet()) {
      EnumSet<VerbType> verbs = EnumSet.noneOf(VerbType.class);
      List<RESTMethod> methods = restAPI.get(noun);
      for (RESTMethod method : methods) {
        VerbType verb = method.getVerb();
        if (!verbs.add(verb)) {
          result.addError(method.getPosition(), "Duplicate verb '" + verb + "' for REST noun '" + noun + "'.");
        }

        RESTParameter properNoun = method.getProperNoun();
        if (properNoun != null) {
          if (!properNoun.getXmlType().isSimple()) {
            result.addError(properNoun.getPosition(), "A proper noun must have a simple xml type.");
          }

          if (properNoun.isCollectionType()) {
            result.addError(properNoun.getPosition(), "A proper noun is not allowed to be a collection or an array.");
          }
        }

        HashSet<String> adjectives = new HashSet<String>();
        for (RESTParameter adjective : method.getAdjectives()) {
          if (!adjectives.add(adjective.getAdjectiveName())) {
            result.addError(adjective.getPosition(), "Duplicate adjective name '" + adjective.getAdjectiveName() + "'.");
          }

          if (!adjective.getXmlType().isSimple()) {
            result.addError(adjective.getPosition(), "An adjective must either be of simple xml type, or of a collection (or array) of simple xml types.");
          }
        }

        RESTParameter nounValue = method.getNounValue();
        if (nounValue != null) {
          if ((verb == VerbType.read) || (verb == VerbType.delete)) {
            result.addError(method.getPosition(), "The verbs 'read' and 'delete' do not support a noun value.");
          }

          XmlTypeMirror nounValueType = nounValue.getXmlType();
          if ((!(nounValueType instanceof XmlClassType)) || (((XmlClassType) nounValueType).getTypeDefinition().getAnnotation(XmlRootElement.class) == null)) {
            result.addError(nounValue.getPosition(), "A noun value must be a JAXB 2.0 root element.");
          }
        }
      }
    }

    return result;
  }

  public ValidationResult validateWebMethod(WebMethod webMethod) {
    ValidationResult result = new ValidationResult();
    if (!webMethod.getModifiers().contains(Modifier.PUBLIC)) {
      result.addError(webMethod.getPosition(), "A non-public method cannot be a web method.");
    }

    javax.jws.WebMethod annotation = webMethod.getAnnotation(javax.jws.WebMethod.class);
    if ((annotation != null) && (annotation.exclude())) {
      result.addError(webMethod.getPosition(), "A method marked as excluded cannot be a web method.");
    }

    if (webMethod.getSoapUse() == SOAPBinding.Use.ENCODED) {
      result.addError(webMethod.getPosition(), "Enunciate doesn't support ENCODED-use web methods.");
    }

    int inParams = 0;
    int outParams = 0;
    boolean oneway = webMethod.isOneWay();
    SOAPBinding.ParameterStyle parameterStyle = webMethod.getSoapParameterStyle();
    SOAPBinding.Style soapBindingStyle = webMethod.getSoapBindingStyle();

    if (oneway && (!(webMethod.getReturnType() instanceof VoidType))) {
      result.addError(webMethod.getPosition(), "A one-way method must have a void return type.");
    }

    if ((parameterStyle == SOAPBinding.ParameterStyle.BARE) && (soapBindingStyle != SOAPBinding.Style.DOCUMENT)) {
      result.addError(webMethod.getPosition(), "A BARE web method must have a DOCUMENT binding style.");
    }

    for (WebParam webParam : webMethod.getWebParameters()) {
      if ((webParam.getMode() == javax.jws.WebParam.Mode.INOUT) && (!webParam.isHolder())) {
        result.addError(webParam.getPosition(), "An INOUT parameter must have a type of javax.xml.ws.Holder");
      }
    }

    for (WebMessage webMessage : webMethod.getMessages()) {
      if (oneway && webMessage.isOutput()) {
        result.addError(webMethod.getPosition(), "A one-way method cannot have any 'out' messages (i.e. non-void return values, thrown exceptions, " +
          "out parameters, or in/out parameters).");
      }

      if (!webMessage.isHeader()) {
        inParams = webMessage.isInput() ? inParams + 1 : inParams;
        outParams = webMessage.isOutput() ? outParams + 1 : outParams;
      }
      else {
        //if it's a header, it's either a web result or a web param.
        if (webMessage instanceof WebResult) {
          DecoratedTypeMirror type = (DecoratedTypeMirror) ((WebResult) webMessage).getType();

          if ((type.isCollection()) || (type.isArray())) {
            String description = type.isCollection() ? "an instance of java.util.Collection" : "an array";
            result.addWarning(webMethod.getPosition(), "The header return value that is " + description + " may not (de)serialize " +
              "correctly.  The spec is unclear as to how this should be handled.");
          }
        }
        else {
          WebParam webParam = (WebParam) webMessage;
          DecoratedTypeMirror type = (DecoratedTypeMirror) webParam.getType();

          if (type.isCollection() || (type.isArray())) {
            String description = type.isCollection() ? "an instance of java.util.Collection" : "an array";
            result.addWarning(webParam.getPosition(), "The header parameter that is " + description + " may not (de)serialize correctly.  " +
              "The spec is unclear as to how this should be handled.");
          }
        }
      }

      if (parameterStyle == SOAPBinding.ParameterStyle.BARE) {
        if (webMessage instanceof WebParam) {
          DecoratedTypeMirror paramType = (DecoratedTypeMirror) ((WebParam) webMessage).getType();
          if (paramType.isArray()) {
            result.addError(webMethod.getPosition(), "A BARE web method must not have an array as a parameter.");
          }
        }
        else if (webMessage instanceof RequestWrapper) {
          //todo: throw a runtime exception?  This is a problem with the engine, not the user.
          result.addError(webMethod.getPosition(), "A BARE web method shouldn't have a request wrapper.");
        }
        else if (webMessage instanceof ResponseWrapper) {
          //todo: throw a runtime exception?  This is a problem with the engine, not the user.
          result.addError(webMethod.getPosition(), "A BARE web method shouldn't have a response wrapper.");
        }

        if (inParams > 1) {
          result.addError(webMethod.getPosition(), "A BARE web method must not have more than one 'in' parameter.");
        }

        if (outParams > 1) {
          result.addError(webMethod.getPosition(), "A BARE web method must not have more than one 'out' message (i.e. non-void return values, " +
            "thrown exceptions, out parameters, or in/out parameters).");
        }
      }
      else if (soapBindingStyle == SOAPBinding.Style.RPC) {
        Collection<WebMessagePart> parts = webMessage.getParts();
        for (WebMessagePart part : parts) {
          if (part instanceof WebParam) {
            WebParam webParam = (WebParam) part;
            DecoratedTypeMirror paramType = (DecoratedTypeMirror) webParam.getType();
            if (paramType.isCollection() || paramType.isArray()) {
              String description = paramType.isCollection() ? "An instance of java.util.Collection" : "An array";
              result.addWarning(webParam.getPosition(), description + " as an RPC-style web message part may " +
                "not be (de)serialized as you expect.  The spec is unclear as to how this should be handled.");
            }
          }
        }
      }
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = validateTypeDefinition(complexType);

    XmlTypeMirror baseType = complexType.getBaseType();
    while ((baseType instanceof XmlClassType) && (((XmlClassType) baseType).getTypeDefinition() instanceof ComplexTypeDefinition)) {
      ComplexTypeDefinition superType = (ComplexTypeDefinition) ((XmlClassType) baseType).getTypeDefinition();
      baseType = superType.getBaseType();

      if (superType.getValue() != null) {
        result.addError(complexType.getPosition(), "A complex type cannot subclass another complex type (" + superType.getQualifiedName() +
          ") that has an xml value.");
      }

      //we don't have to recurse into the superclasses because we will have already validated (or will validate them) later.
    }

    if (complexType.getValue() != null) {
      if (!complexType.getElements().isEmpty()) {
        result.addError(complexType.getPosition(), "A type definition cannot have both an xml value and elements.");
      }
      else if (complexType.getAttributes().isEmpty()) {
        //todo: throw a runtime exception? This is really a problem with the engine, not the user code.
        result.addError(complexType.getPosition(), "Should be a simple type, not a complex type.");
      }
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    ValidationResult result = validateTypeDefinition(simpleType);

    XmlTypeMirror baseType = simpleType.getBaseType();
    if (baseType == null) {
      result.addError(simpleType.getPosition(), "No base type specified.");
    }
    else if ((baseType instanceof XmlClassType) && (((XmlClassType) baseType).getTypeDefinition() instanceof ComplexTypeDefinition)) {
      result.addError(simpleType.getPosition(), "A simple type must have a simple base type. " + new QName(baseType.getNamespace(), baseType.getName())
        + " is a complex type.");
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateEnumType(EnumTypeDefinition enumType) {
    return validateSimpleType(enumType);
  }

  /**
   * Validation logic common to all type definitions.
   *
   * @param typeDef The type definition to validate.
   * @return The validation result.
   */
  public ValidationResult validateTypeDefinition(TypeDefinition typeDef) {
    ValidationResult result = validatePackage(typeDef.getSchema());

    if (isXmlTransient(typeDef)) {
      result.addError(typeDef.getPosition(), "XmlTransient type definition.");
    }

    XmlType xmlType = typeDef.getAnnotation(XmlType.class);

    if ((typeDef.getDeclaringType() != null) && (!typeDef.getModifiers().contains(Modifier.STATIC))) {
      result.addError(typeDef.getPosition(), "An xml type must be either a top-level class or a nested static class.");
    }

    boolean needsNoArgConstructor = (!(typeDef instanceof EnumTypeDefinition));
    if (needsNoArgConstructor && (xmlType != null)) {
      String factoryClassFqn = null;
      try {
        Class factoryClass = xmlType.factoryClass();
        if (factoryClass != XmlType.DEFAULT.class) {
          factoryClassFqn = factoryClass.getName();
        }
      }
      catch (MirroredTypeException e) {
        TypeMirror typeMirror = e.getTypeMirror();
        if (!(typeMirror instanceof DeclaredType)) {
          result.addError(typeDef.getPosition(), "Unsupported factory class: " + typeMirror);
        }
        factoryClassFqn = ((DeclaredType) typeMirror).getDeclaration().getQualifiedName();
      }

      String factoryMethod = xmlType.factoryMethod();

      if ((factoryClassFqn != null) || (!"".equals(factoryMethod))) {
        needsNoArgConstructor = false;
        TypeDeclaration factoryDeclaration = Context.getCurrentEnvironment().getTypeDeclaration(factoryClassFqn);
        Collection<? extends MethodDeclaration> methods = factoryDeclaration.getMethods();
        boolean methodFound = false;
        for (MethodDeclaration method : methods) {
          if ((method.getSimpleName().equals(factoryMethod)) && (method.getParameters().size() == 0) && (method.getModifiers().contains(Modifier.STATIC))) {
            methodFound = true;
            break;
          }
        }

        if (!methodFound) {
          result.addError(typeDef.getPosition(), "A static, parameterless factory method named " + factoryMethod + " was not found on " + factoryClassFqn);
        }
      }
      else if (typeDef.getAnnotation(XmlJavaTypeAdapter.class) != null) {
        needsNoArgConstructor = false;
        //todo: validate that this is a valid type adapter?
      }

      String[] propOrder = xmlType.propOrder();
      if ((propOrder.length > 0) && (!"".equals(propOrder[0]))) {
        //todo: validate that all properties and fields are accounted for in the propOrder list.
      }
    }

    if (needsNoArgConstructor) {
      //check for a zero-arg constructor...
      boolean hasNoArgConstructor = false;
      Collection<ConstructorDeclaration> constructors = typeDef.getConstructors();
      for (ConstructorDeclaration constructor : constructors) {
        if ((constructor.getModifiers().contains(Modifier.PUBLIC)) && (constructor.getParameters().size() == 0)) {
          hasNoArgConstructor = true;
          break;
        }
      }

      if (!hasNoArgConstructor) {
        result.addError(typeDef.getPosition(), "A TypeDefinition must have a public no-arg constructor or be annotated with a factory method.");
      }
    }

    HashMap<QName, Attribute> attributeNames = new HashMap<QName, Attribute>();
    for (Attribute attribute : typeDef.getAttributes()) {
      QName attributeQName = new QName(attribute.getNamespace(), attribute.getName());
      Attribute sameName = attributeNames.put(attributeQName, attribute);
      if (sameName != null) {
        result.addError(attribute.getPosition(), "Attribute has the same name (" + attributeQName + ") as " + sameName.getPosition()
          + ".  Please use annotations to disambiguate.");
        //todo: this check should really be global....
      }

      result.aggregate(validateAttribute(attribute));
    }

    if (typeDef.getValue() != null) {
      result.aggregate(validateValue(typeDef.getValue()));

      if (!typeDef.getElements().isEmpty()) {
        result.addError(typeDef.getValue().getPosition(), "A type definition cannot have both an xml value and child element(s).");
      }
    }
    else {
      HashMap<QName, HashMap<QName, Element>> elementNames = new HashMap<QName, HashMap<QName, Element>>();
      for (Element element : typeDef.getElements()) {
        for (Element choice : element.getChoices()) {
          QName wrapperQName = null;
          if (choice.isWrapped()) {
            wrapperQName = new QName(choice.getWrapperNamespace(), choice.getWrapperName());
          }

          HashMap<QName, Element> choiceNames = elementNames.get(wrapperQName);
          if (choiceNames == null) {
            choiceNames = new HashMap<QName, Element>();
            elementNames.put(wrapperQName, choiceNames);
          }

          //todo: this check should really be global, including supertypes.
          QName choiceQName = new QName(choice.getNamespace(), choice.getName());
          Element sameName = choiceNames.put(choiceQName, choice);
          if (sameName != null) {
            result.addError(choice.getPosition(), "Element (or element choice) has the same name (" + choiceQName + ") as " + sameName.getPosition()
              + ".  Please use annotations to disambiguate.");
          }
          else if ((wrapperQName == null) && (elementNames.containsKey(choiceQName))) {
            result.addError(choice.getPosition(), "Element (or element choice) has the same name (" + choiceQName + ") as element wrapper for " +
              elementNames.get(choiceQName).values().iterator().next().getPosition() + ".  Please use annotations to disambiguate.");
          }
          else if ((wrapperQName != null) && (elementNames.containsKey(null) && (elementNames.get(null).containsKey(wrapperQName)))) {
            result.addError(element.getPosition(), "Wrapper for element has the same name (" + wrapperQName + ") as " +
              elementNames.get(null).get(wrapperQName).getPosition() + ". Please use annotations to disambiguate.");
          }

          if (wrapperQName != null) {
            //todo: is it worth it to validate that wrapper names are unique across different member declarations?
          }
        }

        if (element instanceof ElementRef) {
          result.aggregate(validateElementRef((ElementRef) element));
        }
        else {
          result.aggregate(validateElement(element));
        }
      }

    }

    if (typeDef.getXmlID() != null) {
      validateXmlID(typeDef.getXmlID());
    }

    return result;
  }

  public ValidationResult validateRootElement(RootElementDeclaration rootElementDeclaration) {
    return new ValidationResult();
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

  public ValidationResult validatePackage(Schema schema) {
    ValidationResult result = new ValidationResult();

    XmlSchemaType schemaType = schema.getAnnotation(XmlSchemaType.class);
    if (schemaType != null) {
      try {
        if (schemaType.type() == XmlSchemaType.DEFAULT.class) {
          result.addError(schema.getPosition(), "A type must be specified at the package-level for @XmlSchemaType.");
        }
      }
      catch (MirroredTypeException e) {
        //fall through.  Implies the type was set.
      }
    }

    XmlSchemaTypes schemaTypes = schema.getAnnotation(XmlSchemaTypes.class);
    if (schemaTypes != null) {
      for (XmlSchemaType xmlSchemaType : schemaTypes.value()) {
        try {
          if (xmlSchemaType.type() == XmlSchemaType.DEFAULT.class) {
            result.addError(schema.getPosition(), "A type must be specified at the package-level for all types of @XmlSchemaTypes.");
          }
        }
        catch (MirroredTypeException e) {
          //fall through.  Implies the type was set.
        }
      }
    }

    return result;
  }

  public ValidationResult validateAttribute(Attribute attribute) {
    ValidationResult result = validateAccessor(attribute);

    XmlTypeMirror baseType = attribute.getBaseType();
    if (baseType == null) {
      result.addError(attribute.getPosition(), "No base type specified.");
    }
    else if ((baseType instanceof XmlClassType) && (((XmlClassType) baseType).getTypeDefinition() instanceof ComplexTypeDefinition)) {
      result.addError(attribute.getPosition(), "An attribute must have a simple base type. " + new QName(baseType.getNamespace(), baseType.getName())
        + " is a complex type.");
    }
    else if (attribute.isBinaryData()) {
      result.addError(attribute.getPosition(), "Attributes can't have binary data.");
    }

    boolean qualified = attribute.getTypeDefinition().getSchema().getAttributeFormDefault() == XmlNsForm.QUALIFIED;
    String typeNamespace = attribute.getTypeDefinition().getNamespace();
    typeNamespace = typeNamespace == null ? "" : typeNamespace;
    String attributeNamespace = attribute.getNamespace();
    attributeNamespace = attributeNamespace == null ? "" : attributeNamespace;
    if ((qualified) && (!attributeNamespace.equals(typeNamespace))) {
      result.addError(attribute.getPosition(), "Enunciate doesn't support attributes of a different namespace than their containing type definition if " +
        "their form is qualified.  Use an attribute ref.");
    }
    else if ((!qualified) && (!"".equals(attributeNamespace))) {
      result.addError(attribute.getPosition(), "Enunciate only supports the default namespace on attributes that have an unqualified form.");
    }

    return result;
  }

  public ValidationResult validateElement(Element element) {
    ValidationResult result = validateAccessor(element);

    XmlElements xmlElements = element.getAnnotation(XmlElements.class);
    if ((element.isCollectionType()) && (element.getBaseType() != KnownXmlType.ANY_TYPE) &&
      (xmlElements != null) && (xmlElements.value() != null) && (xmlElements.value().length > 1)) {
      result.addError(element.getPosition(),
                      "A parameterized collection accessor cannot be annotated with XmlElements that has a value with a length greater than one.");
    }

    boolean qualified = element.getTypeDefinition().getSchema().getElementFormDefault() == XmlNsForm.QUALIFIED;

    QName ref = element.getRef();
    if (ref == null) {
      String typeNamespace = element.getTypeDefinition().getNamespace();
      typeNamespace = typeNamespace == null ? "" : typeNamespace;
      String elementNamespace = element.getNamespace();
      elementNamespace = elementNamespace == null ? "" : elementNamespace;
      if ((qualified) && (!elementNamespace.equals(typeNamespace))) {
        result.addError(element.getPosition(), "Enunciate doesn't support elements of a different namespace than their containing type definition if " +
          "their form is qualified.  Use an element ref.");
      }
      else if ((!qualified) && (!"".equals(elementNamespace))) {
        result.addError(element.getPosition(), "Enunciate only supports the default namespace on elements that have an unqualified form.");
      }
    }

    if (element.isWrapped()) {
      String wrapperNamespace = element.getWrapperNamespace();
      wrapperNamespace = wrapperNamespace == null ? "" : wrapperNamespace;
      String typeNamespace = element.getTypeDefinition().getNamespace();
      typeNamespace = typeNamespace == null ? "" : typeNamespace;
      if ((qualified) && (!wrapperNamespace.equals(typeNamespace))) {
        result.addError(element.getPosition(), "Enunciate doesn't support element wrappers of different namespaces than their type definitions if their " +
          "form is qualified.");
      }
      else if ((!qualified) && (!"".equals(wrapperNamespace))) {
        result.addError(element.getPosition(), "Enunciate only supports the default namespace on wrapper elements that have an unqualified form.");
      }
    }

    return result;
  }

  public ValidationResult validateValue(Value value) {
    ValidationResult result = validateAccessor(value);

    XmlTypeMirror baseType = value.getBaseType();
    if (baseType == null) {
      result.addError(value.getPosition(), "No base type specified.");
    }
    else if ((baseType instanceof XmlClassType) && (((XmlClassType) baseType).getTypeDefinition() instanceof ComplexTypeDefinition)) {
      result.addError(value.getPosition(), "An xml value must have a simple base type. " + new QName(baseType.getNamespace(), baseType.getName())
        + " is a complex type.");
    }

    return result;
  }

  public ValidationResult validateElementRef(ElementRef elementRef) {
    ValidationResult result = validateAccessor(elementRef);

    if ((elementRef.getAnnotation(XmlElement.class) != null) || (elementRef.getAnnotation(XmlElements.class) != null)) {
      result.addError(elementRef.getPosition(), "The xml element ref cannot be annotated also with XmlElement or XmlElements.");
    }

    return result;
  }

  public ValidationResult validateAccessor(Accessor accessor) {
    ValidationResult result = new ValidationResult();

    if (accessor.getDelegate() instanceof PropertyDeclaration) {
      PropertyDeclaration property = (PropertyDeclaration) accessor.getDelegate();
      DecoratedMethodDeclaration getter = property.getGetter();
      DecoratedMethodDeclaration setter = property.getSetter();

      if ((getter != null) && (setter != null)) {
        //find all JAXB annotations that are on both the setter and the getter...
        Map<String, AnnotationMirror> getterAnnotations = getter.getAnnotations();
        Map<String, AnnotationMirror> setterAnnotations = setter.getAnnotations();
        for (String annotation : getterAnnotations.keySet()) {
          if ((annotation.startsWith(XmlElement.class.getPackage().getName())) && (setterAnnotations.containsKey(annotation))) {
            result.addError(setter.getPosition(), "'" + annotation + "' is on both the getter and setter.");
          }
        }
      }
      else {
        result.addError(accessor.getPosition(), "A property accessor needs both a setter and a getter.");
      }
    }

    if ((accessor.isXmlIDREF()) && (accessor.getAccessorForXmlID() == null)) {
      result.addError(accessor.getPosition(), "An XML IDREF must have a base type that references another type that has an XML ID.");
    }

    return result;
  }

  public ValidationResult validateXmlID(Accessor accessor) {
    ValidationResult result = new ValidationResult();

    TypeMirror accessorType = accessor.getAccessorType();
    if (!(accessorType instanceof DeclaredType) || !((DeclaredType) accessorType).getDeclaration().getQualifiedName().startsWith(String.class.getName())) {
      result.addError(accessor.getPosition(), "An xml id must be a string.");
    }

    return result;
  }
}
