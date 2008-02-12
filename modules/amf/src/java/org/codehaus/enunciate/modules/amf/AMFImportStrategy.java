/*
 * Copyright 2006 Web Cohesion
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

import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import net.sf.jelly.apt.decorations.declaration.DecoratedMemberDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.jaxb.Attribute;
import org.codehaus.enunciate.contract.jaxb.Element;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxb.Value;
import org.codehaus.enunciate.contract.jaxws.*;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import org.codehaus.enunciate.util.MapType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;

/**
 * Strategy for looping through the (unique) AMF imports for a given declaration.  If a {@link org.codehaus.enunciate.contract.jaxws.WebMethod},
 * then the imports are those for the associated result event.  If a {@link org.codehaus.enunciate.contract.jaxws.EndpointInterface} then the
 * unique imports include the result events and parameters. If a {@link org.codehaus.enunciate.contract.jaxb.TypeDefinition}, then the unique
 * imports include each of the type's properties.
 *
 * @author Ryan Heaton
 */
public class AMFImportStrategy extends EnunciateTemplateLoopStrategy<String> {

  private String var = "amfImport";
  private DecoratedMemberDeclaration declaration;
  private final ClientClassnameForMethod classnameFor;
  private boolean includeComponentTypes = true;

  public AMFImportStrategy(ClientClassnameForMethod classnameFor) {
    this.classnameFor = classnameFor;
  }

  protected Iterator<String> getLoop(TemplateModel model) throws TemplateException {
    final Set<String> imports = new HashSet<String>();
    try {
      if (declaration instanceof EndpointInterface) {
        for (WebMethod webMethod : ((EndpointInterface) declaration).getWebMethods()) {
          for (WebParam webParam : webMethod.getWebParameters()) {
            imports.add(classnameFor.convert(webParam));
            addComponentTypes(webParam.getType(), imports);
          }

          DecoratedTypeMirror returnType = (DecoratedTypeMirror) webMethod.getReturnType();
          if (!returnType.isVoid()) {
            imports.add(classnameFor.convert((ImplicitChildElement) webMethod.getWebResult()));
            addComponentTypes(returnType, imports);
          }
        }
      }
      else if (declaration instanceof TypeDefinition) {
        for (Attribute attribute : ((TypeDefinition) declaration).getAttributes()) {
          imports.add(classnameFor.convert(attribute));
          addComponentTypes(attribute.getAccessorType(), imports);
        }
        for (Element element : ((TypeDefinition) declaration).getElements()) {
          imports.add(classnameFor.convert(element));
          addComponentTypes(element.getAccessorType(), imports);
        }
        Value value = ((TypeDefinition) declaration).getValue();
        if (value != null) {
          imports.add(classnameFor.convert(value));
          addComponentTypes(value.getAccessorType(), imports);
        }
      }
      else if (declaration instanceof WebMethod) {
        WebMethod webMethod = (WebMethod) declaration;
        DecoratedTypeMirror returnType = (DecoratedTypeMirror) webMethod.getReturnType();
        if (!returnType.isVoid()) {
          imports.add(classnameFor.convert((ImplicitChildElement) webMethod.getWebResult()));
          addComponentTypes(returnType, imports);
        }
      }
      else {
        throw new TemplateException("The declaration must be either an endpoint interface or a web method.");
      }
    }
    catch (TemplateModelException e) {
      throw new TemplateException(e);
    }

    Iterator<String> importsIt = imports.iterator();
    while (importsIt.hasNext()) {
      //clear out any of the primitives and primary actionscript types; they don't need an import.
      String imprt = importsIt.next();
      if (imprt.lastIndexOf('.') < 0) {
        importsIt.remove();
      }
    }

    return imports.iterator();
  }

  /**
   * Add the component types of the given type mirror to the given set of imports.
   *
   * @param type The type.
   * @param imports The imports.
   */
  protected void addComponentTypes(TypeMirror type, Set<String> imports) throws TemplateModelException {
    if (includeComponentTypes) {
      if (type instanceof MapType) {
        MapType mapType = ((MapType) type);
        imports.add(classnameFor.convert(mapType.getKeyType()));
        imports.add(classnameFor.convert(mapType.getValueType()));
      }
      else if (((DecoratedTypeMirror) type).isCollection()) {
        DeclaredType declaredType = (DeclaredType) type;
        Iterator<TypeMirror> actualTypeArguments = declaredType.getActualTypeArguments().iterator();
        if (actualTypeArguments.hasNext()) {
          imports.add(classnameFor.convert(actualTypeArguments.next()));
        }
      }
      else if (((DecoratedTypeMirror) type).isArray()) {
        imports.add(classnameFor.convert(((ArrayType) type).getComponentType()));
      }
    }
  }

  @Override
  protected void setupModelForLoop(TemplateModel model, String imprt, int index) throws TemplateException {
    super.setupModelForLoop(model, imprt, index);

    if (this.var != null) {
      getModel().setVariable(this.var, imprt);
    }
  }

  /**
   * The variable into which to store the current import.
   *
   * @return The variable into which to store the current import.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable into which to store the current import.
   *
   * @param var The variable into which to store the current import.
   */
  public void setVar(String var) {
    this.var = var;
  }

  /**
   * The declaration for which to get the unique imports.
   *
   * @return The declaration for which to get the unique imports.
   */
  public DecoratedMemberDeclaration getDeclaration() {
    return declaration;
  }

  /**
   * The declaration for which to get the unique imports.
   *
   * @param declaration The declaration for which to get the unique imports.
   */
  public void setDeclaration(DecoratedMemberDeclaration declaration) {
    this.declaration = declaration;
  }

  /**
   * Whether to add the component types of a collection or map.
   *
   * @return Whether to add the component types of a collection or map.
   */
  public boolean isIncludeComponentTypes() {
    return includeComponentTypes;
  }

  /**
   * Whether to add the component types of a collection or map.
   *
   * @param includeComponentTypes Whether to add the component types of a collection or map.
   */
  public void setIncludeComponentTypes(boolean includeComponentTypes) {
    this.includeComponentTypes = includeComponentTypes;
  }
}
