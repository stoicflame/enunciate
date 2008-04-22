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
import com.sun.mirror.type.InterfaceType;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedInterfaceType;
import org.codehaus.enunciate.contract.rest.ContentTypeHandler;
import org.codehaus.enunciate.contract.rest.RESTMethod;
import org.codehaus.enunciate.contract.rest.RESTNoun;
import org.codehaus.enunciate.contract.validation.BaseValidator;
import org.codehaus.enunciate.contract.validation.ValidationResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The default REST validator.
 * 
 * @author Ryan Heaton
 */
public class RESTValidator extends BaseValidator {

  private final Map<String, String> configuredContentHandlers;

  public RESTValidator(Map<String, String> configuredContentHandlers) {
    this.configuredContentHandlers = configuredContentHandlers;
  }

  @Override
  public ValidationResult validateRESTAPI(Map<RESTNoun, List<RESTMethod>> restAPI) {
    ValidationResult result = super.validateRESTAPI(restAPI);

    // Can't think of any additional rules beyond what's
    // already in the default deployment module....

    return result;
  }

  @Override
  public ValidationResult validateContentTypeHandlers(List<ContentTypeHandler> contentTypeHandlers) {
    ValidationResult result = super.validateContentTypeHandlers(contentTypeHandlers);

    Map<String, ContentTypeHandler> knownContentTypeHandlers = new TreeMap<String, ContentTypeHandler>();
    for (ContentTypeHandler handler : contentTypeHandlers) {
      if (!isRESTRequestDataFormatHandler(handler)) {
        result.addError(handler.getPosition(), "A content type handler must implement " + RESTRequestContentTypeHandler.class.getName() + ".");
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
        result.addError(handler.getPosition(), "A content type handler must have a public no-arg constructor.");
      }

      for (String contentType : handler.getSupportedContentTypes()) {
        ContentTypeHandler otherHandler = knownContentTypeHandlers.put(contentType, handler);
        if ((otherHandler != null) && (this.configuredContentHandlers.get(contentType) != null)) {
          result.addError(handler.getPosition(), "Handler conflicts for content type '" + contentType + "' with " + otherHandler.getQualifiedName() + ".  Please use configuration to specify which handler to use.");
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
