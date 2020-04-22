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
package com.webcohesion.enunciate.modules.jackson1.model.types;

import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import com.webcohesion.enunciate.modules.jackson1.EnunciateJackson1Context;
import com.webcohesion.enunciate.modules.jackson1.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jackson1.model.util.JacksonUtil;
import com.webcohesion.enunciate.modules.jackson1.model.util.MapType;
import com.webcohesion.enunciate.util.TypeHintUtils;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.SimpleTypeVisitor6;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import static com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils.getComponentType;

/**
 * Utility visitor for discovering the json types of type mirrors.
 *
 * @author Ryan Heaton
 */
public class JsonTypeVisitor extends SimpleTypeVisitor6<JsonType, JsonTypeVisitor.Context> {

  @Override
  protected JsonType defaultAction(TypeMirror typeMirror, Context context) {
    return KnownJsonType.OBJECT;
  }

  @Override
  public JsonType visitPrimitive(PrimitiveType primitiveType, Context context) {
    if (context.isInArray() && (primitiveType.getKind() == TypeKind.BYTE)) {
      //special case for byte[]
      return KnownJsonType.STRING; //todo: make sure this is correct serialization of byte[].
    }
    else {
      JsonPrimitiveType jsonType = new JsonPrimitiveType(primitiveType);
      return wrapAsNeeded(jsonType, context);
    }
  }

  private JsonType wrapAsNeeded(JsonType jsonType, Context context) {
    return context.isInArray() || context.isInCollection() ? new JsonArrayType(jsonType) : jsonType;
  }

  @Override
  public JsonType visitDeclared(DeclaredType declaredType, Context context) {
    JsonType jsonType = null;

    Element declaredElement = declaredType.asElement();
    DecoratedProcessingEnvironment env = context.getContext().getContext().getProcessingEnvironment();
    DecoratedTypeMirror decoratedTypeMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(declaredType, env);
    String fqn = declaredElement instanceof TypeElement ? ((TypeElement) declaredElement).getQualifiedName().toString() : declaredType.toString();
    if (context.getStack().contains(fqn) && !decoratedTypeMirror.isCollection() && !decoratedTypeMirror.isStream()) {
      //break out of recursive loop.
      return wrapAsNeeded(KnownJsonType.OBJECT, context);
    }

    context.getStack().push(fqn);

    try {
      TypeHint typeHint = declaredElement.getAnnotation(TypeHint.class);
      if (typeHint != null) {
        TypeMirror hint = TypeHintUtils.getTypeHint(typeHint, context.getContext().getContext().getProcessingEnvironment(), null);
        if (hint != null) {
          jsonType = hint.accept(this, new Context(context.context, false, false, context.stack));
        }
      }

      final JsonSerialize serializeInfo = declaredElement.getAnnotation(JsonSerialize.class);
      if (serializeInfo != null) {
        DecoratedTypeMirror using = Annotations.mirrorOf(new Callable<Class<?>>() {
          @Override
          public Class<?> call() throws Exception {
            return serializeInfo.using();
          }
        }, env, JsonSerializer.None.class);

        if (using != null) {
          //custom serializer; just say it's an object.
          jsonType = KnownJsonType.OBJECT;
        }

        DecoratedTypeMirror as = Annotations.mirrorOf(new Callable<Class<?>>() {
          @Override
          public Class<?> call() throws Exception {
            return serializeInfo.as();
          }
        }, env, Void.class);

        if (as != null) {
          jsonType = (JsonType) as.accept(this, new Context(context.context, false, false, context.stack));
        }
      }

      AdapterType adapterType = JacksonUtil.findAdapterType(declaredElement, context.getContext());
      if (adapterType != null) {
        adapterType.getAdaptingType().accept(this, new Context(context.context, false, false, context.stack));
      }
      else {
        MapType mapType = MapType.findMapType(declaredType, context.getContext());
        if (mapType != null) {
          JsonType keyType = mapType.getKeyType().accept(this, new Context(context.getContext(), false, false, context.getStack()));
          JsonType valueType = mapType.getValueType().accept(this, new Context(context.getContext(), false, false, context.getStack()));
          jsonType = new JsonMapType(keyType, valueType);
        }
        else {
          TypeMirror componentType = getComponentType(decoratedTypeMirror, env);
          if (componentType != null) {
            return wrapAsNeeded(componentType.accept(this, new Context(context.context, false, true, context.stack)), context);
          }
          else {
            switch (declaredElement.getKind()) {
              case ENUM:
              case CLASS:
              case INTERFACE:
                JsonType knownType = context.getContext().getKnownType(declaredElement);
                if (knownType != null) {
                  jsonType = knownType;
                }
                else {
                  //type not known, not specified.  Last chance: look for the type definition.
                  TypeDefinition typeDefinition = context.getContext().findTypeDefinition(declaredElement);
                  if (typeDefinition != null) {
                    jsonType = new JsonClassType(typeDefinition);
                  }
                }
                break;
            }
          }
        }
      }

      if (jsonType == null) {
        jsonType = super.visitDeclared(declaredType, context);
      }

      return wrapAsNeeded(jsonType, context);
    }
    finally {
      context.getStack().pop();
    }
  }

  @Override
  public JsonType visitArray(ArrayType arrayType, Context context) {
    return wrapAsNeeded(arrayType.getComponentType().accept(this, new Context(context.context, true, false, context.stack)), context);
  }

  @Override
  public JsonType visitTypeVariable(TypeVariable typeVariable, Context context) {
    TypeMirror bound = typeVariable.getUpperBound();
    if (bound == null) {
      return wrapAsNeeded(KnownJsonType.OBJECT, context);
    }
    else {
      JsonType jsonType = bound.accept(this, new Context(context.context, false, false, context.stack));
      return wrapAsNeeded(jsonType, context);
    }
  }

  @Override
  public JsonType visitWildcard(WildcardType wildcardType, Context context) {
    TypeMirror bound = wildcardType.getExtendsBound();
    if (bound == null) {
      return wrapAsNeeded(KnownJsonType.OBJECT, context);
    }
    else {
      JsonType jsonType = bound.accept(this, new Context(context.context, false, false, context.stack));
      return wrapAsNeeded(jsonType, context);
    }
  }

  public static class Context {

    private final EnunciateJackson1Context context;
    private final boolean inArray;
    private final boolean inCollection;
    private final LinkedList<String> stack;

    public Context(EnunciateJackson1Context context, boolean inArray, boolean inCollection, LinkedList<String> stack) {
      this.context = context;
      this.inArray = inArray;
      this.inCollection = inCollection;
      this.stack = stack;
    }

    public EnunciateJackson1Context getContext() {
      return context;
    }

    public boolean isInArray() {
      return inArray;
    }

    public boolean isInCollection() {
      return inCollection;
    }

    public LinkedList<String> getStack() {
      return stack;
    }
  }
}
