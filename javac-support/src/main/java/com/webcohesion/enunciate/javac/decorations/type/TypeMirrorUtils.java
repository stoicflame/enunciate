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
package com.webcohesion.enunciate.javac.decorations.type;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Ryan Heaton
 */
public class TypeMirrorUtils {

  private static final String OBJECT_TYPE_PROPERTY = "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#OBJECT_TYPE_PROPERTY";
  private static final String COLLECTION_TYPE_PROPERTY = "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#COLLECTION_TYPE_PROPERTY";
  private static final String COLLECTION_TYPE_ERASURE_PROPERTY = "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#COLLECTION_TYPE_ERASURE_PROPERTY";
  private static final String LIST_TYPE_PROPERTY = "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#LIST_TYPE_PROPERTY";
  private static final String LIST_TYPE_ERASURE_PROPERTY = "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#LIST_TYPE_ERASURE_PROPERTY";
  private static final String STREAM_TYPE_PROPERTY = "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#STREAM_TYPE_PROPERTY";
  private static final String STREAM_TYPE_ERASURE_PROPERTY = "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#STREAM_TYPE_ERASURE_PROPERTY";

  private TypeMirrorUtils() {}

  public static DecoratedTypeMirror mirrorOf(Class<?> clazz, ProcessingEnvironment env) {
    return mirrorOf(clazz, env, true);
  }
  
  static DecoratedTypeMirror mirrorOf(Class<?> clazz, ProcessingEnvironment env, boolean require) {
    if (clazz.isArray()) {
      return (DecoratedTypeMirror) env.getTypeUtils().getArrayType(mirrorOf(clazz.getComponentType(), env));
    }
    else if (clazz.isPrimitive()) {
      return (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.valueOf(clazz.getName().toUpperCase()));
    }
    else {
      TypeElement element = env.getElementUtils().getTypeElement(clazz.getCanonicalName());
      if (element == null && !require) {
        return null;
      }
      else if (element == null) {
        throw new IllegalStateException("Unable to find mirror for " + clazz.getCanonicalName());
      }
      return (DecoratedTypeMirror) element.asType();
    }
  }

  public static DecoratedTypeMirror mirrorOf(String typeName, DecoratedProcessingEnvironment env) {
    return mirrorOf(typeName, env, false);
  }

  public static DecoratedDeclaredType objectType(DecoratedProcessingEnvironment env) {
    DecoratedDeclaredType objectType = (DecoratedDeclaredType) env.getProperty(OBJECT_TYPE_PROPERTY);
    if (objectType == null) {
      objectType = (DecoratedDeclaredType) env.getElementUtils().getTypeElement(Object.class.getName()).asType();
      env.setProperty(OBJECT_TYPE_PROPERTY, objectType);
    }
    return objectType;
  }

  public static DecoratedDeclaredType streamType(DecoratedProcessingEnvironment env) {
    DecoratedDeclaredType streamType = (DecoratedDeclaredType) env.getProperty(STREAM_TYPE_PROPERTY);
    if (streamType == null) {
      streamType = (DecoratedDeclaredType) env.getElementUtils().getTypeElement(Stream.class.getName()).asType();
      env.setProperty(STREAM_TYPE_PROPERTY, streamType);
    }
    return streamType;
  }

  public static DecoratedDeclaredType streamTypeErasure(DecoratedProcessingEnvironment env) {
    DecoratedDeclaredType streamType = (DecoratedDeclaredType) env.getProperty(STREAM_TYPE_ERASURE_PROPERTY);
    if (streamType == null) {
      streamType = (DecoratedDeclaredType) env.getTypeUtils().erasure(streamType(env));
      env.setProperty(STREAM_TYPE_ERASURE_PROPERTY, streamType);
    }
    return streamType;
  }

  public static DecoratedDeclaredType collectionType(DecoratedProcessingEnvironment env) {
    DecoratedDeclaredType collectionType = (DecoratedDeclaredType) env.getProperty(COLLECTION_TYPE_PROPERTY);
    if (collectionType == null) {
      collectionType = (DecoratedDeclaredType) env.getElementUtils().getTypeElement(Collection.class.getName()).asType();
      env.setProperty(COLLECTION_TYPE_PROPERTY, collectionType);
    }
    return collectionType;
  }

  public static DecoratedDeclaredType collectionTypeErasure(DecoratedProcessingEnvironment env) {
    DecoratedDeclaredType collectionType = (DecoratedDeclaredType) env.getProperty(COLLECTION_TYPE_ERASURE_PROPERTY);
    if (collectionType == null) {
      collectionType = (DecoratedDeclaredType) env.getTypeUtils().erasure(collectionType(env));
      env.setProperty(COLLECTION_TYPE_PROPERTY, collectionType);
    }
    return collectionType;
  }

