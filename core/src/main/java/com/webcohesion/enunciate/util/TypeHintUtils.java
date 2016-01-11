package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
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
      typeMirror = TypeMirrorDecorator.decorate(e.getTypeMirror(), env);
    }

    if (hintInfo.asCollectionOf()) {
      typeMirror = env.getTypeUtils().getArrayType(typeMirror);
    }

    return typeMirror;
  }
}
