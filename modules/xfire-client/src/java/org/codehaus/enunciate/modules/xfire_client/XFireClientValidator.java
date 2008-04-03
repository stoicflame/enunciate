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

package org.codehaus.enunciate.modules.xfire_client;

import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import org.codehaus.enunciate.contract.jaxb.Accessor;
import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.SimpleTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.contract.jaxws.WebParam;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * The validator for the xfire-client module.
 *
 * @author Ryan Heaton
 */
public class XFireClientValidator extends BaseValidator {


  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = new ValidationResult();

    HashSet<String> uniqueMethodNames = new HashSet<String>();
    for (WebMethod webMethod : ei.getWebMethods()) {
      if (!uniqueMethodNames.add(webMethod.getSimpleName())) {
        result.addError(webMethod.getPosition(), "Sorry, the xfire client module doesn't support overloaded methods yet.  Unfortunately, each method has " +
          "to have a unique name.");
      }

      for (WebParam webParam : webMethod.getWebParameters()) {
        if (webParam.isOutput()) {
          //todo: add support for in in/out parameters.
          result.addError(webParam.getPosition(), "The xfire client module doesn't support IN/OUT or OUT parameters yet....");
        }
      }
    }

    return result;
  }

  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = super.validateComplexType(complexType);
    validateTypeDefinition(complexType, result);
    return result;
  }

  @Override
  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    ValidationResult result = super.validateSimpleType(simpleType);
    validateTypeDefinition(simpleType, result);
    return result;
  }

  public void validateTypeDefinition(TypeDefinition typeDef, ValidationResult result) {
    ArrayList<Accessor> accessors = new ArrayList<Accessor>();
    accessors.addAll(typeDef.getAttributes());
    accessors.add(typeDef.getValue());
    accessors.addAll(typeDef.getElements());
    for (Accessor accessor : accessors) {
      if (accessor != null) {
        if ((accessor.getAnnotation(XmlIDREF.class) != null) && (accessor.getAnnotation(XmlList.class) != null)) {
          result.addError(accessor.getPosition(), "The xfire client code currently doesn't support @XmlList and @XmlIDREF annotations together.");
        }

        TypeMirror accessorType = accessor.getBareAccessorType();
        if (accessorType instanceof DeclaredType) {
          String accessorTypeFQN = ((DeclaredType) accessorType).getDeclaration().getQualifiedName();
          if (java.awt.Image.class.getName().equals(accessorTypeFQN)) {
            result.addError(accessor.getPosition(), "xfire-client module doesn't yet support handling java.awt.Image.");
          }
          else if (javax.xml.transform.Source.class.getName().equals(accessorTypeFQN)) {
            result.addError(accessor.getPosition(), "xfire-client module doesn't yet support handling javax.xml.transform.Source.");
          }
        }
      }
    }
  }

}
