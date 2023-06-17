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


import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeVisitor;

/**
 * A decorated type mirror provides:
 *
 * <ul>
 *   <li>A string property denoting the java keyword for its {@link #getKind() kind}.
 * </ul>
 *
 * @author Ryan Heaton
 */
public class DecoratedPrimitiveType extends DecoratedTypeMirror<PrimitiveType> implements PrimitiveType {

  public DecoratedPrimitiveType(PrimitiveType delegate, DecoratedProcessingEnvironment env) {
    super(delegate, env);
  }

  public boolean isInstanceOf(String typeName) {
    return getKeyword().equals(typeName) || super.isInstanceOf(typeName);
  }

  public boolean isPrimitive() {
    return true;
  }

  public String getKeyword() {
    return String.valueOf(getKind()).toLowerCase();
  }

  @Override
  public String toString() {
    //for some reason, the `toString()` method in the primitive type started including `@javax.*` annotations in Java 11?
    //todo: check if this ever got fixed and remove this override if possible?
    return getKeyword();
  }

  @Override
  public <R, P> R accept(TypeVisitor<R, P> v, P p) {
    return v.visitPrimitive(this, p);
  }
}
