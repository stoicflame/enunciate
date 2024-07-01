/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.javac.decorations.element;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.ElementDecoration;
import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.javadoc.DefaultJavaDocTagHandler;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unchecked")
public class DecoratedElement<E extends Element> implements Element {

  protected final E delegate;
  protected final DecoratedProcessingEnvironment env;
  private final HashMap<JavaDocTagHandler, JavaDoc> javaDocs = new HashMap<>();
  private TypeMirror type;
  private Element enclosingElement;
  private List<? extends Element> enclosedElements;
  protected List<AnnotationMirror> annotationMirrors;
  private Map<String, AnnotationMirror> annotations = null;

  public DecoratedElement(E delegate, DecoratedProcessingEnvironment env) {
    this.delegate = delegate;
    this.env = env;

    if (this.env.getElementDecorations() != null) {
      for (ElementDecoration elementDecoration : this.env.getElementDecorations()) {
        elementDecoration.applyTo(this, this.env);
      }
    }
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

  public String getDocComment() {
    return this.env.getElementUtils().getDocComment(this.delegate);
  }

  /**
   * The javadoc for this declaration with the default tag handler.
   *
   * @return The javadoc for this declaration with the default tag handler.
   */
  public final JavaDoc getJavaDoc() {
    return getJavaDoc(DefaultJavaDocTagHandler.INSTANCE);
  }

  /**
   * Get the JavaDoc for this element for the given tag handler.
   *
   * @param tagHandler The tag handler.
   * @return The javadoc.
   */
  public final JavaDoc getJavaDoc(JavaDocTagHandler tagHandler) {
    return getJavaDoc(tagHandler, true);
  }

  protected JavaDoc getJavaDoc(JavaDocTagHandler tagHandler, boolean useDelegate) {
    if (useDelegate && this.delegate instanceof DecoratedElement) {
      //if the delegate is decorated, we assume that the extension
      //intends the delegate to be the comment-holder. This is
      //important in order to correctly calculate inherited javadocs.
      //However, it makes overriding tricky because if you override
      //'getDocComment', you have to also override this method lest
      //the JavaDoc be out of sync with the doc comment.
      return ((DecoratedElement) this.delegate).getJavaDoc(tagHandler);
    }

    JavaDoc javaDoc = this.javaDocs.get(tagHandler);
    if (javaDoc == null) {
      javaDoc = new JavaDoc(getDocComment(), tagHandler, this, this.env);
      this.javaDocs.put(tagHandler, javaDoc);
    }

    return javaDoc;
  }

  /**
   * The value of the java doc, before the block tags.
   *
   * @return The value of the java doc, before the block tags, or null if the value is the empty string.
   */
  public final String getDocValue() {
    return getDocValue(DefaultJavaDocTagHandler.INSTANCE);
  }

  /**
   * The value of the java doc, before the block tags.
   *
   * @param tagHandler The tag handler.
   * @return The value of the java doc, before the block tags, or null if the value is the empty string.
   */
  public final String getDocValue(JavaDocTagHandler tagHandler) {
    String value = getJavaDoc(tagHandler).toString();
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
      this.annotations = new HashMap<>();
      for (AnnotationMirror annotationMirror : getAnnotationMirrors()) {
        DeclaredType annotationType = annotationMirror.getAnnotationType();
        if ((annotationType != null) && (annotationType.asElement() instanceof TypeElement)) {
          annotations.put(((TypeElement)annotationType.asElement()).getQualifiedName().toString(), annotationMirror);
        }
      }
    }

    return this.annotations;
  }

  @Override
  public TypeMirror asType() {
    if (this.type == null) {
      this.type = TypeMirrorDecorator.decorate(delegate.asType(), env);
    }

    return this.type;
  }

  @Override
  public ElementKind getKind() {
    return this.delegate.getKind();
  }

  @Override
  public Element getEnclosingElement() {
    if (this.enclosingElement == null) {
      this.enclosingElement = ElementDecorator.decorate(delegate.getEnclosingElement(), env);
    }

    return this.enclosingElement;
  }

  @Override
  public List<? extends Element> getEnclosedElements() {
    if (this.enclosedElements == null) {
      this.enclosedElements = ElementDecorator.decorate(delegate.getEnclosedElements(), env);
    }

    return this.enclosedElements;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitUnknown(this, p);
  }

  //Inherited.
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    if (this.annotationMirrors == null) {
      this.annotationMirrors = ElementDecorator.decorateAnnotationMirrors(delegate.getAnnotationMirrors(), env);
    }

    return this.annotationMirrors;
  }

  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.delegate.getAnnotation(annotationType);
  }

  @Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    return this.delegate.getAnnotationsByType(annotationType);
  }

  //Inherited.
  public Set<Modifier> getModifiers() {
    return this.delegate.getModifiers();
  }

  //Inherited.
  public Name getSimpleName() {
    return this.delegate.getSimpleName();
  }

  public String getSimpleNameString() {
    return getSimpleName().toString();
  }

  public E getDelegate() {
    return this.delegate;
  }

  //Inherited.
  public boolean equals(Object obj) {
    if (obj instanceof DecoratedElement) {
      return equals(((DecoratedElement) obj).delegate);
    }

    Element delegate = this.delegate;
    while (delegate instanceof DecoratedElement) {
      delegate = ((DecoratedElement) delegate).delegate;
    }

    return delegate.equals(obj);
  }

  //Inherited.
  public String toString() {
    return this.delegate.toString();
  }

}
