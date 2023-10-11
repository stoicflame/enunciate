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

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A property, representing the getter/setter pair.  In all cases, the description of the property matches the description of the
 * getter, but the annotations are the union of the getter and the setter, with the intersection preferring the getter.
 *
 * @author Ryan Heaton
 */
public class PropertyElement extends DecoratedExecutableElement {

  private final DecoratedExecutableElement setter;
  private final DecoratedExecutableElement getter;
  private final String propertyName;
  private final String simpleName;
  private final TypeMirror propertyType;
  private final boolean readOnly;
  private final boolean writeOnly;
  private final boolean recordComponent;

  /**
   * A property declaration.
   *
   * @param getter The getter.
   * @param setter The setter.
   * @throws IllegalStateException If the getter and setter don't pair up.
   */
  public PropertyElement(DecoratedExecutableElement getter, DecoratedExecutableElement setter, DecoratedProcessingEnvironment env) {
    this(getter, setter, new ElementUtils.DefaultPropertySpec(env), env);
  }
  
  public PropertyElement(RecordComponentElement component, DecoratedProcessingEnvironment env) {
    super(ElementDecorator.decorate(component.getAccessor(), env), env);
    this.getter = (DecoratedExecutableElement) ElementDecorator.decorate(component.getAccessor(), env);
    this.setter = null;
    this.readOnly = false;
    this.writeOnly = false;
    this.simpleName = component.getSimpleName().toString();
    this.propertyName = this.simpleName;
    this.propertyType = this.getter.getReturnType();
    this.recordComponent = true;
  }

  public PropertyElement(DecoratedExecutableElement getter, DecoratedExecutableElement setter, PropertySpec spec, DecoratedProcessingEnvironment env) {
    super(getter == null ? setter : getter);
    this.getter = getter;
    this.setter = setter;
    this.propertyName = spec.getPropertyName(getter != null ? getter : setter);
    this.simpleName = spec.getSimpleName(getter != null ? getter : setter);
    this.recordComponent = false;

    TypeMirror propertyType = null;
    if (getter != null) {
      propertyType = getter.getReturnType();
    }

    if (setter != null) {
      List<? extends VariableElement> parameters = setter.getParameters();
      if ((parameters == null) || (parameters.size() != 1)) {
        throw new IllegalStateException(this.setter + ": invalid setter for " + propertyType);
      }
      else {
        TypeMirror setterType = parameters.iterator().next().asType();
        if (propertyType == null) {
          propertyType = setterType;
        }
        else if (!env.getTypeUtils().isSameType(propertyType, setterType)) {
          throw new IllegalStateException(this.setter + ": invalid setter for getter of type " + propertyType);
        }
      }
    }

    if (propertyType == null) {
      throw new IllegalStateException("Unable to determine property type for property" + this.propertyName + ".");
    }

    this.propertyType = propertyType;
    this.readOnly = this.setter == null;
    this.writeOnly = this.getter == null;
  }

  @Override
  public TypeMirror asType() {
    return getPropertyType();
  }

  /**
   * The type of this property.
   *
   * @return The type of this property.
   */
  public TypeMirror getPropertyType() {
    return this.propertyType;
  }

  /**
   * The simple name of the property is the property name.
   *
   * @return The simple name of the property is the property name.
   */
  @Override
  public Name getSimpleName() {
    return this.env.getElementUtils().getName(this.simpleName);
  }

  /**
   * Make sure the property name is calculated correctly.
   */
  @Override
  public String getPropertyName() {
    return this.propertyName;
  }

  /**
   * The setter, or null if this property is a read-only property.
   *
   * @return The setter.
   */
  public DecoratedExecutableElement getSetter() {
    return setter;
  }

  /**
   * The getter.
   *
   * @return The getter.
   */
  public DecoratedExecutableElement getGetter() {
    return getter;
  }

  /**
   * Whether this property is defined by record component.
   * 
   * @return Whether this property is defined by record component.
   */
  public boolean isRecordComponent() {
    return recordComponent;
  }

  /**
   * Whether this property is read-only.
   *
   * @return Whether this property is read-only.
   */
  public boolean isReadOnly() {
    return this.readOnly;
  }

  /**
   * Whether this property is write-only.
   *
   * @return Whether this property is write-only.
   */
  public boolean isWriteOnly() {
    return this.writeOnly;
  }

  /**
   * Gets the annotations on the setter and the getter.  If the annotation is on both the setter and the getter, only the one on the getter will
   * be included.
   *
   * @return The union of the annotations on the getter and setter.
   */
  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    Map<String, AnnotationMirror> annotations = new HashMap<String, AnnotationMirror>();

    if (getGetter() != null) {
      annotations.putAll(getGetter().getAnnotations());
    }

    if (getSetter() != null) {
      annotations.putAll(getSetter().getAnnotations());
    }

    return annotations;
  }

  /**
   * Gets the collection of annotations on the setter and the getter.  If the annotation is on both the setter and the getter, only the one on the getter will
   * be included.
   *
   * @return The union of the annotations on the getter and setter.
   */
  @Override
  public List<AnnotationMirror> getAnnotationMirrors() {
    return new ArrayList<AnnotationMirror>(getAnnotations().values());
  }

  /**
   * Gets the annotation on the getter.  If it doesn't exist, returns the one on the setter.
   *
   * @param annotationType The annotation type.
   * @return The annotation.
   */
  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    A annotation = null;

    if (this.getter != null) {
      annotation = this.getter.getAnnotation(annotationType);
    }

    if ((annotation == null) && (this.setter != null)) {
      annotation = this.setter.getAnnotation(annotationType);
    }

    return annotation;
  }

  @Override
  public TypeMirror getReturnType() {
    return getPropertyType();
  }

  @Override
  public boolean isGetter() {
    return false;
  }

  @Override
  public boolean isSetter() {
    return false;
  }

  @Override
  public boolean isVarArgs() {
    return false;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitExecutable(this, p);
  }
}
