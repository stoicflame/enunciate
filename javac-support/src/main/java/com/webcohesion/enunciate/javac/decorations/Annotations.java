package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class Annotations {

  private Annotations(){}

  public static DecoratedTypeMirror mirrorOf(Callable<Class<?>> annotationValueFunction, ProcessingEnvironment env) {
    return mirrorOf(annotationValueFunction, env, null);
  }

  public static DecoratedTypeMirror mirrorOf(Callable<Class<?>> annotationValueFunction, ProcessingEnvironment env, Class<?> emptyClass) {
    try {
      Class<?> clazz = annotationValueFunction.call();
      if (emptyClass != null && emptyClass.equals(clazz)) {
        return null;
      }
      return TypeMirrorUtils.mirrorOf(clazz, env);
    }
    catch (MirroredTypeException e) {
      DecoratedTypeMirror typeMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(e.getTypeMirror(), env);
      if (emptyClass != null && typeMirror.isInstanceOf(emptyClass)) {
        return null;
      }
      return typeMirror;
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static List<DecoratedTypeMirror> mirrorsOf(Callable<Class<?>[]> annotationValueFunction, ProcessingEnvironment env) {
    try {
      Class<?>[] classes = annotationValueFunction.call();
      List<DecoratedTypeMirror> typeMirrors = new ArrayList<DecoratedTypeMirror>(classes.length);
      for (Class<?> clazz : classes) {
        typeMirrors.add(TypeMirrorUtils.mirrorOf(clazz, env));
      }
      return typeMirrors;
    }
    catch (MirroredTypesException e) {
      return (List<DecoratedTypeMirror>) TypeMirrorDecorator.decorate(e.getTypeMirrors(), env);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
