/*
 * Copyright 2006 Ryan Heaton
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
package com.webcohesion.enunciate.javac.decorations.element;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandlerFactory;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.*;

@SuppressWarnings("unchecked")
public class DecoratedElement<E extends Element> implements Element {

  protected final E delegate;
  protected final ProcessingEnvironment env;
  protected final JavaDoc javaDoc;
  protected final TypeMirror type;
  protected final Element enclosingElement;
  protected final List<? extends Element> enclosedElements;
  protected final List<AnnotationMirror> annotationMirrors;
  private Map<String, AnnotationMirror> annotations = null;

  public DecoratedElement(E delegate, ProcessingEnvironment env) {
    while (delegate instanceof DecoratedElement) {
      delegate = (E) ((DecoratedElement) delegate).delegate;
    }

    if (!(env instanceof DecoratedProcessingEnvironment)) {
      env = new DecoratedProcessingEnvironment(env);
    }

    this.javaDoc = constructJavaDoc(env.getElementUtils().getDocComment(delegate), JavaDocTagHandlerFactory.getTagHandler());
    this.type = TypeMirrorDecorator.decorate(delegate.asType(), env);
    this.enclosingElement = ElementDecorator.decorate(delegate.getEnclosingElement(), env);
    this.enclosedElements = ElementDecorator.decorate(delegate.getEnclosedElements(), env);
    this.annotationMirrors = ElementDecorator.decorateAnnotationMirrors(delegate.getAnnotationMirrors(), env);
    this.delegate = delegate;
    this.env = env;
  }

  protected JavaDoc constructJavaDoc(String docComment, JavaDocTagHandler tagHandler) {
    return new JavaDoc(docComment, tagHandler);
  }

  /**
   * Whether the declaration is <code>public</code>.
   *
   * @return Whether the declaration is <code>public</code>.
   */
  public boolean isPublic() {
    return getModifiers().contains(javax.lang.model.element.Modifier.PUBLIC);
  }

  /**
   * Whether the declaration is <code>protected</code>.
   *
   * @return Whether the declaration is <code>protected</code>.
   */
  public boolean isProtected() {
    return getModifiers().contains(Modifier.PROTECTED);
  }

  /**
   * Whether the declaration is <code>private</code>.
   *
   * @return Whether the declaration is <code>private</code>.
   */
  public boolean isPrivate() {
    return getModifiers().contains(Modifier.PRIVATE);
  }

  /**
   * Whether the declaration is <code>abstract</code>.
   *
   * @return Whether the declaration is <code>abstract</code>.
   */
  public boolean isAbstract() {
    return getModifiers().contains(Modifier.ABSTRACT);
  }

  /**
   * Whether the declaration is <code>static</code>.
   *
   * @return Whether the declaration is <code>static</code>.
   */
  public boolean isStatic() {
    return getModifiers().contains(Modifier.STATIC);
  }

  /**
   * Whether the declaration is <code>final</code>.
   *
   * @return Whether the declaration is <code>final</code>.
   */
  public boolean isFinal() {
    return getModifiers().contains(Modifier.FINAL);
  }

  /**
   * Whether the declaration is <code>transient</code>.
   *
   * @return Whether the declaration is <code>transient</code>.
   */
  public boolean isTransient() {
    return getModifiers().contains(Modifier.TRANSIENT);
  }

  /**
   * Whether the declaration is <code>volatile</code>.
   *
   * @return Whether the declaration is <code>volatile</code>.
   */
  public boolean isVolatile() {
    return getModifiers().contains(Modifier.VOLATILE);
  }

  /**
   * Whether the declaration is <code>synchronized</code>.
   *
   * @return Whether the declaration is <code>synchronized</code>.
   */
  public boolean isSynchronized() {
    return getModifiers().contains(Modifier.SYNCHRONIZED);
  }

  /**
   * Whether the declaration is <code>native</code>.
   *
   * @return Whether the declaration is <code>native</code>.
   */
  public boolean isNative() {
    return getModifiers().contains(Modifier.NATIVE);
  }

  /**
   * Whether the declaration is <code>strictfp</code>.
   *
   * @return Whether the declaration is <code>strictfp</code>.
   */
  public boolean isStrictfp() {
    return getModifiers().contains(Modifier.STRICTFP);
  }

  /**
   * The javadoc for this declaration.
   *
   * @return The javadoc for this declaration.
   */
  public JavaDoc getJavaDoc() {
    return javaDoc;
  }

  /**
   * The value of the java doc, before the block tags.
   *
   * @return The value of the java doc, before the block tags, or null if the value is the empty string.
   */
  public String getDocValue() {
    String value = this.javaDoc.toString();
    if (value != null) {
      value = value.trim();

      if ("".equals(value)) {
        value = null;
      }
    }
    return value;
  }

  /**
   * A map of annotations for this declaration.
   *
   * @return A map of annotations for this declaration.
   */
  public Map<String, AnnotationMirror> getAnnotations() {
    if (this.annotations == null) {
      this.annotations = new HashMap<String, AnnotationMirror>();
      for (AnnotationMirror annotationMirror : getAnnotationMirrors()) {
        DeclaredType annotationType = annotationMirror.getAnnotationType();
        if ((annotationType != null) && (annotationType.asElement() instanceof TypeElement)) {
          annotations.put(((TypeElement)annotationType.asElement()).getQualifiedName().toString(), annotationMirror);
        }
      }
    }

    return this.annotations;
  }

  public String getDocComment() {
    return this.env.getElementUtils().getDocComment(this.delegate);
  }

  @Override
  public TypeMirror asType() {
    return this.type;
  }

  @Override
  public ElementKind getKind() {
    return this.delegate.getKind();
  }

  @Override
  public Element getEnclosingElement() {
    return this.enclosingElement;
  }

  @Override
  public List<? extends Element> getEnclosedElements() {
    return this.enclosedElements;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visit(this, p);
  }

  //Inherited.
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    return this.annotationMirrors;
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.delegate.getAnnotation(annotationType);
  }

  //Inherited.
  public Set<Modifier> getModifiers() {
    return this.delegate.getModifiers();
  }

  //Inherited.
  public Name getSimpleName() {
    return this.delegate.getSimpleName();
  }

  public E getDelegate() {
    return this.delegate;
  }

  //Inherited.
  public boolean equals(Object obj) {
    if (obj instanceof DecoratedElement) {
      return equals(((DecoratedElement) obj).delegate);
    }

    return this.delegate.equals(obj);
  }

  //Inherited.
  public String toString() {
    return this.delegate.toString();
  }

}
