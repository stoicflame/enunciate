package net.sf.enunciate.contract.validation;

import com.sun.mirror.declaration.*;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.TypeMirror;
import net.sf.enunciate.contract.jaxb.*;
import net.sf.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.enunciate.contract.jaxb.types.XmlClassType;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.jaxws.*;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

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

    if (!isAssignable((TypeDeclaration) delegate, (InterfaceDeclaration) impl.getEndpointInterface().getDelegate())) {
      result.addError(delegate.getPosition(), "Class does not implement its endpoint interface!");
    }

    return result;
  }

  /**
   * Whether declaration1 is assignable to declaration2.
   *
   * @return Whether declaration1 is assignable to declaration2.
   */
  protected boolean isAssignable(TypeDeclaration declaration1, InterfaceDeclaration declaration2) {
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

    if ((parameterStyle == SOAPBinding.ParameterStyle.BARE) && (soapBindingStyle != SOAPBinding.Style.DOCUMENT)) {
      result.addError(webMethod.getPosition(), "A BARE web method must have a DOCUMENT binding style.");
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

          if (type.isInstanceOf(Collection.class.getName())) {
            result.addWarning(webMethod.getPosition(), "The header return value that is an instance of java.util.Collection may not (de)serialize " +
              "correctly.  The spec is unclear as to how this should be handled.");
          }
        }
        else {
          WebParam webParam = (WebParam) webMessage;
          DecoratedTypeMirror type = (DecoratedTypeMirror) webParam.getType();

          if (type.isInstanceOf(Collection.class.getName())) {
            result.addWarning(webParam.getPosition(), "The header parameter that is an instance of java.util.Collection may not (de)serialize correctly.  " +
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
            if (paramType.isInstanceOf(Collection.class.getName())) {
              result.addWarning(webParam.getPosition(), "An instance of java.util.Collection as an RPC-style web message part may " +
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
   */
  public ValidationResult validateTypeDefinition(TypeDefinition typeDef) {
    ValidationResult result = validatePackage(typeDef.getSchema());

    if (isXmlTransient(typeDef)) {
      result.addError(typeDef.getPosition(), "XmlTransient type definition.");
    }

    XmlType xmlType = typeDef.getAnnotation(XmlType.class);

    boolean needsNoArgConstructor = (!(typeDef instanceof EnumTypeDefinition));
    if (needsNoArgConstructor && (xmlType != null)) {
      if ((typeDef.getDeclaringType() != null) && (!typeDef.getModifiers().contains(Modifier.STATIC))) {
        result.addError(typeDef.getPosition(), "An xml type must be either a top-level class or a nested static class.");
      }

      Class factoryClass = xmlType.factoryClass();
      String factoryMethod = xmlType.factoryMethod();

      if ((factoryClass != XmlType.DEFAULT.class) || (!"".equals(factoryMethod))) {
        needsNoArgConstructor = false;
        try {
          Method method = factoryClass.getMethod(factoryMethod);
          if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
            //todo: is this really a requirement?
            result.addError(typeDef.getPosition(), "'" + factoryMethod + "' must be a static, no-arg method on '" + factoryClass.getName() + "'.");
          }
        }
        catch (NoSuchMethodException e) {
          result.addError(typeDef.getPosition(), "Unknown factory method '" + factoryMethod + "' on class '" + factoryClass.getName() + "'.");
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

    for (Attribute attribute : typeDef.getAttributes()) {
      result.aggregate(validateAttribute(attribute));
    }

    if (typeDef.getValue() != null) {
      result.aggregate(validateValue(typeDef.getValue()));

      if (!typeDef.getElements().isEmpty()) {
        result.addError(typeDef.getValue().getPosition(), "A type definition cannot have both an xml value and child element(s).");
      }
    }
    else {
      for (Element element : typeDef.getElements()) {
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
      if (schemaType.type() == XmlSchemaType.DEFAULT.class) {
        result.addError(schema.getPosition(), "A type must be specified at the package-level for @XmlSchemaType.");
      }
    }

    XmlSchemaTypes schemaTypes = schema.getAnnotation(XmlSchemaTypes.class);
    if (schemaTypes != null) {
      for (XmlSchemaType xmlSchemaType : schemaTypes.value()) {
        if (xmlSchemaType.type() == XmlSchemaType.DEFAULT.class) {
          result.addError(schema.getPosition(), "A type must be specified at the package-level for all types of @XmlSchemaTypes.");
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

    if (elementRef.getChoices().isEmpty()) {
      XmlTypeMirror baseType = elementRef.getBaseType();
      result.addError(elementRef.getPosition(), "No root elements found for " + new QName(baseType.getNamespace(), baseType.getName()).toString() + ".");
    }

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

    if (accessor.getAnnotation(XmlIDREF.class) != null) {
      XmlTypeMirror baseType = accessor.getBaseType();
      if ((!(baseType instanceof XmlClassType)) || (((XmlClassType) baseType).getTypeDefinition().getXmlID() == null)) {
        result.addError(accessor.getPosition(), "An XML IDREF must have a base type that references another type that has an XML ID.");
      }
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
