/*
 * © 2023 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

public class SwaggerEntity implements Entity {
  
  private final List<Entity> delegates;

  public SwaggerEntity(List<Entity> delegates) {
    this.delegates = delegates;
  }

  private <R> R doDelegation(Function<Entity, R> accessor) {
    return this.delegates.stream().map(accessor)
       .filter(Objects::nonNull)
       .findFirst().orElse(null);
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return doDelegation(delegate -> delegate.getAnnotation(annotationType));
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    LinkedHashMap<String, AnnotationMirror> annotations = new LinkedHashMap<>();
    this.delegates.forEach(delegate -> annotations.putAll(delegate.getAnnotations()));
    return annotations;
  }

  @Override
  public String getDescription() {
    return doDelegation(Entity::getDescription);
  }

  @Override
  public List<? extends MediaTypeDescriptor> getMediaTypes() {
    ArrayList<MediaTypeDescriptor> mediaTypes = new ArrayList<>();
    this.delegates.forEach(delegate -> mediaTypes.addAll(delegate.getMediaTypes()));
    return mediaTypes;
  }

  @Override
  public JavaDoc getJavaDoc() {
    return doDelegation(Entity::getJavaDoc);
  }

  @Override
  public boolean isRequired() {
    return this.delegates.stream().anyMatch(Entity::isRequired);
  }
}
