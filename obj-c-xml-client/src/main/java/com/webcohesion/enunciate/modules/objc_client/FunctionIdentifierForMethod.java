/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.objc_client;

import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import java.util.List;


/**
 * Template method used to lookup the function identifier for a given type. Only Objective C primitive types
 * have a function identifier. Returns null if no function identifier is provided.
 *
 * @author Ryan Heaton
 */
public class FunctionIdentifierForMethod implements TemplateMethodModelEx {

  private final NameForTypeDefinitionMethod typeDefName;
  private final EnunciateJaxbContext context;

  public FunctionIdentifierForMethod(NameForTypeDefinitionMethod typeDefName, EnunciateJaxbContext context) {
    this.typeDefName = typeDefName;
    this.context = context;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The functionIdentifierFor method must have an accessor or type mirror as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = FreemarkerUtil.unwrap(from);
    TypeMirror typeMirror;
    if (unwrapped instanceof Accessor) {
      Accessor accessor = (Accessor) unwrapped;
      if (accessor.isAdapted()) {
        typeMirror = accessor.getAdapterType().getAdaptingType(accessor.getAccessorType(), this.context.getContext());
      }
      else {
        typeMirror = accessor.getAccessorType();
      }
    }
    else if (unwrapped instanceof TypeMirror) {
      typeMirror = (TypeMirror) unwrapped;
    }
    else {
      throw new TemplateModelException("The functionIdentifierFor method must have an accessor or type mirror as a parameter.");
    }


    if (typeMirror instanceof PrimitiveType) {
      switch (typeMirror.getKind()) {
        case BOOLEAN:
          return "Boolean";
        case BYTE:
          return "Byte";
        case CHAR:
          return "Character";
        case DOUBLE:
          return "Double";
        case FLOAT:
          return "Float";
        case INT:
          return "Int";
        case LONG:
          return "Long";
        case SHORT:
          return "Short";
        default:
          return (typeMirror.getKind()).toString();
      }
    }
    else if (typeMirror instanceof DeclaredType) {
      TypeElement declaration = (TypeElement) ((DeclaredType) typeMirror).asElement();
      TypeDefinition typeDefinition = this.context.findTypeDefinition(declaration);
      if (typeDefinition != null) {
        if (typeDefinition instanceof EnumTypeDefinition) {
          return typeDefName.calculateName(typeDefinition);
        }
      }
      else {
        String classname = declaration.getQualifiedName().toString();
        if (Boolean.class.getName().equals(classname)) {
          return "Boolean";
        }
        else if (Byte.class.getName().equals(classname)) {
          return "Byte";
        }
        else if (Character.class.getName().equals(classname)) {
          return "UnsignedShort";
        }
        else if (Double.class.getName().equals(classname)) {
          return "Double";
        }
        else if (Float.class.getName().equals(classname)) {
          return "Float";
        }
        else if (Integer.class.getName().equals(classname)) {
          return "Int";
        }
        else if (Long.class.getName().equals(classname)) {
          return "Long";
        }
        else if (Short.class.getName().equals(classname)) {
          return "Short";
        }
      }
    }

    return null;
  }
}