/*
 * © 2024 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.javac.decorations.element;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.ElementDecorator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class DecoratedRecordComponentElement extends DecoratedElement<RecordComponentElement> implements RecordComponentElement {
  
  private DecoratedExecutableElement accessor;

  public DecoratedRecordComponentElement(RecordComponentElement delegate, DecoratedProcessingEnvironment env) {
    super(delegate, env);
  }

  @Override
  public ExecutableElement getAccessor() {
    if (this.accessor == null) {
      this.accessor = new DecoratedExecutableElement(this.delegate.getAccessor(), this.env);
    }
    return this.accessor;
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    if (this.annotationMirrors == null) {
      this.annotationMirrors = new ArrayList<>();
      this.annotationMirrors.addAll(ElementDecorator.decorateAnnotationMirrors(delegate.getAnnotationMirrors(), env));
      this.annotationMirrors.addAll(getAccessor().getAnnotationMirrors());
    }
    return this.annotationMirrors;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    A annotation = super.getAnnotation(annotationType);
    if (annotation == null) {
      annotation = getAccessor().getAnnotation(annotationType);
    }
    return annotation;
  }
}
