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

import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A decorated type declaration provides:
 * <p/>
 * <ul>
 * <li>boolean properties for the "type" of type declaration.
 * </ul>
 *
 * @author Ryan Heaton
 */
public class DecoratedTypeElement extends DecoratedElement<TypeElement> implements TypeElement {

  protected final PackageElement pckg;
  protected final List<PropertyElement> properties;
  protected final TypeMirror superclass;
  protected final List<? extends TypeMirror> interfaces;
  protected final List<ExecutableElement> methods;
  protected final List<ExecutableElement> constructors;
  protected final List<VariableElement> enumConstants;

  public DecoratedTypeElement(TypeElement delegate, ProcessingEnvironment env) {
    super(delegate, env);
    this.pckg = ElementDecorator.decorate(this.env.getElementUtils().getPackageOf(this.delegate), this.env);
    this.properties = loadProperties();
    this.superclass = TypeMirrorDecorator.decorate(this.delegate.getSuperclass(), env);
    this.interfaces = TypeMirrorDecorator.decorate(this.delegate.getInterfaces(), env);
    this.methods = ElementDecorator.decorate(ElementFilter.methodsIn(this.env.getElementUtils().getAllMembers(this.delegate)), this.env);
    this.constructors = ElementDecorator.decorate(ElementFilter.constructorsIn(this.env.getElementUtils().getAllMembers(this.delegate)), this.env);
    this.enumConstants = loadEnumConstants();
  }

  public PackageElement getPackage() {
    return this.pckg;
  }

  public List<? extends TypeParameterElement> getTypeParameters() {
    return ElementDecorator.decorate(this.delegate.getTypeParameters(), this.env);
  }

  @Override
  public NestingKind getNestingKind() {
    return this.delegate.getNestingKind();
  }

  @Override
  public Name getQualifiedName() {
    return this.delegate.getQualifiedName();
  }

  @Override
  public TypeMirror getSuperclass() {
    return this.superclass;
  }

  @Override
  public List<? extends TypeMirror> getInterfaces() {
    return this.interfaces;
  }

  public List<? extends ExecutableElement> getMethods() {
    return this.methods;
  }

  public List<ExecutableElement> getConstructors() {
    return constructors;
  }

  public List<PropertyElement> getProperties() {
    return this.properties;
  }

  public List<VariableElement> getEnumConstants() {
    return enumConstants;
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    A annotation = super.getAnnotation(annotationType);

    if (isClass() && (annotation == null) && (annotationType.getAnnotation(Inherited.class) != null) && (getSuperclass() != null)) {
      TypeElement superDecl = (TypeElement) ((DeclaredType) getSuperclass()).asElement();
      if ((superDecl != null) && (!Object.class.getName().equals(superDecl.getQualifiedName().toString()))) {
        return superDecl.getAnnotation(annotationType);
      }
    }

    return annotation;
  }

  protected List<PropertyElement> loadProperties() {
    HashMap<String, DecoratedExecutableElement> getters = new HashMap<String, DecoratedExecutableElement>();
    HashMap<String, DecoratedExecutableElement> setters = new HashMap<String, DecoratedExecutableElement>();
    for (ExecutableElement method : getMethods()) {
      DecoratedExecutableElement decoratedMethod = (DecoratedExecutableElement) method;
      if (decoratedMethod.isPublic()) {
        if (decoratedMethod.isGetter() || decoratedMethod.isSetter()) {
          HashMap<String, DecoratedExecutableElement> methodMap = decoratedMethod.isGetter() ? getters : setters;
          methodMap.put(decoratedMethod.getPropertyName(), decoratedMethod);
        }
      }
    }

    ArrayList<PropertyElement> properties = new ArrayList<PropertyElement>(getters.size());
    //now iterate through the getters and setters and pair them up....
    for (String propertyName : getters.keySet()) {
      DecoratedExecutableElement getter = getters.get(propertyName);
      DecoratedExecutableElement setter = setters.remove(propertyName);
      if (isPaired(getter, setter)) {
        properties.add(new PropertyElement(getter, setter, this.env));
      }
    }

    for (DecoratedExecutableElement setter : setters.values()) {
      properties.add(new PropertyElement(null, setter, this.env));
    }

    return properties;
  }

  protected List<VariableElement> loadEnumConstants() {
    ArrayList<VariableElement> constants = new ArrayList<VariableElement>();
    if (isEnum()) {
      List<VariableElement> fields = ElementFilter.fieldsIn(this.env.getElementUtils().getAllMembers(this.delegate));
      for (VariableElement field : fields) {
        if (field.getKind() == ElementKind.ENUM_CONSTANT) {
          constants.add(field);
        }
      }
    }
    return constants;
  }

  /**
   * Whether a specified getter and setter are paired.
   *
   * @param getter The getter.
   * @param setter The setter.
   * @return Whether a specified getter and setter are paired.
   */
  protected boolean isPaired(DecoratedExecutableElement getter, DecoratedExecutableElement setter) {
    if (getter == null) {
      return false;
    }

    if (!getter.isGetter()) {
      return false;
    }

    if (getter.getParameters().size() != 0) {
      return false;
    }

    if (setter != null) {
      if (!setter.isSetter()) {
        return false;
      }

      if (!getter.getPropertyName().equals(setter.getPropertyName())) {
        return false;
      }

      List<? extends VariableElement> setterParams = setter.getParameters();
      if ((setterParams == null) || (setterParams.size() != 1) || (!this.env.getTypeUtils().isSameType(getter.getReturnType(), setterParams.iterator().next().asType()))) {
        return false;
      }
    }

    return true;
  }

  public boolean isClass() {
    return getKind() == ElementKind.CLASS;
  }

  public boolean isInterface() {
    return getKind() == ElementKind.INTERFACE;
  }

  public boolean isEnum() {
    return getKind() == ElementKind.ENUM;
  }

  public boolean isAnnotatedType() {
    return getKind() == ElementKind.ANNOTATION_TYPE;
  }

}
