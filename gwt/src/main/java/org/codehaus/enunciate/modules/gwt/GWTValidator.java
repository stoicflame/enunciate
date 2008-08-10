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

package org.codehaus.enunciate.modules.gwt;

import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.declaration.*;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedDeclaredType;
import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.jaxb.adapters.Adaptable;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.util.TypeDeclarationComparator;

import java.util.*;

/**
 * The validator for the xfire-client module.
 *
 * @author Ryan Heaton
 */
public class GWTValidator extends BaseValidator {

  private final boolean enforceNoFieldAccessors;
  private final boolean enforceNamespaceConformance;
  private final String gwtModuleNamespace;
  private final Set<String> unsupportedTypes = new HashSet<String>();

  public GWTValidator(String gwtModuleNamespace, boolean enforceNamespaceConformance, boolean enforceNoFieldAccessors) {
    this.gwtModuleNamespace = gwtModuleNamespace;
    unsupportedTypes.add(javax.xml.datatype.Duration.class.getName());
    unsupportedTypes.add(java.awt.Image.class.getName());
    unsupportedTypes.add(javax.xml.transform.Source.class.getName());
    this.enforceNamespaceConformance = enforceNamespaceConformance;
    this.enforceNoFieldAccessors = enforceNoFieldAccessors;
  }

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);

    TreeSet<WebFault> allFaults = new TreeSet<WebFault>(new TypeDeclarationComparator());
    if (!isGWTTransient(ei)) {
      if ((this.enforceNamespaceConformance) && (!ei.getPackage().getQualifiedName().startsWith(this.gwtModuleNamespace))) {
        result.addError(ei, String.format("The package of the endpoint interface, %s, must start with the GWT module namespace, %s.", ei.getPackage().getQualifiedName(), gwtModuleNamespace));
      }

      for (WebMethod webMethod : ei.getWebMethods()) {
        if (!isGWTTransient(webMethod)) {
          if (!isSupported(webMethod.getWebResult())) {
            result.addError(webMethod, "GWT doesn't support '" + webMethod.getWebResult() + "' as a return type.");
          }
          for (WebParam webParam : webMethod.getWebParameters()) {
            if (!isSupported(webParam.getType())) {
              result.addError(webParam, "GWT doesn't support '" + webParam.getType() + "' as a parameter type.");
            }
          }

          allFaults.addAll(webMethod.getWebFaults());
        }
      }

      if (ei.getEndpointImplementations().size() > 1) {
        ArrayList<String> impls = new ArrayList<String>();
        for (EndpointImplementation impl : ei.getEndpointImplementations()) {
          impls.add(impl.getQualifiedName());
        }
        result.addError(ei, "Sorry, GWT doesn't support two endpoint implementations for interface '" + ei.getQualifiedName() +
          "'.  Found " + ei.getEndpointImplementations().size() + " implementations (" + impls.toString() + ").");
      }
      else if (ei.getEndpointImplementations().isEmpty()) {
        result.addError(ei, "GWT requires an implementation for each service interface.");
      }
    }
    
    for (WebFault fault : allFaults) {
      if (!isGWTTransient(fault)) {
        if ((this.enforceNamespaceConformance) && (!fault.getPackage().getQualifiedName().startsWith(this.gwtModuleNamespace))) {
          result.addError(fault, String.format("The package of the fault, %s, must start with the GWT module namespace, %s.", fault.getPackage().getQualifiedName(), gwtModuleNamespace));
        }
      }
    }

    return result;
  }

  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = super.validateComplexType(complexType);
    if (!isGWTTransient(complexType)) {
      if (!hasDefaultConstructor(complexType)) {
        result.addError(complexType, "The mapping from GWT to JAXB requires a public no-arg constructor.");
      }

      if ((this.enforceNamespaceConformance) && (!complexType.getPackage().getQualifiedName().startsWith(this.gwtModuleNamespace))) {
        result.addError(complexType, String.format("The package of the complex type, %s, must start with the GWT module namespace, %s.", complexType.getPackage().getQualifiedName(), gwtModuleNamespace));
      }

      for (Attribute attribute : complexType.getAttributes()) {
        if (!isGWTTransient(attribute)) {
          if ((attribute.getDelegate() instanceof FieldDeclaration) && (enforceNoFieldAccessors)) {
            result.addError(attribute, "If you're mapping to GWT, you can't use fields for your accessors. ");
          }

          if (!isSupported(attribute.getAccessorType())) {
            result.addError(attribute, "GWT doesn't support the '" + attribute.getAccessorType() + "' type.");
          }
        }
      }

      for (Element element : complexType.getElements()) {
        if (!isGWTTransient(element)) {
          if ((element.getDelegate() instanceof FieldDeclaration) && (enforceNoFieldAccessors)) {
            result.addError(element, "If you're mapping to GWT, you can't use fields for your accessors. ");
          }

          if (!isSupported(element.getAccessorType())) {
            result.addError(element, "GWT doesn't support the '" + element.getAccessorType() + "' type.");
          }
        }
      }

      Value value = complexType.getValue();
      if (value != null) {
        if (!isGWTTransient(value)) {
          if ((value.getDelegate() instanceof FieldDeclaration) && (enforceNoFieldAccessors)) {
            result.addError(value, "If you're mapping to GWT, you can't use fields for your accessors. ");
          }

          if (!isSupported(value.getAccessorType())) {
            result.addError(value, "GWT doesn't support the '" + value.getAccessorType() + "' type.");
          }
        }
      }
    }

    return result;
  }


  @Override
  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    ValidationResult result = super.validateSimpleType(simpleType);
    if (!isGWTTransient(simpleType)) {
      if ((this.enforceNamespaceConformance) && (!simpleType.getPackage().getQualifiedName().startsWith(this.gwtModuleNamespace))) {
        result.addError(simpleType, String.format("The package of the simple type, %s, must start with the GWT module namespace, %s.", simpleType.getPackage().getQualifiedName(), gwtModuleNamespace));
      }

      if (!hasDefaultConstructor(simpleType)) {
        result.addError(simpleType, "The mapping from GWT to JAXB requires a public no-arg constructor.");
      }
    }
    return result;
  }


  @Override
  public ValidationResult validateEnumType(EnumTypeDefinition enumType) {
    ValidationResult result = super.validateEnumType(enumType);
    if (!isGWTTransient(enumType)) {
      if ((this.enforceNamespaceConformance) && (!enumType.getPackage().getQualifiedName().startsWith(this.gwtModuleNamespace))) {
        result.addError(enumType, String.format("The package of the enum type, %s, must start with the GWT module namespace, %s.", enumType.getPackage().getQualifiedName(), gwtModuleNamespace));
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
      if ((declaredType.getDeclaration() != null) && (isGWTTransient(declaredType.getDeclaration()))) {
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
        return !unsupportedTypes.contains(declaredType.getDeclaration().getQualifiedName());
      }
    }

    //by default, we're going to assume that the type is complex and is supported.
    return true;
  }

  /**
   * Whether the given type declaration is GWT-transient.
   *
   * @param declaration The type declaration.
   * @return Whether the given tyep declaration is GWT-transient.
   */
  protected boolean isGWTTransient(TypeDeclaration declaration) {
    return isGWTTransient((Declaration) declaration) || isGWTTransient(declaration.getPackage());
  }

  /**
   * Whether the given type declaration is GWT-transient.
   *
   * @param declaration The type declaration.
   * @return Whether the given tyep declaration is GWT-transient.
   */
  protected boolean isGWTTransient(Declaration declaration) {
    return declaration != null && declaration.getAnnotation(GWTTransient.class) != null;
  }

}
