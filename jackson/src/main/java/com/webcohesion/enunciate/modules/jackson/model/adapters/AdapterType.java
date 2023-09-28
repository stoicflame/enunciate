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
package com.webcohesion.enunciate.modules.jackson.model.adapters;

import com.fasterxml.jackson.databind.util.Converter;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class AdapterType extends DecoratedDeclaredType {

  private final TypeMirror adaptedType;
  private final TypeMirror adaptingType;

  public AdapterType(DeclaredType adapterType, EnunciateJacksonContext context) {
    super(adapterType, context.getContext().getProcessingEnvironment());

    DeclaredType adaptorInterfaceType = findJsonAdapterType(adapterType, new TypeVariableContext(), context.getContext().getProcessingEnvironment());
    if (adaptorInterfaceType != null) {
      List<? extends TypeMirror> adaptorTypeArgs = adaptorInterfaceType.getTypeArguments();
      if ((adaptorTypeArgs == null) || (adaptorTypeArgs.size() != 2)) {
        throw new EnunciateException(adapterType + " must specify both a value type and a bound type.");
      }

      this.adaptingType = adaptorTypeArgs.get(1);
      this.adaptedType = context.getContext().getProcessingEnvironment().getTypeUtils().erasure(adaptorTypeArgs.get(0));
    }
    else if (context.isHonorJaxb()) {
      adaptorInterfaceType = findXmlAdapterType(adapterType, new TypeVariableContext(), context.getContext().getProcessingEnvironment());

      if (adaptorInterfaceType == null) {
        throw new EnunciateException(adapterType + " is neither an instance of com.fasterxml.jackson.databind.util.Converter nor an instance of jakarta.xml.bind.annotation.adapters.XmlAdapter.");
      }

      List<? extends TypeMirror> adaptorTypeArgs = adaptorInterfaceType.getTypeArguments();
      if ((adaptorTypeArgs == null) || (adaptorTypeArgs.size() != 2)) {
        throw new EnunciateException(adapterType + " must specify both a value type and a bound type.");
      }

      this.adaptingType = adaptorTypeArgs.get(0);
      this.adaptedType = context.getContext().getProcessingEnvironment().getTypeUtils().erasure(adaptorTypeArgs.get(1));
    }
    else {
      throw new EnunciateException(adapterType + " is not an instance of com.fasterxml.jackson.databind.util.Converter.");
    }
  }

  /**
   * Finds the interface type that declares that the specified declaration implements Converter.
   *
   * @param declaredType The declaration.
   * @return The interface type, or null if none found.
   */
  private static DeclaredType findJsonAdapterType(DeclaredType declaredType, TypeVariableContext variableContext, DecoratedProcessingEnvironment env) {
    TypeElement element = (TypeElement) declaredType.asElement();
    if (element == null) {
      return null;
    }
    else if (Object.class.getName().equals(element.getQualifiedName().toString())) {
      return null;
    }
    else if (Converter.class.getName().equals(element.getQualifiedName().toString())) {
      return (DeclaredType) variableContext.resolveTypeVariables(declaredType, env);
    }
    else {
      DeclaredType superclass = (DeclaredType) element.getSuperclass();
      if (superclass == null || superclass.getKind() == TypeKind.NONE) {
        return null;
      }
      else {
        return findJsonAdapterType(superclass, variableContext.push(element.getTypeParameters(), declaredType.getTypeArguments()), env);
      }
    }
  }

  /**
   * Finds the interface type that declares that the specified declaration implements XmlAdapter.
   *
   * @param declaredType The declaration.
   * @return The interface type, or null if none found.
   */
  private static DeclaredType findXmlAdapterType(DeclaredType declaredType, TypeVariableContext variableContext, DecoratedProcessingEnvironment env) {
    TypeElement element = (TypeElement) declaredType.asElement();
    if (element == null) {
      return null;
    }
    else if (Object.class.getName().equals(element.getQualifiedName().toString())) {
      return null;
    }
    else if (XmlAdapter.class.getName().equals(element.getQualifiedName().toString())) {
      return (DeclaredType) variableContext.resolveTypeVariables(declaredType, env);
    }
    else {
      DeclaredType superclass = (DeclaredType) element.getSuperclass();
      if (superclass == null || superclass.getKind() == TypeKind.NONE) {
        return null;
      }
      else {
        return findXmlAdapterType(superclass, variableContext.push(element.getTypeParameters(), declaredType.getTypeArguments()), env);
      }
    }
  }

  /**
   * Whether this adapter can adapt the specified type.
   *
   * @param type The type.
   * @return Whether this adapter can adapt the specified type.
   */
  public boolean canAdapt(TypeMirror type, EnunciateContext context) {
    return context.getProcessingEnvironment().getTypeUtils().isAssignable(type, getAdaptedType());
  }

  /**
   * Get the adapting type for the specified type. This method differs from {@link #getAdaptingType()} because it takes
   * into account whether the adapted is an array or collection.
   *
   * @param adaptedType The type.
   * @return The adapting type, or null if not adaptable.
   */
  public TypeMirror getAdaptingType(DecoratedTypeMirror adaptedType, EnunciateContext context) {
    TypeMirror componentType = null;
    if (adaptedType.isCollection() || adaptedType.isStream()) {
      List<? extends TypeMirror> itemTypes = ((DeclaredType) adaptedType).getTypeArguments();
      if (itemTypes.isEmpty()) {
        componentType = TypeMirrorUtils.objectType(context.getProcessingEnvironment());
      }
      else {
        componentType = itemTypes.get(0);
      }
    }
    else if (adaptedType instanceof ArrayType) {
      componentType = ((ArrayType) adaptedType).getComponentType();
    }

    if (componentType != null && canAdapt(componentType, context)) {
      //if we can adapt the component type, then the adapting type is the collection of the declared adapting type.
      return context.getProcessingEnvironment().getTypeUtils().getDeclaredType((TypeElement) TypeMirrorUtils.collectionType(context.getProcessingEnvironment()).asElement(), getAdaptingType());
    }
    else {
      return getAdaptingType();
    }
  }

  /**
   * The type that is being adapted by this adapter.
   *
   * @return The type that is being adapted by this adapter.
   */
  public TypeMirror getAdaptedType() {
    return adaptedType;
  }

  /**
   * The type to which this adapter is adapting.
   *
   * @return The type to which this adapter is adapting.
   */
  public TypeMirror getAdaptingType() {
    return adaptingType;
  }
}
