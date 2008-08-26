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

package org.codehaus.enunciate.modules.rest;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.type.*;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedInterfaceType;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.rest.ContentTypeHandler;
import org.codehaus.enunciate.contract.rest.RESTMethod;
import org.codehaus.enunciate.contract.rest.RESTNoun;
import org.codehaus.enunciate.contract.rest.RESTParameter;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;
import org.codehaus.enunciate.rest.annotations.VerbType;

import javax.activation.DataHandler;
import java.util.*;

/**
 * The default REST validator.
 * 
 * @author Ryan Heaton
 */
public class RESTValidator extends BaseValidator {

  private final boolean requireJAXBCompatibility;
  private final Map<String, String> configuredContentHandlers;

  public RESTValidator(Map<String, String> configuredContentHandlers, boolean requireJAXBCompatibility) {
    this.configuredContentHandlers = configuredContentHandlers;
    this.requireJAXBCompatibility = requireJAXBCompatibility;
  }

  @Override
  public ValidationResult validateRESTAPI(Map<RESTNoun, List<RESTMethod>> restAPI) {
    ValidationResult result = super.validateRESTAPI(restAPI);

    for (RESTNoun noun : restAPI.keySet()) {
      Map<String, Set<VerbType>> contentTypes2Verbs = new TreeMap<String, Set<VerbType>>();
      List<RESTMethod> methods = restAPI.get(noun);
      for (RESTMethod method : methods) {
        Collection<VerbType> verbList = Arrays.asList(method.getVerbs());
        for (String contentType : method.getContentTypes()) {
          Set<VerbType> verbs = contentTypes2Verbs.get(contentType);
          if (verbs == null) {
            verbs = EnumSet.noneOf(VerbType.class);
            contentTypes2Verbs.put(contentType, verbs);
          }

          for (VerbType verb : verbList) {
            if (!verbs.add(verb)) {
              result.addError(method, "Duplicate verb '" + verb + "' for content type '" + contentType + "' of REST noun '" + noun + "'.");
            }
          }
        }

        RESTParameter properNoun = method.getProperNoun();
        if (properNoun != null) {
          if (properNoun.isCollectionType()) {
            result.addError(properNoun, "A proper noun is not allowed to be a collection or an array.");
          }

          if ((properNoun.isOptional()) && (properNoun.getType() instanceof PrimitiveType)) {
            result.addError(properNoun, "An optional proper noun parameter cannot be a primitive type.");
          }
        }

        HashSet<String> adjectives = new HashSet<String>();
        for (RESTParameter adjective : method.getAdjectives()) {
          if (!adjectives.add(adjective.getAdjectiveName())) {
            result.addError(adjective, "Duplicate adjective name '" + adjective.getAdjectiveName() + "'.");
          }

          if (!adjective.isComplexAdjective()) {
            if ((adjective.isOptional()) && (adjective.getType() instanceof PrimitiveType)) {
              result.addError(adjective, "An optional adjective parameter cannot be a primitive type.");
            }

            if (adjective.getAdjectiveName().equals(method.getJSONPParameter())) {
              result.addError(adjective, "Invalid adjective name '" + adjective.getAdjectiveName() + "': conflicts with the JSONP parameter name.");
            }
          }
        }

        HashSet<String> contextParameters = new HashSet<String>();
        for (RESTParameter contextParam : method.getContextParameters()) {
          if (!contextParameters.add(contextParam.getContextParameterName())) {
            result.addError(contextParam, "Duplicate context parameter name '" + contextParam.getContextParameterName() + "'.");
          }

          boolean parameterFound = false;
          for (String contextParameter : noun.getContextParameters()) {
            if (contextParam.getContextParameterName().equals(contextParameter)) {
              parameterFound = true;
              break;
            }
          }

          if (!parameterFound) {
            result.addError(contextParam, "No context parameter named '" + contextParam.getContextParameterName() + "' is found in the context (" + noun.toString() + ")");
          }
        }

        if (!method.getContentTypeParameters().isEmpty()) {
          if (method.getContentTypeParameters().size() > 1) {
            result.addError(method, "Multiple content type parameters.");
          }
          else {
            RESTParameter restParameter = method.getContentTypeParameters().iterator().next();
            if (!((DecoratedTypeMirror) restParameter.getType()).isInstanceOf(String.class.getName())) {
              result.addError(restParameter, "Content type parameter must be a String.");
            }
          }
        }

        RESTParameter nounValue = method.getNounValue();
        if (nounValue != null) {
          if ((verbList.contains(VerbType.read)) || (verbList.contains(VerbType.delete))) {
            result.addError(method, "The verbs 'read' and 'delete' do not support a noun value.");
          }

          DecoratedTypeMirror decoratedNounValueType = (DecoratedTypeMirror) nounValue.getType();
          if (!isDataHandler(decoratedNounValueType) && !isDataHandlers(decoratedNounValueType) && !decoratedNounValueType.isClass()) {
            if (requireJAXBCompatibility) {
              result.addError(nounValue, "Enunciate doesn't support unmarshalling objects of type " + decoratedNounValueType +
                " because it is not a JAXB-compatible type. If JAXB compatibility isn't required because you have provided your own" +
                " content type handlers, use the Enunciate configuration to disable this error.");
            }
            else {
              result.addWarning(nounValue, "Enunciate doesn't support unmarshalling objects of type " + decoratedNounValueType +
                ". Unless a custom content type handler is provided, this operation will fail.");
            }
          }

          if ((nounValue.isOptional()) && (nounValue.getType() instanceof PrimitiveType)) {
            result.addError(nounValue, "An optional noun value parameter cannot be a primitive type.");
          }
        }

        DecoratedTypeMirror returnType = ((DecoratedTypeMirror) method.getReturnType());
        if (!returnType.isVoid() && !returnType.isInstanceOf(DataHandler.class.getName()) && !returnType.isClass()) {
          if (requireJAXBCompatibility) {
            result.addError(method, "Enunciate doesn't support marshalling objects of type " + returnType + 
              " because it is not a JAXB-compatible type. If JAXB compatibility isn't required because you have provided your own" +
              " content type handlers, use the Enunciate configuration to disable this error.");
          }
          else {
            result.addWarning(method, "Enunciate doesn't support marshalling objects of type " + returnType + ". Unless a custom content type handler is provided, this operation will fail.");
          }
        }
      }
    }

    return result;
  }

