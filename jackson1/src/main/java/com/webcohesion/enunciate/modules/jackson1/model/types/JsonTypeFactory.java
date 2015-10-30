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

package com.webcohesion.enunciate.modules.jackson1.model.types;

import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.modules.jackson1.EnunciateJackson1Context;
import com.webcohesion.enunciate.modules.jackson1.model.Accessor;
import com.webcohesion.enunciate.modules.jackson1.model.adapters.Adaptable;
import com.webcohesion.enunciate.modules.jackson1.model.util.MapType;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.lang.model.type.TypeMirror;
import java.util.concurrent.Callable;

/**
 * A decorator that decorates the relevant type mirrors as json type mirrors.
 *
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class JsonTypeFactory {

  /**
   * Find the specified type of the given adaptable element, if it exists.
   *
   * @param adaptable The adaptable element for which to find the specified type.
   * @param context The context
   * @return The specified JSON type, or null if it doesn't exist.
   */
  public static JsonType findSpecifiedType(Adaptable adaptable, EnunciateJackson1Context context) {
    JsonType jsonType = null;

    if (adaptable instanceof Accessor) {
      Accessor accessor = (Accessor) adaptable;

      final JsonSerialize serializeInfo = accessor.getAnnotation(JsonSerialize.class);

      if (serializeInfo != null) {
        DecoratedProcessingEnvironment env = context.getContext().getProcessingEnvironment();

        DecoratedTypeMirror using = Annotations.mirrorOf(new Callable<Class<?>>() {
          @Override
          public Class<?> call() throws Exception {
            return serializeInfo.using();
          }
        }, env, JsonSerializer.None.class);

        if (using != null) {
          //we're using some custom serialization, so we just have to return a generic object.
          return KnownJsonType.OBJECT;
        }
        else {
          DecoratedTypeMirror as = Annotations.mirrorOf(new Callable<Class<?>>() {
            @Override
            public Class<?> call() throws Exception {
              return serializeInfo.as();
            }
          }, env, Void.class);

          if (as != null) {
            return getJsonType(as, context);
          }
          else {
            DecoratedTypeMirror contentAs = Annotations.mirrorOf(new Callable<Class<?>>() {
              @Override
              public Class<?> call() throws Exception {
                return serializeInfo.contentAs();
              }
            }, env, Void.class);

            DecoratedTypeMirror contentUsing = Annotations.mirrorOf(new Callable<Class<?>>() {
              @Override
              public Class<?> call() throws Exception {
                return serializeInfo.contentUsing();
              }
            }, env, JsonSerializer.None.class);

            DecoratedTypeMirror accessorType = (DecoratedTypeMirror) accessor.asType();
            if (accessorType.isCollection() || accessorType.isArray()) {
              if (contentUsing != null) {
                //we're using some custom serialization of the elements of the collection, so
                //the json type has to be just a list of object.
                return new JsonArrayType(KnownJsonType.OBJECT);
              }
              else if (contentAs != null) {
                return new JsonArrayType(getJsonType(contentAs, context));
              }
            }
            else {
              MapType mapType = MapType.findMapType(accessorType, context);
              if (mapType != null) {
                DecoratedTypeMirror keyAs = Annotations.mirrorOf(new Callable<Class<?>>() {
                  @Override
                  public Class<?> call() throws Exception {
                    return serializeInfo.keyAs();
                  }
                }, env, Void.class);

                DecoratedTypeMirror keyUsing = Annotations.mirrorOf(new Callable<Class<?>>() {
                  @Override
                  public Class<?> call() throws Exception {
                    return serializeInfo.keyUsing();
                  }
                }, env, JsonSerializer.None.class);

                if (keyAs != null || contentAs != null) {
                  JsonType keyType = keyUsing == null ? getJsonType(keyAs == null ? (DecoratedTypeMirror) mapType.getKeyType() : keyAs, context) : KnownJsonType.OBJECT;
                  JsonType valueType = contentUsing == null ? getJsonType(contentAs == null ? (DecoratedTypeMirror) mapType.getValueType() : contentAs, context) : KnownJsonType.OBJECT;
                  return new JsonMapType(keyType, valueType);
                }
              }
            }
          }
        }
      }
    }

    if (adaptable.isAdapted()) {
      jsonType = getJsonType(adaptable.getAdapterType().getAdaptingType(), context);
    }

    return jsonType;
  }

  /**
   * Get the json type for the specified type mirror.
   *
   * @param typeMirror The type mirror.
   * @param context The context.
   * @return The json type for the specified type mirror.
   */
  public static JsonType getJsonType(TypeMirror typeMirror, EnunciateJackson1Context context) {
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror, context.getContext().getProcessingEnvironment());
    JsonTypeVisitor visitor = new JsonTypeVisitor();
    TypeMirror componentType = TypeMirrorUtils.getComponentType(decorated, context.getContext().getProcessingEnvironment());
    componentType = componentType == null ? decorated : componentType;
    return componentType.accept(visitor, new JsonTypeVisitor.Context(context, decorated.isArray(), decorated.isCollection()));
  }

}
