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

package org.codehaus.enunciate.modules.objc;

import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.EnumType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.contract.jaxb.Accessor;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;

import java.util.List;

import net.sf.jelly.apt.freemarker.FreemarkerModel;

/**
 * Template method used to lookup the function identifier for a given type. Only Objective C primitive types
 * have a function identifier. Returns null if no function identifier is provided.
 *
 * @author Ryan Heaton
 */
public class FunctionIdentifierForMethod implements TemplateMethodModelEx {

  private final NameForTypeDefinitionMethod typeDefName;

  public FunctionIdentifierForMethod(NameForTypeDefinitionMethod typeDefName) {
    this.typeDefName = typeDefName;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The functionIdentifierFor method must have an accessor or type mirror as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    TypeMirror typeMirror;
    if (unwrapped instanceof Accessor) {
      Accessor accessor = (Accessor) unwrapped;
      if (accessor.isAdapted()) {
        typeMirror = accessor.getAdapterType().getAdaptingType();
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
      switch (((PrimitiveType) typeMirror).getKind()) {
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
          return (((PrimitiveType) typeMirror).getKind()).toString();
      }
    }
    else if (typeMirror instanceof EnumType) {
      EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();
      TypeDefinition typeDefinition = model.findTypeDefinition(((EnumType) typeMirror).getDeclaration());
      if (typeDefinition != null) {
        return typeDefName.calculateName(typeDefinition);
      }
    }
    else if ((typeMirror instanceof DeclaredType) && (((DeclaredType)typeMirror).getDeclaration() != null)) {
      String classname = ((DeclaredType) typeMirror).getDeclaration().getQualifiedName();
      if (Boolean.class.getName().equals(classname)) {
        return "Boolean";
      }
      else if (Byte.class.getName().equals(classname)) {
        return "Byte";
      }
      else if (Character.class.getName().equals(classname)) {
        return "Character";
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

    return null;
  }
}