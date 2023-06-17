package com.webcohesion.enunciate.modules.jackson.javac;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.SourcePosition;
import com.webcohesion.enunciate.javac.decorations.adaptors.ExecutableElementAdaptor;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ToStringValueProperty implements ExecutableElementAdaptor {

  private final ExecutableElement toStringMethod;
  private final DecoratedProcessingEnvironment env;
  private final Name simpleName;

  public ToStringValueProperty(ExecutableElement toStringMethod, DecoratedProcessingEnvironment env) {
    this.toStringMethod = toStringMethod;
    this.env = env;
    this.simpleName = this.env.getElementUtils().getName("stringValue");
  }

  @Override
  public List<? extends TypeParameterElement> getTypeParameters() {
    return Collections.emptyList();
  }

  @Override
  public TypeMirror getReturnType() {
    return this.toStringMethod.asType();
  }

  @Override
  public List<? extends VariableElement> getParameters() {
    return Collections.emptyList();
  }

  @Override
  public boolean isVarArgs() {
    return false;
  }

  @Override
  public List<? extends TypeMirror> getThrownTypes() {
    return Collections.emptyList();
  }

  @Override
  public AnnotationValue getDefaultValue() {
    return null;
  }

  @Override
  public Name getSimpleName() {
    return simpleName;
  }

  @Override
  public TypeMirror asType() {
    return TypeMirrorUtils.mirrorOf(String.class.getName(), this.env);
  }

  @Override
  public ElementKind getKind() {
    return ElementKind.METHOD;
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    return this.toStringMethod.getAnnotationMirrors();
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.toStringMethod.getAnnotation(annotationType);
  }

  @Override
  public Set<Modifier> getModifiers() {
    return EnumSet.of(Modifier.PUBLIC);
  }

  @Override
  public Element getEnclosingElement() {
    return this.toStringMethod.getEnclosingElement();
  }

  @Override
  public List<? extends Element> getEnclosedElements() {
    return Collections.emptyList();
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitExecutable(this, p);
  }

  @Override
  public boolean overrides(ExecutableElement overridden, TypeElement scope) {
    return (overridden instanceof ToStringValueProperty) && (this.env.getElementUtils().hides(this.toStringMethod, ((ToStringValueProperty) overridden).toStringMethod));
  }

  @Override
  public String getDocComment() {
    String docComment = this.env.getElementUtils().getDocComment(this.toStringMethod);
    if (docComment != null && !docComment.trim().isEmpty()) {
      return docComment + "\n@return " + this.toStringMethod.getSimpleName().toString() + ' ' + docComment;
    }
    return null;
  }

  @Override
  public boolean isDeprecated() {
    return this.env.getElementUtils().isDeprecated(this.toStringMethod);
  }

  @Override
  public boolean isOverriddenBy(ExecutableElement overrider, TypeElement type) {
    return (overrider instanceof ToStringValueProperty) && ((ToStringValueProperty) overrider).overrides(this, type);
  }

  @Override
  public PackageElement getPackage() {
    return this.env.getElementUtils().getPackageOf(this.toStringMethod);
  }

  @Override
  public List<? extends AnnotationMirror> getAllAnnotationMirrors() {
    return this.env.getElementUtils().getAllAnnotationMirrors(this.toStringMethod);
  }

  @Override
  public boolean hides(Element hidden) {
    return false;
  }

  @Override
  public boolean isHiddenBy(Element hider) {
    return false;
  }

  @Override
  public SourcePosition getSourcePosition() {
    return this.env.findSourcePosition(this.toStringMethod);
  }

  @Override
  public TypeMirror getReceiverType() {
    return env.getTypeUtils().getNoType(TypeKind.NONE);
  }

  @Override
  public boolean isDefault() {
    return false;
  }

  @Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    return this.toStringMethod.getAnnotationsByType(annotationType);
  }

}
