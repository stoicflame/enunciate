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

package org.codehaus.enunciate.contract.validation;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedDeclaredType;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxrs.*;
import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlClassType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.qname.XmlQNameEnum;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.*;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.*;

/**
 * Default validator.
 *
 * @author Ryan Heaton
 */
public class DefaultValidator extends BaseValidator implements ConfigurableRules {

  private final Set<String> disabledRules = new TreeSet<String>();

  public void disableRules(Set<String> ruleIds) {
    if (ruleIds != null) {
      this.disabledRules.addAll(ruleIds);
    }
  }

  @Override
  public ValidationResult validate(EnunciateFreemarkerModel model) {
    ValidationResult result = super.validate(model);

    //validate unique content type ids.
    Set<String> uniqueContentTypeIds = new TreeSet<String>();
    for (String contentType : model.getContentTypesToIds().keySet()) {
      String id = model.getContentTypesToIds().get(contentType);
      if (!uniqueContentTypeIds.add(id)) {
        StringBuilder builder = new StringBuilder("All content types must have unique ids.  The id '").
          append(id).append("' is assigned to the following content types: '").append(contentType).append("'");
        for (String ct : model.getContentTypesToIds().keySet()) {
          if (!contentType.equals(ct) && (id.equals(model.getContentTypesToIds().get(ct)))) {
            builder.append(", '").append(ct).append("'");
          }
        }
        builder.append(". Please use the Enunciate configuration to specify a unique id for each content type.");
        result.addError((Declaration) null, builder.toString());
        break;
      }
    }

    return result;
  }

  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = new ValidationResult();

    if ((ei.getEndpointImplementations() == null || (ei.getEndpointImplementations().isEmpty()))) {
      result.addWarning(ei, "Endpoint interface has no implementations!  It will NOT be deployed...");
    }

    Declaration delegate = ei.getDelegate();

    WebService ws = delegate.getAnnotation(WebService.class);
    if (ws == null) {
      result.addError(delegate, "Not an endpoint interface: no WebService annotation");
    }
    else {
      if (((ei.getPackage() == null) || ("".equals(ei.getPackage().getQualifiedName()))) && (ei.getTargetNamespace() == null)) {
        result.addError(delegate, "An endpoint interface in no package must specify a target namespace.");
      }

      if ((ws.endpointInterface() != null) && (!"".equals(ws.endpointInterface()))) {
        result.addError(delegate, "Not an endpoint interface (it references another endpoint interface).");
      }
    }

    if (delegate instanceof AnnotationTypeDeclaration) {
      result.addError(delegate, "Annotation types are not valid endpoint interfaces.");
    }

    if (delegate instanceof EnumDeclaration) {
      result.addError(delegate, "Enums cannot be endpoint interfaces.");
    }

    WebMethod styleHead = null;
    TreeSet<WebMethod> uniquelyNamedWebMethods = new TreeSet<WebMethod>();
    for (WebMethod webMethod : ei.getWebMethods()) {
      if (styleHead == null) {
        styleHead = webMethod;
      }
      else if (styleHead.getSoapBindingStyle() != webMethod.getSoapBindingStyle()) {
        result.addError(webMethod, "Mixed-style endpoint interfaces break conformity to the WS-I Basic Profile.  The '" + webMethod.getSimpleName() +
          "' method has " + webMethod.getSoapBindingStyle() + " style, which isn't the same as the " + styleHead.getSimpleName() +
          " method  on the endpoint which has '" + styleHead.getSoapBindingStyle() + "' style.");
      }

      if (!uniquelyNamedWebMethods.add(webMethod)) {
        result.addError(webMethod, "Web methods must have unique operation names.  Use annotations to disambiguate.");
      }

      result.aggregate(validateWebMethod(webMethod));
    }

    for (EndpointImplementation implementation : ei.getEndpointImplementations()) {
      result.aggregate(validateEndpointImplementation(implementation));
    }

    if (ei.getSoapUse() == SOAPBinding.Use.ENCODED) {
      result.addError(ei, "Enunciate does not support encoded-use web services.");
    }

