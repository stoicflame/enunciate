package com.webcohesion.enunciate.javac.decorations.element;

import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class ElementUtils {

  private ElementUtils() {}

  public static String findDeprecationMessage(DecoratedElement<?> el) {
    Deprecated deprecation = el.getAnnotation(Deprecated.class);
    String message = null;
    if (deprecation != null) {
      message = "";
    }

    JavaDoc.JavaDocTagList tagList = el.getJavaDoc().get("deprecated");
    if (tagList != null) {
      message = tagList.toString();
    }
    return message;
  }

  public static boolean isCollection(TypeElement declaration) {
    if (declaration != null) {
      String fqn = declaration.getQualifiedName().toString();
      if (Collection.class.getName().equals(fqn)) {
        return true;
      }
      else if (Object.class.getName().equals(fqn)) {
        return false;
      }
      else {
        TypeMirror superclass = declaration.getSuperclass();
        if (superclass instanceof DeclaredType && isCollection((TypeElement) ((DeclaredType) superclass).asElement())) {
          return true;
        }

        for (TypeMirror interfaceType : declaration.getInterfaces()) {
          if (interfaceType instanceof DeclaredType && isCollection((TypeElement) ((DeclaredType) interfaceType).asElement())) {
            return true;
          }
        }
      }
    }

    return false;
  }

  public static boolean isMap(TypeElement declaration) {
    if (declaration != null) {
      String fqn = declaration.getQualifiedName().toString();
      if (Map.class.getName().equals(fqn)) {
        return true;
      }
      else if (Object.class.getName().equals(fqn)) {
        return false;
      }
      else {
        TypeMirror superclass = declaration.getSuperclass();
        if (superclass instanceof DeclaredType && isMap((TypeElement) ((DeclaredType) superclass).asElement())) {
          return true;
        }

        for (TypeMirror interfaceType : declaration.getInterfaces()) {
          if (interfaceType instanceof DeclaredType && isMap((TypeElement) ((DeclaredType) interfaceType).asElement())) {
            return true;
          }
        }
      }
    }

    return false;
  }
}
