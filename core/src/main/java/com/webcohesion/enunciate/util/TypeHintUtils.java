/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * @author Ryan Heaton
 */
public class TypeHintUtils {

  private TypeHintUtils() {}


  public static TypeMirror getTypeHint(TypeHint hintInfo, ProcessingEnvironment env, TypeMirror defaultValue) {
    TypeMirror typeMirror;
    try {
      Class hint = hintInfo.value();
      if (TypeHint.NO_CONTENT.class.equals(hint)) {
        typeMirror = env.getTypeUtils().getNoType(TypeKind.VOID);
      }
      else {
        String hintName = hint.getName();

        if (TypeHint.NONE.class.equals(hint)) {
          hintName = hintInfo.qualifiedName();
        }

        if (!"##NONE".equals(hintName)) {
          TypeElement type = env.getElementUtils().getTypeElement(hintName);
          typeMirror = TypeMirrorDecorator.decorate(env.getTypeUtils().getDeclaredType(type), env);
        }
        else {
          typeMirror = defaultValue;
        }
      }
    }
    catch (MirroredTypeException e) {
      DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(e.getTypeMirror(), env);
      if (decorated.isInstanceOf(TypeHint.NO_CONTENT.class)) {
        typeMirror = env.getTypeUtils().getNoType(TypeKind.VOID);
      }
      else if (decorated instanceof DeclaredType){
        String hintName = ((TypeElement)((DeclaredType)decorated).asElement()).getQualifiedName().toString();

        if (decorated.isInstanceOf(TypeHint.NONE.class)) {
          hintName = hintInfo.qualifiedName();
        }

        if (!"##NONE".equals(hintName)) {
          TypeElement type = env.getElementUtils().getTypeElement(hintName);
          typeMirror = TypeMirrorDecorator.decorate(env.getTypeUtils().getDeclaredType(type), env);
        }
        else {
          typeMirror = defaultValue;
        }
      }
      else {
        typeMirror = decorated;
      }
    }
    return typeMirror;
  }
}
