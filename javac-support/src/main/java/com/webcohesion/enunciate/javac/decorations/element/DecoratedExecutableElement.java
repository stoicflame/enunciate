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
import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedReferenceType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.javadoc.ParamDocComment;
import com.webcohesion.enunciate.javac.javadoc.ReturnDocComment;
import com.webcohesion.enunciate.javac.javadoc.ThrowsDocComment;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.beans.Introspector;
import java.util.List;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class DecoratedExecutableElement extends DecoratedElement<ExecutableElement> implements ExecutableElement {

  private List<? extends VariableElement> parameters;
  private List<? extends TypeMirror> thrownTypes;
  private List<? extends TypeParameterElement> typeParameters;
  private TypeMirror typeMirror;

  public DecoratedExecutableElement(ExecutableElement delegate, DecoratedProcessingEnvironment env) {
    super(delegate, env);
  }

  protected DecoratedExecutableElement(DecoratedExecutableElement copy) {
    super(copy.delegate, copy.env);
    this.parameters = copy.parameters;
    this.thrownTypes = copy.thrownTypes;
    this.typeParameters = copy.typeParameters;
    this.typeMirror = copy.typeMirror;
  }

  private List<? extends TypeMirror> loadDecoratedThrownTypes(ExecutableElement delegate) {
    List<? extends TypeMirror> thrownTypes = TypeMirrorDecorator.decorate(delegate.getThrownTypes(), env);

    if (thrownTypes != null && !thrownTypes.isEmpty()) {
      for (TypeMirror thrownType : thrownTypes) {
        ((DecoratedReferenceType)thrownType).setDocComment(new ThrowsDocComment(this, String.valueOf(thrownType)));
      }
    }

    return thrownTypes;
  }

  private List<? extends VariableElement> loadDecoratedParameters() {
    List<? extends VariableElement> parameters = ElementDecorator.decorate(((ExecutableElement) this.delegate).getParameters(), this.env);
    if (parameters != null) {
      for (VariableElement param : parameters) {
        ((DecoratedVariableElement) param).setDocComment(createParamDocComment(param));
      }
    }
    return parameters;
  }

  protected ParamDocComment createParamDocComment(VariableElement param) {
    return new ParamDocComment(this, param.getSimpleName().toString());
  }

  @Override
  public List<? extends TypeParameterElement> getTypeParameters() {
    if (this.typeParameters == null) {
      this.typeParameters = ElementDecorator.decorate(delegate.getTypeParameters(), env);
    }

    return this.typeParameters;
  }

  @Override
  public TypeMirror getReturnType() {
    if (this.typeMirror == null) {
      this.typeMirror = TypeMirrorDecorator.decorate(delegate.getReturnType(), env);
      ((DecoratedTypeMirror)this.typeMirror).setDocComment(new ReturnDocComment(this));
    }
    
    return this.typeMirror;
  }

  @Override
  public boolean isVarArgs() {
    return this.delegate.isVarArgs();
  }

  @Override
  public AnnotationValue getDefaultValue() {
    return this.delegate.getDefaultValue();
  }

  public List<? extends VariableElement> getParameters() {
    if (this.parameters == null) {
      this.parameters = loadDecoratedParameters();
    }

    return this.parameters;
  }

  public List<? extends TypeMirror> getThrownTypes() {
    if (this.thrownTypes == null) {
      this.thrownTypes = loadDecoratedThrownTypes(delegate);
    }

    return this.thrownTypes;
  }

  public boolean isGetter() {
    return ((getSimpleName().toString().startsWith("get") || isIs()) && getParameters().isEmpty());
  }

  private boolean isIs() {
    return getSimpleName().toString().startsWith("is") && (getReturnType().getKind() == TypeKind.BOOLEAN || ((DecoratedTypeMirror)getReturnType()).isInstanceOf(Boolean.class));
  }

  public boolean isSetter() {
    return (getSimpleName().toString().startsWith("set") && getParameters().size() == 1);
  }

  public String getPropertyName() {
    String propertyName = null;

    if (isIs()) {
      propertyName = Introspector.decapitalize(getSimpleName().toString().substring(2));
    }
    else if (isGetter() || (isSetter())) {
      propertyName = Introspector.decapitalize(getSimpleName().toString().substring(3));
    }

    return propertyName;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitExecutable(this, p);
  }
}
