package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.MirroredTypeException;
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

}
