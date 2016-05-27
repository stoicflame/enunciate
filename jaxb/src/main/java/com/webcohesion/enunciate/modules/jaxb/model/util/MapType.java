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

package com.webcohesion.enunciate.modules.jaxb.model.util;

import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterType;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.Types;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A decorated map type.
 *
 * @author Ryan Heaton
 */
public class MapType extends DecoratedDeclaredType {

  private TypeMirror keyType;
  private TypeMirror valueType;
  private DeclaredType originalType;

  private MapType(DeclaredType mapType, ProcessingEnvironment env) {
    super(mapType, env);
    this.originalType = mapType;
  }

  /**
   * Finds the map type for the specified type mirror, if it exists.
   *
   * @param typeMirror The type mirror.
   * @param context The context
   * @return The map type or null.
   */
  public static MapType findMapType(TypeMirror typeMirror, EnunciateJaxbContext context) {
    if (!(typeMirror instanceof DeclaredType)) {
      return null;
    }

    DeclaredType declaredType = (DeclaredType) typeMirror;
    TypeElement element = (TypeElement) declaredType.asElement();
    if (element == null) {
      return null;
    }
    else {
      DeclaredType declaredMapType = findMapTypeDeclaration(declaredType, context);
      if (declaredMapType == null) {
        return null;
      }

      MapType newMapType = new MapType(declaredType, context.getContext().getProcessingEnvironment());

      TypeMirror keyType = null;
      TypeMirror valueType = null;

      List<? extends TypeMirror> typeArgs = declaredMapType.getTypeArguments();
      if ((typeArgs != null) && (typeArgs.size() == 2)) {
        Iterator<? extends TypeMirror> argIt = typeArgs.iterator();
        keyType = argIt.next();
        valueType = argIt.next();
      }

      if ((keyType == null) || (valueType == null)) {
        TypeMirror objectType = TypeMirrorUtils.objectType(context.getContext().getProcessingEnvironment());
        keyType = objectType;
        valueType = objectType;
      }

      TypeMirror mapKeyType = findMapType(keyType, context);
      if (mapKeyType != null) {
        newMapType.keyType = mapKeyType;
      }
      else if (((DecoratedTypeMirror) keyType).isInterface()) {
        //JAXB can't handle interfaces; we'll just resolve to a generic object.
        newMapType.keyType = TypeMirrorUtils.objectType(context.getContext().getProcessingEnvironment());
      }
      else {
        newMapType.keyType = keyType;
      }

      TypeMirror mapValueType = findMapType(valueType, context);
      if (mapValueType != null) {
        newMapType.valueType = mapValueType;
      }
      else if (((DecoratedTypeMirror) valueType).isInterface()) {
        //JAXB can't handle interfaces; we'll just resolve to a generic object.
        newMapType.valueType = TypeMirrorUtils.objectType(context.getContext().getProcessingEnvironment());
      }
      else {
        newMapType.valueType = valueType;
      }

      return newMapType;
    }
  }

  public static DeclaredType findMapTypeDeclaration(TypeMirror typeMirror, EnunciateJaxbContext context) {
    if (!(typeMirror instanceof DeclaredType)) {
      return null;
    }

    DeclaredType declaredType = (DeclaredType) typeMirror;
    TypeElement element = (TypeElement) declaredType.asElement();
    String fqn = element.getQualifiedName().toString();
    if (Map.class.getName().equals(fqn)) {
      return declaredType;
    }

    AdapterType adapterType = JAXBUtil.findAdapterType(element, context);
    if (adapterType != null) {
      return findMapTypeDeclaration(adapterType.getAdaptingType(), context);
    }

    DeclaredType mapType = null;
    Types typeUtils = context.getContext().getProcessingEnvironment().getTypeUtils();
    List<? extends TypeMirror> supers = typeUtils.directSupertypes(declaredType);
    for (TypeMirror superInterface : supers) {
      mapType = findMapTypeDeclaration(superInterface, context);
      if (mapType != null) {
        break;
      }
    }

    return mapType;
  }

  /**
   * The key type associated with this map type.
   *
   * @return The key type associated with this map type.
   */
  public TypeMirror getKeyType() {
    return keyType;
  }

  /**
   * The key type associated with this map type.
   *
   * @return The key type associated with this map type.
   */
  public TypeMirror getValueType() {
    return valueType;
  }

  /**
   * The original map type.
   *
   * @return The original map type.
   */
  public DeclaredType getOriginalType() {
    return originalType;
  }

  public boolean isMap() {
    return true;
  }

  @Override
  public TypeKind getKind() {
    return this.originalType.getKind();
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return this.originalType.accept(v, p);
  }
}