    return result;
  }

  public ValidationResult validateEndpointImplementation(EndpointImplementation impl) {
    ValidationResult result = new ValidationResult();
    Declaration delegate = impl.getDelegate();

    WebService ws = delegate.getAnnotation(WebService.class);
    if (ws == null) {
      result.addError(delegate, "Not an endpoint implementation (no WebService annotation).");
    }

    if (delegate instanceof EnumDeclaration) {
      result.addError(delegate, "An enum cannot be an endpoint implementation.");
    }

    if (!isAssignable((TypeDeclaration) delegate, (TypeDeclaration) impl.getEndpointInterface().getDelegate())) {
      result.addError(delegate, "Class does not implement its endpoint interface!");
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

  public ValidationResult validateRootResources(List<RootResource> rootResources) {
    ValidationResult result = new ValidationResult();
    for (RootResource rootResource : rootResources) {
      for (FieldDeclaration field : rootResource.getFields()) {
        if (isSuppliableByJAXRS(field) && ((field.getAnnotation(javax.ws.rs.core.Context.class) == null) && !isConvertableToStringByJAXRS(field.getType()))) {
          result.addError(field, "Unsupported JAX-RS type.");
        }
      }

      for (PropertyDeclaration prop : rootResource.getProperties()) {
        if (isSuppliableByJAXRS(prop) && ((prop.getAnnotation(javax.ws.rs.core.Context.class) == null) && !isConvertableToStringByJAXRS(prop.getPropertyType()))) {
          result.addError(prop.getSetter(), "Unsupported JAX-RS type.");
        }
      }

      for (ResourceMethod resourceMethod : rootResource.getResourceMethods(true)) {
        if (resourceMethod.getDeclaredEntityParameters().size() > 1) {
          result.addError(resourceMethod, "No more than one JAX-RS entity parameter is allowed (all other parameters must be annotated with one of the JAX-RS resource parameter annotations).");
        }

        int formParamCount = 0;
        for (ResourceParameter resourceParameter : resourceMethod.getResourceParameters()) {
          if (resourceParameter.isFormParam()) {
            formParamCount++;
          }
        }

        ResourceEntityParameter entityParam = resourceMethod.getEntityParameter();
        if (entityParam != null && (formParamCount > 0)) {
          DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(entityParam.getType());
          if (!decorated.isInstanceOf(MultivaluedMap.class.getName())) {
            result.addError(entityParam, "An entity parameter must be of type MultivaluedMap<String, String> if there is another parameter annotated with @FormParam.");
          }
        }

        //todo: warn about resource methods that are not public?
        //todo: error out with ambiguous resource methods (produce same thing at same path with same method)?
      }
    }
    return result;
  }

  /**
   * Whether the specified type is convertable from a String according to JAX-RS.
   *
   * @param type The type.
   * @return Whether it's convertable.
   */
  protected boolean isConvertableToStringByJAXRS(TypeMirror type) {
    //unwrap the lists first.
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(type);
    if (decorated.isInstanceOf("java.util.List") || decorated.isInstanceOf("java.util.Set") || decorated.isInstanceOf("java.util.SortedSet")) {
      Collection<TypeMirror> typeArgs = ((DeclaredType) type).getActualTypeArguments();
      if (typeArgs != null && typeArgs.size() == 1) {
        type = typeArgs.iterator().next();
      }
      else {
        return false;
      }
    }

    if (type instanceof PrimitiveType) {
      return true;
    }
    else if (isString(type)) {
      return true;
    }
    else if (type instanceof DeclaredType) {
      TypeDeclaration declaration = ((DeclaredType) type).getDeclaration();
      if (declaration != null) {
        if (declaration instanceof ClassDeclaration) {
          for (ConstructorDeclaration constructor : ((ClassDeclaration) declaration).getConstructors()) {
            if (constructor.getParameters().size() == 1) {
              if (isString(constructor.getParameters().iterator().next().getType())) {
                return true;
              }
            }
          }
        }

        for (MethodDeclaration method : declaration.getMethods()) {
          if (method.getModifiers().contains(Modifier.STATIC) && "valueOf".equals(method.getSimpleName()) &&
        		  method.getReturnType().equals(type) &&
        		  method.getParameters().size() == 1 && isString(method.getParameters().iterator().next().getType())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Whether the type is a string.
   *
   * @param type The type.
   * @return Whether the type is a string.
   */
  protected boolean isString(TypeMirror type) {
    return type != null
      && type instanceof DeclaredType 
      && ((DeclaredType) type).getDeclaration() != null
      && String.class.getName().equals(((DeclaredType) type).getDeclaration().getQualifiedName());
  }

  /**
   * Whether the specified declaration is suppliable by JAX-RS.
   *
   * @param declaration The declaration.
   * @return Whether the specified declaration is suppliable by JAX-RS.
   */
  protected boolean isSuppliableByJAXRS(Declaration declaration) {
    return (declaration.getAnnotation(MatrixParam.class) != null)
        || (declaration.getAnnotation(PathParam.class) != null)
        || (declaration.getAnnotation(QueryParam.class) != null)
        || (declaration.getAnnotation(CookieParam.class) != null)
        || (declaration.getAnnotation(HeaderParam.class) != null)
        || (declaration.getAnnotation(javax.ws.rs.core.Context.class) != null);
  }

  public ValidationResult validateWebMethod(WebMethod webMethod) {
    ValidationResult result = new ValidationResult();
    if (!webMethod.getModifiers().contains(Modifier.PUBLIC)) {
      result.addError(webMethod, "A non-public method cannot be a web method.");
    }

    javax.jws.WebMethod annotation = webMethod.getAnnotation(javax.jws.WebMethod.class);
    if ((annotation != null) && (annotation.exclude())) {
      result.addError(webMethod, "A method marked as excluded cannot be a web method.");
    }

    if (webMethod.getSoapUse() == SOAPBinding.Use.ENCODED) {
      result.addError(webMethod, "Enunciate doesn't support ENCODED-use web methods.");
    }

    Collection<WebMessage> inParams = new ArrayList<WebMessage>();
    Collection<WebMessage> outParams = new ArrayList<WebMessage>();
    boolean oneway = webMethod.isOneWay();
    SOAPBinding.ParameterStyle parameterStyle = webMethod.getSoapParameterStyle();
    SOAPBinding.Style soapBindingStyle = webMethod.getSoapBindingStyle();

    if (oneway && (!(webMethod.getReturnType() instanceof VoidType))) {
      result.addError(webMethod, "A one-way method must have a void return type.");
    }

    if (oneway && webMethod.getThrownTypes() != null && !webMethod.getThrownTypes().isEmpty()) {
      result.addError(webMethod, "A one-way method can't throw any exceptions.");
    }

    if ((parameterStyle == SOAPBinding.ParameterStyle.BARE) && (soapBindingStyle != SOAPBinding.Style.DOCUMENT)) {
      result.addError(webMethod, "A BARE web method must have a DOCUMENT binding style.");
    }

    for (WebParam webParam : webMethod.getWebParameters()) {
      if ((webParam.getMode() == javax.jws.WebParam.Mode.INOUT) && (!webParam.isHolder())) {
        result.addError(webParam, "An INOUT parameter must have a type of javax.xml.ws.Holder");
      }
    }

    for (WebMessage webMessage : webMethod.getMessages()) {
      if (oneway && webMessage.isOutput()) {
        result.addError(webMethod, "A one-way method cannot have any 'out' messages (i.e. non-void return values, thrown exceptions, " +
          "out parameters, or in/out parameters).");
      }

      if (!webMessage.isHeader()) {
        if (webMessage.isInput()) {
          inParams.add(webMessage);
        }

        if (webMessage.isOutput()) {
          outParams.add(webMessage);
        }
      }
      else {
        //if it's a header, it's either a web result or a web param.
        if (webMessage instanceof WebResult) {
          WebResult webResult = (WebResult) webMessage;
          if ("".equals(webResult.getElementName())) {
            result.addError(webResult.getWebMethod(), "A web result that is a header must specify a name with the @WebResult annotation.");
          }
          DecoratedTypeMirror type = (DecoratedTypeMirror) webResult.getType();

          if ((type.isCollection()) || (type.isArray())) {
            String description = type.isCollection() ? "an instance of java.util.Collection" : "an array";
            result.addWarning(webMethod, "The header return value that is " + description + " may not (de)serialize " +
              "correctly.  The spec is unclear as to how this should be handled.");
          }
        }
        else {
          WebParam webParam = (WebParam) webMessage;
          if ("".equals(webParam.getElementName())) {
            result.addError(webParam, "A header parameter must specify a name using the @WebParam annotation.");
          }

          DecoratedTypeMirror type = (DecoratedTypeMirror) webParam.getType();

          if (type.isCollection() || (type.isArray())) {
            String description = type.isCollection() ? "an instance of java.util.Collection" : "an array";
            result.addWarning(webParam, "The header parameter that is " + description + " may not (de)serialize correctly.  " +
              "The spec is unclear as to how this should be handled.");
          }
        }
      }

      if (parameterStyle == SOAPBinding.ParameterStyle.BARE) {
        if (webMessage instanceof WebParam) {
          DecoratedTypeMirror paramType = (DecoratedTypeMirror) ((WebParam) webMessage).getType();
          if (paramType.isArray()) {
            result.addError(webMethod, "A BARE web method must not have an array as a parameter.");
          }
        }
        else if (webMessage instanceof RequestWrapper) {
          //todo: throw a runtime exception?  This is a problem with the engine, not the user.
          result.addError(webMethod, "A BARE web method shouldn't have a request wrapper.");
        }
        else if (webMessage instanceof ResponseWrapper) {
          //todo: throw a runtime exception?  This is a problem with the engine, not the user.
          result.addError(webMethod, "A BARE web method shouldn't have a response wrapper.");
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
              result.addWarning(webParam, description + " as an RPC-style web message part may " +
                "not be (de)serialized as you expect.  The spec is unclear as to how this should be handled.");
            }
          }
        }
      }
    }

    if (parameterStyle == SOAPBinding.ParameterStyle.BARE) {
      if (inParams.size() > 1) {
        result.addError(webMethod, "A BARE web method must not have more than one 'in' parameter.");
      }
      else if (inParams.isEmpty()) {
        result.addWarning(webMethod, "A BARE web method should have one IN parameter.");
      }

      if (outParams.size() > 1) {
        result.addError(webMethod, "A BARE web method must not have more than one 'out' message (i.e. non-void return values, " +
          "out parameters, or in/out parameters).");
      }
      else if (outParams.isEmpty() && !webMethod.isOneWay()) {
        result.addError(webMethod, "A BARE web method that is not one-way must have one OUT parameter.");
      }
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = validateTypeDefinition(complexType);

    try {
      complexType.getBaseType();
    }
    catch (ValidationException e) {
      result.addError(complexType, e.getMessage());
    }

    if (complexType.getValue() != null) {
      if (!complexType.isBaseObject()) {
        result.addError(complexType, "A type with an @XmlValue must not extend another object (other than java.lang.Object).");
      }

      if (!complexType.getElements().isEmpty()) {
        result.addError(complexType, "A type definition cannot have both an xml value and elements.");
      }
      else if (complexType.getAttributes().isEmpty()) {
        //todo: throw a runtime exception? This is really a problem with the engine, not the user code.
        result.addError(complexType, "Should be a simple type, not a complex type.");
      }
    }

    return result;
  }

  // Inherited.
  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    ValidationResult result = validateTypeDefinition(simpleType);

    try {
      XmlType baseType = simpleType.getBaseType();
      if (baseType == null) {
        result.addError(simpleType, "No base type specified.");
      }
      else if ((baseType instanceof XmlClassType) && (((XmlClassType) baseType).getTypeDefinition() instanceof ComplexTypeDefinition)) {
        result.addError(simpleType, "A simple type must have a simple base type. " + new QName(baseType.getNamespace(), baseType.getName())
          + " is a complex type.");
      }
    }
    catch (ValidationException e) {
      result.addError(simpleType, e.getMessage());
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
      result.addError(typeDef, "XmlTransient type definition.");
    }

    javax.xml.bind.annotation.XmlType xmlType = typeDef.getAnnotation(javax.xml.bind.annotation.XmlType.class);

    if ((typeDef.getDeclaringType() != null) && (!typeDef.getModifiers().contains(Modifier.STATIC))) {
      result.addError(typeDef, "An xml type must be either a top-level class or a nested static class.");
    }

    boolean needsNoArgConstructor = (!(typeDef instanceof EnumTypeDefinition) && (!disabledRules.contains("jaxb.noarg.constructor")));
    if (needsNoArgConstructor && (xmlType != null)) {
      String factoryClassFqn = null;
      try {
        Class factoryClass = xmlType.factoryClass();
        if (factoryClass != javax.xml.bind.annotation.XmlType.DEFAULT.class) {
          factoryClassFqn = factoryClass.getName();
        }
      }
      catch (MirroredTypeException e) {
        TypeMirror typeMirror = e.getTypeMirror();
        if (!(typeMirror instanceof DeclaredType)) {
          result.addError(typeDef, "Unsupported factory class: " + typeMirror);
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
          result.addError(typeDef, "A static, parameterless factory method named " + factoryMethod + " was not found on " + factoryClassFqn);
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
        if ((constructor.getParameters().size() == 0)) {
          hasNoArgConstructor = true;
          break;
        }
      }

      if (!hasNoArgConstructor) {
        result.addError(typeDef, "A TypeDefinition must have a no-arg constructor or be annotated with a factory method.");
      }
    }

    HashMap<QName, Attribute> attributeNames = new HashMap<QName, Attribute>();
    for (Attribute attribute : typeDef.getAttributes()) {
      QName attributeQName = new QName(attribute.getNamespace(), attribute.getName());
      Attribute sameName = attributeNames.put(attributeQName, attribute);
      if (sameName != null) {
        result.addError(attribute, "Attribute has the same name (" + attributeQName + ") as " + sameName.getPosition()
          + ".  Please use annotations to disambiguate.");
        //todo: this check should really be global (including supertypes)....
      }

      result.aggregate(validateAttribute(attribute));
    }

    if (typeDef.getValue() != null) {
      result.aggregate(validateValue(typeDef.getValue()));

      if (!typeDef.getElements().isEmpty()) {
        result.addError(typeDef.getValue(), "A type definition cannot have both an xml value and child element(s).");
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

          //todo: this check should really be global (including supertypes)....
          QName choiceQName = new QName(choice.getNamespace(), choice.getName());
          Element sameName = choiceNames.put(choiceQName, choice);
          if (sameName != null) {
            result.addError(choice, "Element (or element choice) has the same name (" + choiceQName + ") as " + sameName.getPosition()
              + ".  Please use annotations to disambiguate.");
          }
          else if ((wrapperQName == null) && (elementNames.containsKey(choiceQName))) {
            result.addError(choice, "Element (or element choice) has the same name (" + choiceQName + ") as element wrapper for " +
              elementNames.get(choiceQName).values().iterator().next().getPosition() + ".  Please use annotations to disambiguate.");
          }
          else if ((wrapperQName != null) && (elementNames.containsKey(null) && (elementNames.get(null).containsKey(wrapperQName)))) {
            result.addError(element, "Wrapper for element has the same name (" + wrapperQName + ") as " +
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
      result.aggregate(validateXmlID(typeDef.getXmlID()));
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
          result.addError(schema, "A type must be specified at the package-level for @XmlSchemaType.");
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
            result.addError(schema, "A type must be specified at the package-level for all types of @XmlSchemaTypes.");
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

    XmlType baseType = attribute.getBaseType();
    if (baseType == null) {
      result.addError(attribute, "No base type specified.");
    }
    else if ((baseType instanceof XmlClassType) && (((XmlClassType) baseType).getTypeDefinition() instanceof ComplexTypeDefinition)) {
      result.addError(attribute, "An attribute must have a simple base type. " + new QName(baseType.getNamespace(), baseType.getName())
        + " is a complex type.");
    }

    return result;
  }

  public ValidationResult validateElement(Element element) {
    ValidationResult result = validateAccessor(element);

    XmlElements xmlElements = element.getAnnotation(XmlElements.class);
    if ((element.isCollectionType()) && (element.getBaseType() != KnownXmlType.ANY_TYPE) &&
      (xmlElements != null) && (xmlElements.value() != null) && (xmlElements.value().length > 1)) {
      //make sure all @XmlElement classes are an instance of the parameterized type.
      TypeMirror itemType = element.getCollectionItemType();
      if (itemType instanceof DeclaredType && ((DeclaredType) itemType).getDeclaration() != null) {
        String fqn = ((DeclaredType) itemType).getDeclaration().getQualifiedName();
        for (XmlElement xmlElement : xmlElements.value()) {
          DecoratedTypeMirror elementCandidate;
          try {
            Class clazz = xmlElement.type();
            AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
            DeclaredType declaredType = env.getTypeUtils().getDeclaredType(env.getTypeDeclaration(clazz.getName()));
            elementCandidate = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(declaredType);
          }
          catch (MirroredTypeException e) {
            elementCandidate = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(e.getTypeMirror());
          }
          if (!elementCandidate.isInstanceOf(fqn)) {
            result.addError(element, elementCandidate + " is not an instance of " + fqn);
          }
        }
      }
      else {
        result.addWarning(element, "Unknown or invisible collection item type.");
      }
    }

    return result;
  }

  public ValidationResult validateValue(Value value) {
    ValidationResult result = validateAccessor(value);

    XmlType baseType = value.getBaseType();
    if (baseType == null) {
      result.addError(value, "No base type specified.");
    }
    else if ((baseType instanceof XmlClassType) && (((XmlClassType) baseType).getTypeDefinition() instanceof ComplexTypeDefinition)) {
      result.addError(value, "An xml value must have a simple base type. " + new QName(baseType.getNamespace(), baseType.getName())
        + " is a complex type.");
    }

    return result;
  }

  public ValidationResult validateElementRef(ElementRef elementRef) {
    ValidationResult result = validateAccessor(elementRef);

    if ((elementRef.getAnnotation(XmlElement.class) != null) || (elementRef.getAnnotation(XmlElements.class) != null)) {
      result.addError(elementRef, "The xml element ref cannot be annotated also with XmlElement or XmlElements.");
    }

    if (elementRef.isCollectionType() && elementRef.getChoices().isEmpty()) {
      result.addError(elementRef, String.format("Member %s of %s: no known root element subtypes of %s",
                                                                            elementRef.getSimpleName(), elementRef.getTypeDefinition().getQualifiedName(),
                                                                            elementRef.getBareAccessorType()));
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
            result.addError(setter, "'" + annotation + "' is on both the getter and setter.");
          }
        }
      }
    }

    if ((accessor.isXmlIDREF()) && (accessor.getAccessorForXmlID() == null)) {
      if (this.disabledRules.contains("jaxb.xmlidref.references.xmlid")) {
        result.addError(accessor, "An XML IDREF must have a base type that references another type that has an XML ID.");
      }
      else {
        result.addWarning(accessor, "An XML IDREF must have a base type that references another type that has an XML ID.");
      }
    }

    if (accessor.isReferencesQNameEnum()) {
      XmlType baseType = accessor.getBaseType();
      if (baseType == null || (!KnownXmlType.QNAME.getQname().equals(baseType.getQname()) && !KnownXmlType.ANY_URI.getQname().equals(baseType.getQname()) && !KnownXmlType.STRING.getQname().equals(baseType.getQname()))) {
        result.addError(accessor, "An accessor that references a QName enumeration must return QName or URI.");
      }

      TypeMirror enumRef = accessor.getQNameEnumRef();
      if (!(enumRef instanceof EnumType) || ((EnumType) enumRef).getDeclaration() == null || ((DeclaredType) enumRef).getDeclaration().getAnnotation(XmlQNameEnum.class) == null) {
        result.addError(accessor, "A QName enum reference must reference an enum type annotated with @org.codehaus.enunciate.qname.XmlQNameEnum.");
      }
    }

    return result;
  }

  public ValidationResult validateXmlID(Accessor accessor) {
    ValidationResult result = new ValidationResult();

    TypeMirror accessorType = accessor.isAdapted() ? accessor.getAdapterType().getAdaptingType() : accessor.getAccessorType();
    if (!(accessorType instanceof DeclaredType) || !((DeclaredType) accessorType).getDeclaration().getQualifiedName().startsWith(String.class.getName())) {
      result.addError(accessor, "An xml id must be a string.");
    }

    return result;
  }
}
