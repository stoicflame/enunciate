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

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class OptionalUtils {

  private OptionalUtils() {}

  public static DecoratedTypeMirror stripOptional(DecoratedTypeMirror type, DecoratedProcessingEnvironment env) {
    if (type instanceof DeclaredType) {
      Element element = ((DeclaredType) type).asElement();
      if (element instanceof TypeElement) {
        String fqn = ((TypeElement) element).getQualifiedName().toString();
        if (fqn.equals("java.util.Optional")) {
          List<? extends TypeMirror> typeArgs = ((DeclaredType) type).getTypeArguments();
          if (typeArgs.size() == 1) {
            type = (DecoratedTypeMirror) typeArgs.get(0);
          }
          else {
            type = TypeMirrorUtils.objectType(env);
          }
        }
        else if (fqn.equals("java.util.OptionalInt")) {
          type = (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.INT);
        }
        else if (fqn.equals("java.util.OptionalDouble")) {
          type = (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.DOUBLE);
        }
        else if (fqn.equals("java.util.OptionalLong")) {
          type = (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.LONG);
        }
      }
    }

    return type;
  }
}
