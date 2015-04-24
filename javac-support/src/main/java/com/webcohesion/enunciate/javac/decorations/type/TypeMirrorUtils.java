package com.webcohesion.enunciate.javac.decorations.type;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class TypeMirrorUtils {

  private static final String OBJECT_TYPE_PROPERTY = "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#OBJECT_TYPE_PROPERTY";
  private static final String COLLECTION_TYPE_PROPERTY = "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#COLLECTION_TYPE_PROPERTY";

  private TypeMirrorUtils() {}

  public static TypeMirror mirrorOf(Class<?> clazz, ProcessingEnvironment env) {
    if (clazz.isArray()) {
      return env.getTypeUtils().getArrayType(mirrorOf(clazz.getComponentType(), env));
    }
    else if (clazz.isPrimitive()) {
      return env.getTypeUtils().getPrimitiveType(TypeKind.valueOf(clazz.getName().toUpperCase()));
    }
    else {
      return env.getElementUtils().getTypeElement(clazz.getName()).asType();
    }
  }

  public static TypeMirror mirrorOf(String typeName, ProcessingEnvironment env) {
    return mirrorOf(typeName, env, false);
  }

  public static DeclaredType objectType(DecoratedProcessingEnvironment env) {
    DeclaredType objectType = (DeclaredType) env.getProperty(OBJECT_TYPE_PROPERTY);
    if (objectType == null) {
      objectType = (DeclaredType) env.getElementUtils().getTypeElement(Object.class.getName()).asType();
      env.setProperty(OBJECT_TYPE_PROPERTY, objectType);
    }
    return objectType;
  }

  public static DeclaredType collectionType(DecoratedProcessingEnvironment env) {
    DeclaredType collectionType = (DeclaredType) env.getProperty(COLLECTION_TYPE_PROPERTY);
    if (collectionType == null) {
      collectionType = (DeclaredType) env.getElementUtils().getTypeElement(Collection.class.getName()).asType();
      env.setProperty(COLLECTION_TYPE_PROPERTY, collectionType);
    }
    return collectionType;
  }

  private static TypeMirror mirrorOf(String typeName, ProcessingEnvironment env, boolean inArray) {
    if (typeName.startsWith("[")) {
      return env.getTypeUtils().getArrayType(mirrorOf(typeName.substring(1), env, true));
    }
    else if (typeName.endsWith("[]")) {
      return env.getTypeUtils().getArrayType(mirrorOf(typeName.substring(0, typeName.length() - 2), env, false));
    }
    else if (inArray) {
      char firstChar = typeName.charAt(0);
      if (firstChar == 'L' && typeName.endsWith(";")) {
        return mirrorOf(typeName.substring(1, typeName.length() - 2), env, false);
      }

      switch (firstChar) {
        case 'Z':
          return env.getTypeUtils().getPrimitiveType(TypeKind.BOOLEAN);
        case 'B':
          return env.getTypeUtils().getPrimitiveType(TypeKind.BYTE);
        case 'C':
          return env.getTypeUtils().getPrimitiveType(TypeKind.CHAR);
        case 'D':
          return env.getTypeUtils().getPrimitiveType(TypeKind.DOUBLE);
        case 'F':
          return env.getTypeUtils().getPrimitiveType(TypeKind.FLOAT);
        case 'I':
          return env.getTypeUtils().getPrimitiveType(TypeKind.INT);
        case 'L':
          return env.getTypeUtils().getPrimitiveType(TypeKind.LONG);
        case 'S':
          return env.getTypeUtils().getPrimitiveType(TypeKind.SHORT);
      }
    }
    else {
      try {
        TypeKind kind = TypeKind.valueOf(typeName.toUpperCase());
        if (kind.isPrimitive()) {
          return env.getTypeUtils().getPrimitiveType(kind);
        }
      }
      catch (IllegalArgumentException e) {
        return env.getElementUtils().getTypeElement(typeName).asType();
      }
    }

    return null;
  }

}
