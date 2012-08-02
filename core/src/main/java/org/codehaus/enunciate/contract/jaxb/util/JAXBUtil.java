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

package org.codehaus.enunciate.contract.jaxb.util;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.*;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import net.sf.jelly.apt.decorations.declaration.DecoratedDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedTypeDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.Context;

import java.util.Collection;
import java.util.Iterator;
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
  public static TypeMirror unwrapComponentType(TypeMirror typeMirror) {
    DecoratedTypeMirror decoratedType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror);

    if (decoratedType.isArray()) {
      typeMirror = ((ArrayType) decoratedType).getComponentType();
    }
    else if (decoratedType.isCollection()) {
      //if it's a collection type, the xml type is its component type.
      Iterator<TypeMirror> actualTypeArguments = ((DeclaredType) decoratedType).getActualTypeArguments().iterator();
      if (!actualTypeArguments.hasNext()) {
        //no type arguments, java.lang.Object type.
        AnnotationProcessorEnvironment ape = Context.getCurrentEnvironment();
        typeMirror = ape.getTypeUtils().getDeclaredType(ape.getTypeDeclaration(Object.class.getName()));
      }
      else {
        typeMirror = actualTypeArguments.next();
      }
    }
    else {
      return typeMirror;
    }

    return typeMirror;
  }

  /**
   * Strip the extensions of java.util.Collection or java.util.List until you get the raw type mirror.
   *
   * @param typeMirror The type mirror to strip.
   * @return The strip or the original type mirror.
   */
  public static TypeMirror stripCollectionExtensions(TypeMirror typeMirror) {
    TypeMirror tm = JAXBUtil.findCollectionOrList(typeMirror);
    if (tm != null) {
      return TypeMirrorDecorator.decorate(tm);
    }
    else {
      return typeMirror;
    }
  }

  public static TypeMirror findCollectionOrList(TypeMirror typeMirror) {
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
            found = findCollectionOrList(si);
            if (found != null) {
              break;
            }
          }

          if (found == null && decl instanceof ClassDeclaration) {
            found = findCollectionOrList(((ClassDeclaration) decl).getSuperclass());
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

    return TypeMirrorDecorator.decorate(found);
  }

}