  private boolean isDataHandler(DecoratedTypeMirror type) {
    return type.isDeclared()
      && ((DeclaredType) type).getDeclaration() != null
      && "javax.activation.DataHandler".equals(((DeclaredType) type).getDeclaration().getQualifiedName());
  }

  private boolean isDataHandlers(DecoratedTypeMirror type) {
    if (type.isCollection()) {
      Collection<TypeMirror> typeArgs = ((DeclaredType) type).getActualTypeArguments();
      if ((typeArgs != null) && (typeArgs.size() == 1)) {
        return isDataHandler((DecoratedTypeMirror) typeArgs.iterator().next());
      }
    }
    else if (type.isArray()) {
      return isDataHandler((DecoratedTypeMirror) ((ArrayType) type).getComponentType());
    }
    return false;
  }

  @Override
  public ValidationResult validateContentTypeHandlers(List<ContentTypeHandler> contentTypeHandlers) {
    ValidationResult result = super.validateContentTypeHandlers(contentTypeHandlers);

    Map<String, ContentTypeHandler> knownContentTypeHandlers = new TreeMap<String, ContentTypeHandler>();
    for (ContentTypeHandler handler : contentTypeHandlers) {
      if (!isRESTRequestDataFormatHandler(handler)) {
        result.addError(handler, "A content type handler must implement " + RESTRequestContentTypeHandler.class.getName() + ".");
      }

      //check for a zero-arg constructor...
      boolean hasNoArgConstructor = false;
      Collection<ConstructorDeclaration> constructors = handler.getConstructors();
      for (ConstructorDeclaration constructor : constructors) {
        if ((constructor.getModifiers().contains(Modifier.PUBLIC)) && (constructor.getParameters().size() == 0)) {
          hasNoArgConstructor = true;
          break;
        }
      }

      if (!hasNoArgConstructor) {
        result.addError(handler, "A content type handler must have a public no-arg constructor.");
      }

      for (String contentType : handler.getSupportedContentTypes()) {
        ContentTypeHandler otherHandler = knownContentTypeHandlers.put(contentType, handler);
        if ((otherHandler != null) && (this.configuredContentHandlers.get(contentType) != null)) {
          result.addError(handler, "Handler conflicts for content type '" + contentType + "' with " + otherHandler.getQualifiedName() + ".  Please use configuration to specify which handler to use.");
        }
      }
    }

    return result;
  }

  /**
   * Whether the given class declaration is a valid data format handler for this module.
   *
   * @param declaration The declaration.
   * @return Whether the given class declaration is a valid data format handler for this module.
   */
  protected boolean isRESTRequestDataFormatHandler(ClassDeclaration declaration) {
    if (Object.class.getName().equals(declaration.getQualifiedName())) {
      return false;
    }

    Collection<InterfaceType> interfaceTypes = declaration.getSuperinterfaces();
    for (InterfaceType interfaceType : interfaceTypes) {
      DecoratedInterfaceType decorated = (DecoratedInterfaceType) TypeMirrorDecorator.decorate(interfaceType);
      if (decorated.isInstanceOf(RESTRequestContentTypeHandler.class.getName())) {
        return true;
      }
    }

    return declaration.getSuperclass() != null && isRESTRequestDataFormatHandler(declaration.getSuperclass().getDeclaration());
  }
}
