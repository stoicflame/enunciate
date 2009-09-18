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

package org.codehaus.enunciate.modules.amf;

import com.sun.mirror.declaration.*;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedDeclaredType;
import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.jaxb.adapters.Adaptable;
import org.codehaus.enunciate.contract.jaxws.EndpointImplementation;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.contract.jaxws.WebParam;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.contract.validation.ConfigurableRules;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * The validator for the xfire-client module.
 *
 * @author Ryan Heaton
 */
public class AMFValidator extends BaseValidator implements ConfigurableRules {

  private final Set<String> unsupportedTypes = new HashSet<String>();
  private Set<String> disabledRules = new TreeSet<String>();

  public AMFValidator() {
    unsupportedTypes.add(QName.class.getName());
    unsupportedTypes.add(XMLGregorianCalendar.class.getName());
    unsupportedTypes.add(javax.xml.datatype.Duration.class.getName());
    unsupportedTypes.add(java.awt.Image.class.getName());
    unsupportedTypes.add(javax.xml.transform.Source.class.getName());
  }

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);

    if (!isAMFTransient(ei)) {
      for (WebMethod webMethod : ei.getWebMethods()) {
        if (!isAMFTransient(webMethod)) {
          if (!isSupported(webMethod.getWebResult())) {
            result.addError(webMethod, "AMF doesn't support '" + webMethod.getWebResult() + "' as a return type.");
          }
          for (WebParam webParam : webMethod.getWebParameters()) {
            if (!isSupported(webParam.getType())) {
              result.addError(webParam, "AMF doesn't support '" + webParam.getType() + "' as a parameter type.");
            }
          }
        }
      }

      if (ei.getEndpointImplementations().size() > 1) {
        ArrayList<String> impls = new ArrayList<String>();
        for (EndpointImplementation impl : ei.getEndpointImplementations()) {
          impls.add(impl.getQualifiedName());
        }
        result.addError(ei, "Sorry, AMF doesn't support two endpoint implementations for interface '" + ei.getQualifiedName() +
          "'.  Found " + ei.getEndpointImplementations().size() + " implementations (" + impls.toString() + ").");
      }
      else if (ei.getEndpointImplementations().isEmpty()) {
        result.addError(ei, "AMF requires an implementation for each service interface.");
      }
    }
    
    return result;
  }

  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = super.validateComplexType(complexType);
    if (!isAMFTransient(complexType)) {
      if (!hasDefaultConstructor(complexType)) {
        result.addError(complexType, "The mapping from AMF to JAXB requires a public no-arg constructor.");
      }

      if (!disabledRules.contains("as3.conflicting.names")) {
        if ("Date".equals(complexType.getClientSimpleName())) {
          result.addError(complexType, "ActionScript can't handle a class named 'Date'.  It conflicts with the top-level ActionScript class of the same name. Either rename the class, or use the @org.codehaus.enunciate.ClientName annotation to rename the class on the client-side.");
        }
        else if ("Event".equals(complexType.getClientSimpleName())) {
          result.addError(complexType, "The Enunciate-generated ActionScript code can't handle a class named 'Event'.  It conflicts with the ActionScript remoting class of the same name. Either rename the class, or use the @org.codehaus.enunciate.ClientName annotation to rename the class on the client-side.");
        }
      }

      for (Attribute attribute : complexType.getAttributes()) {
        if (!isAMFTransient(attribute)) {
          if (attribute.getDelegate() instanceof FieldDeclaration) {
            result.addError(attribute, "If you're mapping to AMF, you can't use fields for your accessors. ");
          }

          if (!isSupported(attribute.getAccessorType())) {
            result.addError(attribute, "AMF doesn't support the '" + attribute.getAccessorType() + "' type.");
          }
        }
      }

      for (Element element : complexType.getElements()) {
        if (!isAMFTransient(element)) {
          if (element.getDelegate() instanceof FieldDeclaration) {
            result.addError(element, "If you're mapping to AMF, you can't use fields for your accessors. ");
          }

          if (!isSupported(element.getAccessorType())) {
            result.addError(element, "AMF doesn't support the '" + element.getAccessorType() + "' type.");
          }
        }
      }

      Value value = complexType.getValue();
      if (value != null) {
        if (!isAMFTransient(value)) {
          if (value.getDelegate() instanceof FieldDeclaration) {
            result.addError(value, "If you're mapping to AMF, you can't use fields for your accessors. ");
          }

          if (!isSupported(value.getAccessorType())) {
            result.addError(value, "AMF doesn't support the '" + value.getAccessorType() + "' type.");
          }
        }
      }
    }

    return result;
  }


  @Override
  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    ValidationResult result = super.validateSimpleType(simpleType);
    if (!isAMFTransient(simpleType)) {
      if (!hasDefaultConstructor(simpleType)) {
        result.addError(simpleType, "The mapping from AMF to JAXB requires a public no-arg constructor.");
      }

      if (!disabledRules.contains("as3.conflicting.names")) {
        if ("Date".equals(simpleType.getClientSimpleName())) {
          result.addError(simpleType, "ActionScript can't handle a class named 'Date'.  It conflicts with the top-level ActionScript class of the same name. Either rename the class, or use the @org.codehaus.enunciate.ClientName annotation to rename the class on the client-side.");
        }
        else if ("Event".equals(simpleType.getClientSimpleName())) {
          result.addError(simpleType, "The Enunciate-generated ActionScript code can't handle a class named 'Event'.  It conflicts with the ActionScript remoting class of the same name. Either rename the class, or use the @org.codehaus.enunciate.ClientName annotation to rename the class on the client-side.");
        }
      }
    }
    return result;
  }

  @Override
  public ValidationResult validateEnumType(EnumTypeDefinition enumType) {
    ValidationResult result = super.validateEnumType(enumType);
    if (!isAMFTransient(enumType)) {
      if (!disabledRules.contains("as3.conflicting.names")) {
        if ("Date".equals(enumType.getClientSimpleName())) {
          result.addError(enumType, "ActionScript can't handle a class named 'Date'.  It conflicts with the top-level ActionScript class of the same name. Either rename the class, or use the @org.codehaus.enunciate.ClientName annotation to rename the class on the client-side.");
        }
        else if ("Event".equals(enumType.getClientSimpleName())) {
          result.addError(enumType, "The Enunciate-generated ActionScript code can't handle a class named 'Event'.  It conflicts with the ActionScript remoting class of the same name. Either rename the class, or use the @org.codehaus.enunciate.ClientName annotation to rename the class on the client-side.");
        }
      }
    }
    return result;
  }

  private boolean hasDefaultConstructor(TypeDefinition typeDefinition) {
    Collection<ConstructorDeclaration> constructors = typeDefinition.getConstructors();
    for (ConstructorDeclaration constructor : constructors) {
      if ((constructor.getModifiers().contains(Modifier.PUBLIC)) && (constructor.getParameters().isEmpty())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether the given type is supported.
   *
   * @param type The type to test for supportability.
   * @return Whether the given type is supported.
   */
  protected boolean isSupported(TypeMirror type) {
    if ((type instanceof Adaptable) && ((Adaptable) type).isAdapted()) {
      return isSupported(((Adaptable) type).getAdapterType().getAdaptingType());
    }
    else if (type instanceof DeclaredType) {
      DecoratedDeclaredType declaredType = (DecoratedDeclaredType) TypeMirrorDecorator.decorate(type);
      if ((declaredType.getDeclaration() != null) && (isAMFTransient(declaredType.getDeclaration()))) {
        return false;
      }
      else if ((declaredType.isInstanceOf(Collection.class.getName())) || (declaredType.isInstanceOf(java.util.Map.class.getName()))) {
        boolean supported = true;
        for (TypeMirror typeArgument : declaredType.getActualTypeArguments()) {
          supported &= isSupported(typeArgument);
        }
        return supported;
      }
      else {
        return declaredType.getDeclaration() != null && !unsupportedTypes.contains(declaredType.getDeclaration().getQualifiedName());
      }
    }

    //by default, we're going to assume that the type is complex and is supported.
    return true;
  }

  /**
   * Whether the given type declaration is AMF-transient.
   *
   * @param declaration The type declaration.
   * @return Whether the given tyep declaration is AMF-transient.
   */
  protected boolean isAMFTransient(TypeDeclaration declaration) {
    return isAMFTransient((Declaration) declaration) || isAMFTransient(declaration.getPackage());
  }

  /**
   * Whether the given type declaration is AMF-transient.
   *
   * @param declaration The type declaration.
   * @return Whether the given tyep declaration is AMF-transient.
   */
  protected boolean isAMFTransient(Declaration declaration) {
    return declaration != null && declaration.getAnnotation(AMFTransient.class) != null;
  }

  /**
   * Disables the specified rules.
   * 
   * @param ruleIds The ids of the rules to disable.
   */
  public void disableRules(Set<String> ruleIds) {
    this.disabledRules.addAll(ruleIds);
  }
}
