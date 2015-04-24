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

package com.webcohesion.enunciate.modules.jaxb.model.util;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

/**
 * Consolidation of common logic for implementing the JAXB contract.
 * 
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class JAXBUtil {

  public static DecoratedTypeMirror getComponentType(DecoratedTypeMirror typeMirror, DecoratedProcessingEnvironment env) {
    if (typeMirror.isCollection()) {
      List<? extends TypeMirror> itemTypes = ((DeclaredType) typeMirror).getTypeArguments();
      if (itemTypes.isEmpty()) {
        typeMirror = TypeMirrorUtils.objectType(env);
      }
      else {
        typeMirror = (DecoratedTypeMirror) itemTypes.get(0);
      }
    }
    else if (typeMirror instanceof ArrayType) {
      typeMirror = (DecoratedTypeMirror) ((ArrayType) typeMirror).getComponentType();
    }

    return typeMirror;
  }

  public static DecoratedDeclaredType getNormalizedCollection(DecoratedTypeMirror typeMirror, DecoratedProcessingEnvironment env) {
    DecoratedDeclaredType base = typeMirror.isList() ? TypeMirrorUtils.listType(env) : typeMirror.isCollection() ? TypeMirrorUtils.collectionType(env) : null;

    if (base != null) {
      //now narrow the component type to what can be valid xml.
      List<? extends DecoratedTypeMirror> typeArgs = (List<? extends DecoratedTypeMirror>) base.getTypeArguments();
      if (typeArgs.size() == 1) {
        DecoratedTypeMirror componentType = typeArgs.get(0);
        Element element = env.getTypeUtils().asElement(componentType);

        //the interface isn't adapted, check for @XmlTransient and if it's there, narrow it to java.lang.Object.
        //see https://jira.codehaus.org/browse/ENUNCIATE-660
        if (element == null || (element.getAnnotation(XmlJavaTypeAdapter.class) == null && element.getAnnotation(XmlTransient.class) != null)) {
          return base;
        }

        base = (DecoratedDeclaredType) env.getTypeUtils().getDeclaredType((TypeElement) TypeMirrorUtils.collectionType(env).asElement(), componentType);
      }
    }

    return base;
  }

}
