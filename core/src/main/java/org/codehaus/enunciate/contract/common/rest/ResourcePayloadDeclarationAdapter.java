/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.contract.common.rest;

import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.SourcePosition;
import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.decorations.declaration.DecoratedDeclaration;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class ResourcePayloadDeclarationAdapter extends DecoratedDeclaration implements RESTResourcePayload {

  private final DecoratedDeclaration delegate;

  public ResourcePayloadDeclarationAdapter(DecoratedDeclaration delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  public boolean isPublic() {
    return delegate.isPublic();
  }

  public boolean isProtected() {
    return delegate.isProtected();
  }

  public boolean isPrivate() {
    return delegate.isPrivate();
  }

  public boolean isAbstract() {
    return delegate.isAbstract();
  }

  public boolean isStatic() {
    return delegate.isStatic();
  }

  public boolean isFinal() {
    return delegate.isFinal();
  }

  public boolean isTransient() {
    return delegate.isTransient();
  }

  public boolean isVolatile() {
    return delegate.isVolatile();
  }

  public boolean isSynchronized() {
    return delegate.isSynchronized();
  }

  public boolean isNative() {
    return delegate.isNative();
  }

  public boolean isStrictfp() {
    return delegate.isStrictfp();
  }

  public JavaDoc getJavaDoc() {
    return delegate.getJavaDoc();
  }

  public String getDocValue() {
    return delegate.getDocValue();
  }

  public Map<String, AnnotationMirror> getAnnotations() {
    return delegate.getAnnotations();
  }

  public String getDocComment() {
    return delegate.getDocComment();
  }

  public Collection<AnnotationMirror> getAnnotationMirrors() {
    return delegate.getAnnotationMirrors();
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return delegate.getAnnotation(annotationType);
  }

  public Collection<Modifier> getModifiers() {
    return delegate.getModifiers();
  }

  public String getSimpleName() {
    return delegate.getSimpleName();
  }

  public SourcePosition getPosition() {
    return delegate.getPosition();
  }

  public void accept(DeclarationVisitor v) {
    delegate.accept(v);
  }

  public Declaration getDelegate() {
    return delegate.getDelegate();
  }

  public boolean equals(Object obj) {
    return delegate.equals(obj);
  }

  public String toString() {
    return delegate.toString();
  }
}
