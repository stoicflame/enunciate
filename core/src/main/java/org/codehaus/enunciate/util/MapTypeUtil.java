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

package org.codehaus.enunciate.util;

import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;

import java.util.Collection;
import java.util.Iterator;

import net.sf.jelly.apt.Context;

/**
 * Utility for handling map types.
 *
 * @author Ryan Heaton
 */
public class MapTypeUtil {

  /**
   * Finds the map type for the specified type mirror, if it exists.
   *
   * @param typeMirror The type mirror.
   * @return The map type or null.
   */
  public static MapType findMapType(TypeMirror typeMirror) {
    if (!(typeMirror instanceof DeclaredType)) {
      return null;
    }
    else if (typeMirror instanceof MapType) {
      return (MapType) typeMirror;
    }

    DeclaredType declaredType = (DeclaredType) typeMirror;
    TypeDeclaration declaration = declaredType.getDeclaration();
    if (declaration == null) {
      return null;
    }
    else if ("java.util.Map".equals(declaration.getQualifiedName())) {
      TypeMirror keyType = null;
      TypeMirror valueType = null;

      Collection<TypeMirror> typeArgs = declaredType.getActualTypeArguments();
      if ((typeArgs != null) && (typeArgs.size() == 2)) {
        Iterator<TypeMirror> argIt = typeArgs.iterator();
        keyType = argIt.next();
        valueType = argIt.next();
      }

      if ((keyType == null) || (valueType == null)) {
        AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
        TypeMirror objectType = env.getTypeUtils().getDeclaredType(env.getTypeDeclaration(Object.class.getName()));
        keyType = objectType;
        valueType = objectType;
      }

      return new MapType((InterfaceType) declaredType, keyType, valueType);
    }
    else {
      MapType mapType = null;
      Collection<InterfaceType> superInterfaces = declaredType.getSuperinterfaces();
      for (InterfaceType superInterface : superInterfaces) {
        mapType = findMapType(superInterface);
        if (mapType != null) {
          break;
        }
      }

      if ((mapType == null) && (declaredType instanceof ClassType)) {
        mapType = findMapType(((ClassType) declaredType).getSuperclass());
      }

      if (mapType != null) {
        mapType.setOriginalType(declaredType);
      }

      return mapType;
    }
  }
}
