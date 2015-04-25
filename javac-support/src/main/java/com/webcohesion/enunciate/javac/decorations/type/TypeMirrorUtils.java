package com.webcohesion.enunciate.javac.decorations.type;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import java.util.Collection;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class TypeMirrorUtils {

  private static final String OBJECT_TYPE_PROPERTY = "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#OBJECT_TYPE_PROPERTY";
  private static final String COLLECTION_TYPE_PROPERTY = "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#COLLECTION_TYPE_PROPERTY";
  private static final String LIST_TYPE_PROPERTY = "com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils#LIST_TYPE_PROPERTY";

  private TypeMirrorUtils() {}

  public static DecoratedTypeMirror mirrorOf(Class<?> clazz, ProcessingEnvironment env) {
    if (clazz.isArray()) {
      return (DecoratedTypeMirror) env.getTypeUtils().getArrayType(mirrorOf(clazz.getComponentType(), env));
    }
    else if (clazz.isPrimitive()) {
      return (DecoratedTypeMirror) env.getTypeUtils().getPrimitiveType(TypeKind.valueOf(clazz.getName().toUpperCase()));
    }
    else {
      return (DecoratedTypeMirror) env.getElementUtils().getTypeElement(clazz.getName()).asType();
    }
  }

  public static DecoratedTypeMirror mirrorOf(String typeName, ProcessingEnvironment env) {
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

  public static DecoratedDeclaredType collectionType(DecoratedProcessingEnvironment env) {
    DecoratedDeclaredType collectionType = (DecoratedDeclaredType) env.getProperty(COLLECTION_TYPE_PROPERTY);
    if (collectionType == null) {
      collectionType = (DecoratedDeclaredType) env.getElementUtils().getTypeElement(Collection.class.getName()).asType();
      env.setProperty(COLLECTION_TYPE_PROPERTY, collectionType);
    }
    return collectionType;
  }

  public static DecoratedDeclaredType listType(DecoratedProcessingEnvironment env) {
    DecoratedDeclaredType listType = (DecoratedDeclaredType) env.getProperty(LIST_TYPE_PROPERTY);
    if (listType == null) {
      listType = (DecoratedDeclaredType) env.getElementUtils().getTypeElement(List.class.getName()).asType();
      env.setProperty(LIST_TYPE_PROPERTY, listType);
    }
    return listType;
  }

  private static DecoratedTypeMirror mirrorOf(String typeName, ProcessingEnvironment env, boolean inArray) {
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
        return (DecoratedTypeMirror) env.getElementUtils().getTypeElement(typeName).asType();
      }
    }

    return null;
  }

}
