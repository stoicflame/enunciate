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

package com.webcohesion.enunciate.modules.jackson.model.util;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;
import com.webcohesion.enunciate.modules.jackson.model.Accessor;
import com.webcohesion.enunciate.modules.jackson.model.adapters.AdapterType;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Consolidation of common logic for implementing the Jackson contract.
 * 
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class JacksonUtil {

  private JacksonUtil() {}

  public static DecoratedTypeMirror getComponentType(DecoratedTypeMirror typeMirror, DecoratedProcessingEnvironment env) {
    if (typeMirror.isCollection()) {
      List<? extends TypeMirror> itemTypes = ((DeclaredType) typeMirror).getTypeArguments();
      if (itemTypes.isEmpty()) {
        return TypeMirrorUtils.objectType(env);
      }
      else {
        return (DecoratedTypeMirror) itemTypes.get(0);
      }
    }
    else if (typeMirror instanceof ArrayType) {
      return (DecoratedTypeMirror) ((ArrayType) typeMirror).getComponentType();
    }

    return null;
  }

  public static DecoratedDeclaredType getNormalizedCollection(DecoratedTypeMirror typeMirror, DecoratedProcessingEnvironment env) {
    DecoratedDeclaredType base = typeMirror.isList() ? TypeMirrorUtils.listType(env) : typeMirror.isCollection() ? TypeMirrorUtils.collectionType(env) : null;

    if (base != null) {
      //now narrow the component type to what can be valid json.
      List<? extends DecoratedTypeMirror> typeArgs = (List<? extends DecoratedTypeMirror>) ((DeclaredType)typeMirror).getTypeArguments();
      if (typeArgs.size() == 1) {
        DecoratedTypeMirror componentType = typeArgs.get(0);
        base = (DecoratedDeclaredType) env.getTypeUtils().getDeclaredType((TypeElement) base.asElement(), componentType);
      }
    }

    return base;
  }

  /**
   * Finds the adapter type for the specified declaration, if any.
   *
   * @param declaration The declaration for which to find that adapter type.
   * @param context The context.
   * @return The adapter type, or null if none was specified.
   */
  public static AdapterType findAdapterType(Element declaration, EnunciateJacksonContext context) {
    if (declaration instanceof Accessor) {
      //jaxb accessor can be adapted.
      Accessor accessor = ((Accessor) declaration);
      return findAdapterType(accessor.getAccessorType(), accessor, context);
    }
    else if (declaration instanceof ExecutableElement) {
      //assume the return type of the method is adaptable (e.g. web results, fault bean getters).
      ExecutableElement method = ((ExecutableElement) declaration);
      return findAdapterType((DecoratedTypeMirror) method.getReturnType(), method, context);
    }
    else if (declaration instanceof TypeElement) {
      return findAdapterType((DecoratedDeclaredType) declaration.asType(), null, context);
    }
    else {
      throw new IllegalArgumentException("A " + declaration.getClass().getSimpleName() + " is not an adaptable declaration according to the JAXB spec.");
    }
  }

  private static AdapterType findAdapterType(DecoratedTypeMirror maybeContainedAdaptedType, Element referer, EnunciateJacksonContext context) {
    DecoratedProcessingEnvironment env = context.getContext().getProcessingEnvironment();
    TypeMirror adaptedType = getComponentType(maybeContainedAdaptedType, env);
    boolean isContained = adaptedType != null;
    adaptedType = isContained ? adaptedType : maybeContainedAdaptedType;
    JsonSerialize serializationInfo = referer != null ? referer.getAnnotation(JsonSerialize.class) : null;
    if (serializationInfo == null && adaptedType instanceof DeclaredType) {
      serializationInfo = ((DeclaredType) adaptedType).asElement().getAnnotation(JsonSerialize.class);
    }

    if (serializationInfo != null) {
      DeclaredType adapterTypeMirror;

      try {
        Class adaptedClass = isContained ? serializationInfo.contentConverter() : serializationInfo.converter();
        adapterTypeMirror = env.getTypeUtils().getDeclaredType(env.getElementUtils().getTypeElement(adaptedClass.getCanonicalName()));
      }
      catch (MirroredTypeException e) {
        adapterTypeMirror = (DeclaredType) TypeMirrorDecorator.decorate(e.getTypeMirror(), env);
      }

      AdapterType adapterType = new AdapterType(adapterTypeMirror, context.getContext());
      if ((adaptedType instanceof DeclaredType && adapterType.canAdapt(adaptedType, context.getContext())) ||
        (maybeContainedAdaptedType != adaptedType && adapterType.canAdapt(maybeContainedAdaptedType, context.getContext()))) {
        return adapterType;
      }

      throw new EnunciateException(referer + ": adapter " + adapterTypeMirror + " does not adapt " + maybeContainedAdaptedType);
    }

    if (context.isHonorJaxb()) {
      XmlJavaTypeAdapter typeAdapterInfo = referer != null ? referer.getAnnotation(XmlJavaTypeAdapter.class) : null;
      if (adaptedType instanceof DeclaredType) {
        if (typeAdapterInfo == null) {
          typeAdapterInfo = ((DeclaredType) adaptedType).asElement().getAnnotation(XmlJavaTypeAdapter.class);
        }
      }

      if (typeAdapterInfo != null) {
        final XmlJavaTypeAdapter finalInfo = typeAdapterInfo;
        DecoratedDeclaredType adapterTypeMirror = (DecoratedDeclaredType) Annotations.mirrorOf(new Callable<Class<?>>() {
          @Override
          public Class<?> call() throws Exception {
            return finalInfo.value();
          }
        }, env);

        AdapterType adapterType = new AdapterType(adapterTypeMirror, context.getContext());
        if ((adaptedType instanceof DeclaredType && adapterType.canAdapt(adaptedType, context.getContext())) ||
          (maybeContainedAdaptedType != adaptedType && adapterType.canAdapt(maybeContainedAdaptedType, context.getContext()))) {
          return adapterType;
        }

        throw new EnunciateException(referer + ": adapter " + adapterTypeMirror + " does not adapt " + maybeContainedAdaptedType);
      }
    }

    return null;

  }
}
