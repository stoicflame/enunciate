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

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.List;

/**
 * Consolidation of common logic for implementing the JAXB contract.
 * 
 * @author Ryan Heaton
 */
public class JAXBUtil {

  /**
   * Unwraps the specified type to its component type if its a collection or an array.  Otherwise, the
   * specified type is returned.
   *
   * @param typeMirror The type to unwrap if necessary.
   * @return The component type, or the type itself.
   */
  public static TypeMirror unwrapComponentType(TypeMirror typeMirror, EnunciateContext context) {
    if (context.isInstanceOf(typeMirror, java.util.Collection.class)) {
      List<? extends TypeMirror> itemTypes = ((DeclaredType) typeMirror).getTypeArguments();
      if (itemTypes.isEmpty()) {
        typeMirror = context.getTypeElement(Object.class.getName()).asType();
      }
      else {
        typeMirror = itemTypes.get(0);
      }
    }
    else if (typeMirror instanceof ArrayType) {
      typeMirror = ((ArrayType) typeMirror).getComponentType();
    }

    return typeMirror;
  }

  public static TypeMirror getNormalizedCollection(TypeMirror typeMirror, EnunciateContext context) {
    TypeMirror base = findCollectionStrippedOfExtensions(typeMirror);
    if (base != null) {
      //now narrow the component type to what can be valid xml.
      List<? extends TypeMirror> typeArgs = ((DeclaredType) base).getTypeArguments();
      if (typeArgs.size() == 1) {
        TypeMirror candidateToNarrow = typeArgs.get(0);
        NarrowingCollectionComponentVisitor visitor = new NarrowingCollectionComponentVisitor();
        candidateToNarrow.accept(visitor);
        TypeMirror narrowing = visitor.getResult();
        if (narrowing != null) {
          TypeDeclaration decl = ((DeclaredType) base).getDeclaration();
          while (decl instanceof DecoratedTypeDeclaration) {
            decl = (TypeDeclaration) ((DecoratedTypeDeclaration) decl).getDelegate();
          }

          while (narrowing instanceof DecoratedTypeMirror) {
            narrowing = ((DecoratedTypeMirror) narrowing).getDelegate();
          }

          base = Context.getCurrentEnvironment().getTypeUtils().getDeclaredType(decl, narrowing);
        }
      }
    }
    return TypeMirrorDecorator.decorate(base);
  }

  private static TypeMirror findCollectionStrippedOfExtensions(TypeMirror typeMirror) {
    TypeMirror found = null;

    if (typeMirror instanceof DeclaredType) {
      TypeDeclaration decl = ((DeclaredType) typeMirror).getDeclaration();
      if (decl != null) {
        String qn = decl.getQualifiedName();
        if (List.class.getName().equals(qn) || Collection.class.getName().equals(qn)) {
          return typeMirror;
        }
        else {
          for (InterfaceType si : decl.getSuperinterfaces()) {
            found = findCollectionStrippedOfExtensions(si);
            if (found != null) {
              break;
            }
          }

          if (found == null && decl instanceof ClassDeclaration) {
            found = findCollectionStrippedOfExtensions(((ClassDeclaration) decl).getSuperclass());
          }

          if (found != null) {
            TypeParameterDeclaration typeParam = null;
            for (TypeMirror typeArg : ((DeclaredType) found).getActualTypeArguments()) {
              if (typeArg instanceof TypeVariable) {
                typeParam = ((TypeVariable) typeArg).getDeclaration();
                break;
              }
            }

            if (typeParam != null) {
              int typeArgIndex = -1;
              for (TypeParameterDeclaration typeParamDeclaration : decl.getFormalTypeParameters()) {
                typeArgIndex++;
                if (typeParam.getSimpleName().equals(typeParamDeclaration.getSimpleName())) {
                  Iterator<TypeMirror> resolvingTypeArgs = ((DeclaredType) typeMirror).getActualTypeArguments().iterator();
                  TypeMirror resolved = null;
                  for (int resolvingTypeIndex = 0; resolvingTypeIndex <= typeArgIndex && resolvingTypeArgs.hasNext(); resolvingTypeIndex++) {
                    resolved = resolvingTypeArgs.next();
                  }
                  if (resolved != null) {
                    //got the resolved type mirror, create a new type mirror with the resolved argument instead.
                    TypeDeclaration foundDecl = ((DeclaredType) found).getDeclaration();
                    while (foundDecl instanceof DecoratedTypeDeclaration) {
                      foundDecl = (TypeDeclaration) ((DecoratedTypeDeclaration) foundDecl).getDelegate();
                    }

                    while (resolved instanceof DecoratedTypeMirror) {
                      resolved = ((DecoratedTypeMirror) resolved).getDelegate();
                    }

                    found = Context.getCurrentEnvironment().getTypeUtils().getDeclaredType(foundDecl, resolved);
                  }
                }
              }
            }
          }
        }
      }
    }

    return found;
  }

}
