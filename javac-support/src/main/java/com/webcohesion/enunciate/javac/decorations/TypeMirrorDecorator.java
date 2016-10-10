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
package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.decorations.type.*;

import javax.lang.model.type.*;
import javax.lang.model.util.SimpleTypeVisitor6;
import java.util.ArrayList;
import java.util.List;

/**
 * Decorates a {@link TypeMirror} when visited.
 *
 * @author Ryan Heaton
 */
@SuppressWarnings({"unchecked"})
public class TypeMirrorDecorator<T extends TypeMirror> extends SimpleTypeVisitor6<T, Void> {

  private final DecoratedProcessingEnvironment env;

  public TypeMirrorDecorator(DecoratedProcessingEnvironment env) {
    this.env = env;
  }

  /**
   * Decorate a type mirror.
   *
   * @param typeMirror The mirror to decorate.
   * @param env The environment.
   * @return The decorated type mirror.
   */
  public static <T extends TypeMirror> T decorate(T typeMirror, DecoratedProcessingEnvironment env) {
    if (typeMirror == null) {
      return null;
    }

    if (typeMirror instanceof DecoratedTypeMirror) {
      return typeMirror;
    }

    TypeMirrorDecorator<T> decorator = new TypeMirrorDecorator<T>(env);
    return typeMirror.accept(decorator, null);
  }

  /**
   * Decorate a collection fo type mirrors.
   *
   * @param typeMirrors The type mirrors to decorate.
   * @param env The environment.
   * @return The collection of decorated type mirrors.
   */
  public static <T extends TypeMirror> List<T> decorate(List<T> typeMirrors, DecoratedProcessingEnvironment env) {
    if (typeMirrors == null) {
      return null;
    }

    ArrayList<T> mirrors = new ArrayList<T>(typeMirrors.size());
    for (T mirror : typeMirrors) {
      mirrors.add(decorate(mirror, env));
    }
    return mirrors;
  }

  @Override
  public T visitPrimitive(PrimitiveType t, Void nil) {
    return (T) new DecoratedPrimitiveType(t, this.env);
  }

  @Override
  public T visitNull(NullType t, Void nil) {
    return (T) new DecoratedNullType(t, this.env);
  }

  @Override
  public T visitArray(ArrayType t, Void nil) {
    return (T) new DecoratedArrayType(t, this.env);
  }

  @Override
  public T visitDeclared(DeclaredType t, Void nil) {
    return (T) new DecoratedDeclaredType(t, this.env);
  }

  @Override
  public T visitError(ErrorType t, Void nil) {
    return (T) new DecoratedErrorType(t, this.env);
  }

  @Override
  public T visitTypeVariable(TypeVariable t, Void nil) {
    return (T) new DecoratedTypeVariable(t, this.env);
  }

  @Override
  public T visitWildcard(WildcardType t, Void nil) {
    return (T) new DecoratedWildcardType(t, this.env);
  }

  @Override
  public T visitExecutable(ExecutableType t, Void nil) {
    return (T) new DecoratedExecutableType(t, this.env);
  }

  @Override
  public T visitNoType(NoType t, Void nil) {
    return (T) new DecoratedNoType(t, this.env);
  }
}
