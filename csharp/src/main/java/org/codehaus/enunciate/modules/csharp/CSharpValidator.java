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

import org.codehaus.enunciate.contract.jaxb.ComplexTypeDefinition;
import org.codehaus.enunciate.contract.jaxb.Element;
import org.codehaus.enunciate.contract.jaxws.EndpointInterface;
import org.codehaus.enunciate.contract.jaxws.WebMethod;
import org.codehaus.enunciate.contract.jaxws.WebParam;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.util.MapType;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.declaration.Declaration;

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
    }

    return result;
  }

  @Override
  public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
    ValidationResult result = super.validateComplexType(complexType);
    for (Element element : complexType.getElements()) {
      if (element.getAccessorType() instanceof MapType && !element.isAdapted()) {
        result.addError(element, "C# doesn't have a built-in way of serializing a Map. So you're going to have to use @XmlJavaTypeAdapter to supply " +
          "your own adapter for the Map, or disable the C# module.");
      }
    }
    return result;
  }

}
