package com.webcohesion.enunciate.modules.jackson1.javac;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedArrayType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;

/**
 * Needed to contain synthetic types.
 */
public class SyntheticJackson1ArrayType extends DecoratedArrayType {

  private final DecoratedTypeMirror component;

  public SyntheticJackson1ArrayType(ArrayType delegate, DecoratedTypeMirror component, DecoratedProcessingEnvironment env) {
    super(delegate, env);
    this.component = component;
  }

  @Override
  public TypeMirror getComponentType() {
    return this.component;
  }
}
