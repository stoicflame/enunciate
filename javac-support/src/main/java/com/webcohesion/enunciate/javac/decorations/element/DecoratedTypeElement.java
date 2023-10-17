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

  private PackageElement pckg;
  private List<PropertyElement> properties;
  private TypeMirror superclass;
  private List<? extends TypeMirror> interfaces;
  private List<ExecutableElement> methods;
  private List<ExecutableElement> constructors;
  private List<VariableElement> enumConstants;

  public DecoratedTypeElement(TypeElement delegate, DecoratedProcessingEnvironment env) {
    super(delegate, env);
  }

  public PackageElement getPackage() {
    if (this.pckg == null) {
      this.pckg = ElementDecorator.decorate(this.env.getElementUtils().getPackageOf(this.delegate), this.env);
    }

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
    if (this.superclass == null) {
      this.superclass = TypeMirrorDecorator.decorate(this.delegate.getSuperclass(), env);
    }

    return this.superclass;
  }

  @Override
  public List<? extends TypeMirror> getInterfaces() {
    if (this.interfaces == null) {
      this.interfaces = TypeMirrorDecorator.decorate(this.delegate.getInterfaces(), env);
    }

    return this.interfaces;
  }

  @Override
  public List<? extends RecordComponentElement> getRecordComponents() {
    return this.delegate.getRecordComponents();
  }

  public List<ExecutableElement> getMethods() {
    if (this.methods == null) {
      this.methods = ElementDecorator.decorate(ElementFilter.methodsIn(this.delegate.getEnclosedElements()), this.env);
    }

    return this.methods;
  }

  public List<? extends VariableElement> getFields() {
    List<VariableElement> fields = new ArrayList<VariableElement>();
    List<VariableElement> allFields = ElementFilter.fieldsIn(this.delegate.getEnclosedElements());
    for (VariableElement field : allFields) {
      if (field.getKind() == ElementKind.FIELD && !(field.getModifiers().contains(Modifier.STATIC))) {
        fields.add(field);
      }
    }

    return fields;
  }

  public List<ExecutableElement> getConstructors() {
    if (this.constructors == null) {
      this.constructors = ElementDecorator.decorate(ElementFilter.constructorsIn(this.delegate.getEnclosedElements()), this.env);
    }

    return constructors;
  }

  public List<PropertyElement> getProperties() {
    return getProperties(new ElementUtils.DefaultPropertySpec(this.env));
  }

  public List<PropertyElement> getProperties(PropertySpec spec) {
    if (this.properties == null) {
      this.properties = loadProperties(spec);
    }

    return this.properties;
  }

  public List<VariableElement> enumValues() {
    if (this.enumConstants == null) {
      this.enumConstants = loadEnumConstants();
    }

    return enumConstants;
  }
  
  

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    A annotation = super.getAnnotation(annotationType);

    if (ElementUtils.isClassOrRecord(this) && annotation == null && (annotationType.getAnnotation(Inherited.class) != null) && (getSuperclass() instanceof DeclaredType)) {
      TypeElement superDecl = (TypeElement) ((DeclaredType) getSuperclass()).asElement();
      if ((superDecl != null) && (!Object.class.getName().equals(superDecl.getQualifiedName().toString()))) {
        return superDecl.getAnnotation(annotationType);
      }
    }

    return annotation;
  }
  
  protected List<PropertyElement> loadProperties(PropertySpec spec) {
    HashMap<String, DecoratedExecutableElement> getters = new HashMap<String, DecoratedExecutableElement>();
    HashMap<String, DecoratedExecutableElement> setters = new HashMap<String, DecoratedExecutableElement>();
    for (ExecutableElement method : getMethods()) {
      DecoratedExecutableElement decoratedMethod = (DecoratedExecutableElement) method;
      boolean getter = spec.isGetter(decoratedMethod);
      boolean setter = spec.isSetter(decoratedMethod);
      if (getter || setter) {
        HashMap<String, DecoratedExecutableElement> methodMap = getter ? getters : setters;
        methodMap.put(decoratedMethod.getPropertyName(), decoratedMethod);
      }
    }

    ArrayList<PropertyElement> properties = new ArrayList<PropertyElement>(getters.size());
    //now iterate through the getters and setters and pair them up....
    for (String propertyName : getters.keySet()) {
      DecoratedExecutableElement getter = getters.get(propertyName);
      DecoratedExecutableElement setter = setters.remove(propertyName);

      if (spec.isPaired(getter, setter)) {
        properties.add(new PropertyElement(getter, setter, spec, this.env));
      }
      else {
        properties.add(new PropertyElement(getter, null, spec, this.env));
      }
    }

    for (DecoratedExecutableElement setter : setters.values()) {
      properties.add(new PropertyElement(null, setter, spec, this.env));
    }

    List<? extends RecordComponentElement> recordComponents = getRecordComponents();
    for (RecordComponentElement recordComponent : recordComponents) {
      properties.add(new PropertyElement(recordComponent, env));
    }

    return properties;
  }

  protected List<VariableElement> loadEnumConstants() {
    ArrayList<VariableElement> constants = new ArrayList<VariableElement>();
    if (isEnum()) {
      List<VariableElement> fields = ElementFilter.fieldsIn(this.delegate.getEnclosedElements());
      for (VariableElement field : fields) {
        if (field.getKind() == ElementKind.ENUM_CONSTANT) {
          constants.add(ElementDecorator.decorate((field), this.env));
        }
      }
    }
    return constants;
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

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitType(this, p);
  }

}
