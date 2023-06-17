package com.webcohesion.enunciate.modules.lombok;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.javac.decorations.AnnotationMirrorDecoration;
import com.webcohesion.enunciate.javac.decorations.ElementDecoration;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecoration;
import com.webcohesion.enunciate.module.BasicEnunicateModule;
import com.webcohesion.enunciate.module.ContextModifyingModule;

import java.util.Collections;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class LombokModule extends BasicEnunicateModule implements ContextModifyingModule {

  @Override
  public String getName() {
    return "lombok";
  }

  @Override
  public List<ElementDecoration> getElementDecorations() {
    return Collections.singletonList(new LombokDecoration());
  }

  @Override
  public List<TypeMirrorDecoration> getTypeMirrorDecorations() {
    return Collections.emptyList();
  }

  @Override
  public List<AnnotationMirrorDecoration> getAnnotationMirrorDecorations() {
    return Collections.emptyList();
  }

  @Override
  public void call(EnunciateContext context) {
    //no-op.
  }

}