  public static DecoratedDeclaredType listType(DecoratedProcessingEnvironment env) {
    DecoratedDeclaredType listType = (DecoratedDeclaredType) env.getProperty(LIST_TYPE_PROPERTY);
    if (listType == null) {
      listType = (DecoratedDeclaredType) env.getTypeUtils().erasure(env.getElementUtils().getTypeElement(List.class.getName()).asType());
      env.setProperty(LIST_TYPE_PROPERTY, listType);
    }
    return listType;
  }

  public static DecoratedDeclaredType listTypeErasure(DecoratedProcessingEnvironment env) {
    DecoratedDeclaredType listType = (DecoratedDeclaredType) env.getProperty(LIST_TYPE_ERASURE_PROPERTY);
    if (listType == null) {
      listType = (DecoratedDeclaredType) env.getTypeUtils().erasure(listType(env));
      env.setProperty(LIST_TYPE_PROPERTY, listType);
    }
    return listType;
  }

  public static TypeMirror resolveTypeVariable(TypeMirror typeVariable, List<? extends TypeParameterElement> elementParams, List<? extends TypeMirror> elementArgs) {
    if (typeVariable.getKind() == TypeKind.TYPEVAR) {
      int argIndex = -1;

      Name name = ((TypeVariable) typeVariable).asElement().getSimpleName();
      for (int i = 0; i < elementParams.size(); i++) {
        TypeParameterElement elementParam = elementParams.get(i);
        if (elementParam.getSimpleName().equals(name)) {
          argIndex = i;
          break;
        }
      }

      if (argIndex < 0 || elementArgs.size() != elementParams.size()) {
        //best we can do is get the upper bound. should this maybe be an illegal state?
        typeVariable = ((TypeVariable) typeVariable).getUpperBound();
      }
      else {
        typeVariable = elementArgs.get(argIndex);
      }
    }

    return typeVariable;
  }

  private static DecoratedTypeMirror mirrorOf(String typeName, DecoratedProcessingEnvironment env, boolean inArray) {
    DecoratedTypeMirror cached = (DecoratedTypeMirror) env.getProperty(mirrorKey(typeName));
    if (cached != null) {
      return cached;
    }

    if (typeName.startsWith("[")) {
      return (DecoratedTypeMirror) env.getTypeUtils().getArrayType(mirrorOf(typeName.substring(1), env, true));
    }
    else if (typeName.endsWith("[]")) {
      return (DecoratedTypeMirror) env.getTypeUtils().getArrayType(mirrorOf(typeName.substring(0, typeName.length() - 2), env, false));
    }
    else if (inArray) {
      char firstChar = typeName.charAt(0);
      if (firstChar == 'L' && typeName.endsWith(";")) {
        return mirrorOf(typeName.substring(1, typeName.length() - 2), env, false);
      }

      switch (firstChar) {
        case 'Z':
          return (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.BOOLEAN);
        case 'B':
          return (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.BYTE);
        case 'C':
          return (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.CHAR);
        case 'D':
          return (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.DOUBLE);
        case 'F':
          return (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.FLOAT);
        case 'I':
          return (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.INT);
        case 'L':
          return (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.LONG);
        case 'S':
          return (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.SHORT);
      }
    }
    else {
      try {
        TypeKind kind = TypeKind.valueOf(typeName.toUpperCase());
        if (kind.isPrimitive()) {
          return (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(kind);
        }
      }
      catch (IllegalArgumentException e) {
        TypeElement element = env.getElementUtils().getTypeElement(typeName);
        if (element != null) {
          return (DecoratedTypeMirror) env.getTypeUtils().getDeclaredType(element);
        }
      }
    }

    return null;
  }

  private static String mirrorKey(String typeName) {
    return "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#MIRROR_OF_" + typeName;
  }

  public static DecoratedTypeMirror getComponentType(DecoratedTypeMirror typeMirror, DecoratedProcessingEnvironment env) {
    if (typeMirror.isCollection()) {
      List<? extends TypeMirror> itemTypes = ((DeclaredType) typeMirror).getTypeArguments();
      if (itemTypes.isEmpty()) {
        return objectType(env);
      }
      else {
        return (DecoratedTypeMirror) itemTypes.get(0);
      }
    }
    else if (typeMirror instanceof ArrayType) {
      return (DecoratedTypeMirror) ((ArrayType) typeMirror).getComponentType();
    }

    return null;
  }
}
