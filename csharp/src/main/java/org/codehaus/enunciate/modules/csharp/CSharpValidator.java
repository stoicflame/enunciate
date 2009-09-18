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

package org.codehaus.enunciate.modules.csharp;

import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.contract.jaxws.WebParam;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.util.MapType;
import org.codehaus.enunciate.ClientName;

import java.util.HashMap;
import java.util.Map;

/**
 * Validator for the C# module.
 *
 * @author Ryan Heaton
 */
public class CSharpValidator extends BaseValidator {

  @Override
  public ValidationResult validateEndpointInterface(EndpointInterface ei) {
    ValidationResult result = super.validateEndpointInterface(ei);
    Map<String, Declaration> paramsByName = new HashMap<String, Declaration>();
    for (WebMethod webMethod : ei.getWebMethods()) {
      for (WebParam webParam : webMethod.getWebParameters()) {
        //no out or in/out non-header parameters!
        if (webParam.isHeader()) {
          //unique parameter names for all header parameters of a given ei
          Declaration conflict = paramsByName.put(webParam.getElementName(), webParam);
          if (conflict != null) {
            result.addError(webParam, "C# requires that all header parameters defined in the same endpoint interface have unique names. " +
              "This parameter conflicts with the one at " + (conflict.getPosition() == null ? "(unknown source position)" : conflict.getPosition()));
          }

          DecoratedTypeMirror paramType = (DecoratedTypeMirror) webParam.getType();
          if (paramType.isCollection()) {
            result.addError(webParam, "C# can't handle header parameters that are collections.");
          }

        }
        else if (webParam.getMode() != javax.jws.WebParam.Mode.IN) {
          result.addError(webParam, "C# doesn't support non-header parameters of mode " + webParam.getMode());
        }

        //parameters/results can't be maps
        if (webParam.getType() instanceof MapType) {
          result.addError(webParam, "C# can't handle types that are maps.");
        }
      }

      //web result cannot be a header.
      if (webMethod.getWebResult().isHeader()) {
        Declaration conflict = paramsByName.put(webMethod.getWebResult().getElementName(), webMethod);
        if (conflict != null) {
          result.addError(webMethod, "C# requires that all header parameters defined in the same endpoint interface have unique names. " +
            "This parameter conflicts with the one at " + (conflict.getPosition() == null ? "(unknown source position)" : conflict.getPosition()));
        }
      }

      if (webMethod.getWebResult().getType() instanceof MapType) {
        result.addError(webMethod, "C# can't handle types that are maps.");
      }

      if (capitalize(webMethod.getClientSimpleName()).equals(ei.getClientSimpleName())) {
        result.addError(webMethod, "C# can't handle methods that are of the same name as their containing class. Either rename the method, or use the @org.codehaus.enunciate.ClientName annotation to rename the method (or type) on the client-side.");
      }
    }

    return result;
  }

  /**
   * Capitalizes a string.
   *
   * @param string The string to capitalize.
   * @return The capitalized value.
   */
  public static String capitalize(String string) {
    return Character.toString(string.charAt(0)).toUpperCase() + string.substring(1);
  }

  @Override
  public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
    ValidationResult result = super.validateSimpleType(simpleType);
    if (simpleType.getValue() != null) {
      if (capitalize(simpleType.getValue().getClientSimpleName()).equals(simpleType.getClientSimpleName())) {
        result.addError(simpleType.getValue(), "C# can't handle properties/fields that are of the same name as their containing class. Either rename the property/field, or use the @org.codehaus.enunciate.ClientName annotation to rename the property/field on the client-side.");
      }
    }
    return result;
  }

  @Override
  public ValidationResult validateEnumType(EnumTypeDefinition enumType) {
    ValidationResult result = super.validateEnumType(enumType);
    for (EnumConstantDeclaration enumItem : ((EnumDeclaration) enumType.getDelegate()).getEnumConstants()) {
      String simpleName = enumItem.getSimpleName();
      ClientName clientNameInfo = enumItem.getAnnotation(ClientName.class);
      if (clientNameInfo != null) {
        simpleName = clientNameInfo.value();
      }

      if ("event".equals(simpleName)) {
        result.addError(enumItem, "C# can't handle an enum constant named 'Event'. Either rename the enum constant, or use the @org.codehaus.enunciate.ClientName annotation to rename it on the client-side.");
      }
      else if (simpleName.equals(enumType.getClientSimpleName())) {
        result.addError(enumItem, "C# can't handle properties/fields that are of the same name as their containing class. Either rename the property/field, or use the @org.codehaus.enunciate.ClientName annotation to rename the property/field on the client-side.");
      }
    }
    return result;
  }

  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = super.validateComplexType(complexType);
    for (Attribute attribute : complexType.getAttributes()) {
      if (capitalize(attribute.getClientSimpleName()).equals(complexType.getClientSimpleName())) {
        result.addError(attribute, "C# can't handle properties/fields that are of the same name as their containing class.");
      }
    }

    if (complexType.getValue() != null) {
      if (capitalize(complexType.getValue().getClientSimpleName()).equals(complexType.getClientSimpleName())) {
        result.addError(complexType.getValue(), "C# can't handle properties/fields that are of the same name as their containing class. Either rename the property/field, or use the @org.codehaus.enunciate.ClientName annotation to rename the property/field on the client-side.");
      }
    }

    for (Element element : complexType.getElements()) {
      if (element.getAccessorType() instanceof MapType && !element.isAdapted()) {
        result.addError(element, "C# doesn't have a built-in way of serializing a Map. So you're going to have to use @XmlJavaTypeAdapter to supply " +
          "your own adapter for the Map, or disable the C# module.");
      }

      if (capitalize(element.getClientSimpleName()).equals(complexType.getClientSimpleName())) {
        result.addError(element, "C# can't handle properties/fields that are of the same name as their containing class. Either rename the property/field, or use the @org.codehaus.enunciate.ClientName annotation to rename the property/field on the client-side.");
      }
    }


    return result;
  }

}
