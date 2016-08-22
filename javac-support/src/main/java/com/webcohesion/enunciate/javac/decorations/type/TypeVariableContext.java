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
package com.webcohesion.enunciate.javac.decorations.type;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class TypeVariableContext {

  private final TypeVariableContext stack;
  private final List<? extends TypeParameterElement> params;
  private final List<? extends TypeMirror> variables;

  public TypeVariableContext() {
    this(null, new ArrayList<TypeParameterElement>(), new ArrayList<TypeMirror>());
  }

  private TypeVariableContext(TypeVariableContext stack, List<? extends TypeParameterElement> params, List<? extends TypeMirror> variables) {
    this.stack = stack;
    this.params = params;
    this.variables = variables;
  }

  public TypeVariableContext push(List<? extends TypeParameterElement> params, List<? extends TypeMirror> variables) {
    return new TypeVariableContext(this, params, variables);
  }

  public TypeMirror resolveTypeVariables(TypeMirror var, ProcessingEnvironment env) {
    TypeMirror resolved = resolveTypeVariable(var);

    if (resolved.getKind() == TypeKind.DECLARED) {
      //if we resolved to a declared type, we need to resolve the type arguments, too.
      List<? extends TypeMirror> args = ((DeclaredType) resolved).getTypeArguments();
      TypeMirror[] resolvedArgs = new TypeMirror[args.size()];
      for (int i = 0; i < args.size(); i++) {
        resolvedArgs[i] = resolveTypeVariables(args.get(i), env);
      }

      resolved = env.getTypeUtils().getDeclaredType((TypeElement) ((DeclaredType) resolved).asElement(), resolvedArgs);
    }

    return resolved;
  }

  public TypeMirror resolveTypeVariable(TypeMirror typeVariable) {
    if (typeVariable.getKind() == TypeKind.TYPEVAR) {
      int argIndex = -1;

      Name name = ((TypeVariable) typeVariable).asElement().getSimpleName();
      for (int i = 0; i < this.params.size(); i++) {
        TypeParameterElement elementParam = this.params.get(i);
        if (elementParam.getSimpleName().equals(name)) {
          argIndex = i;
          break;
        }
      }

      if (argIndex < 0 || this.variables.size() != this.params.size()) {
        //best we can do is get the upper bound. should this maybe be an illegal state?
        typeVariable = ((TypeVariable) typeVariable).getUpperBound();
      }
      else {
        typeVariable = this.variables.get(argIndex);
      }
    }

    if (typeVariable.getKind() == TypeKind.TYPEVAR) {
      if (this.stack == null) {
        //end of the stack; return the upper bound, I guess..
        typeVariable = ((TypeVariable) typeVariable).getUpperBound();
      }
      else {
        typeVariable = this.stack.resolveTypeVariable(typeVariable);
      }
    }

    return typeVariable;
  }
}
