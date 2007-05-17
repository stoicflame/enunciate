/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.contract.jaxb.util;

import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.Context;

import java.util.Iterator;

/**
 * Consolidation of common logic for implementing the JAXB contract.
 * 
 * @author Ryan Heaton
 */
public class JAXBUtil {

  /**
   * Unwraps the specified type to its component type if its a collection or an array.  Otherwise, the
   * specified type is returned.
   *
   * @param typeMirror The type to unwrap if necessary.
   * @return The component type, or the type itself.
   */
  public static TypeMirror unwrapComponentType(TypeMirror typeMirror) {
    DecoratedTypeMirror decoratedType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror);

    if (decoratedType.isArray()) {
      typeMirror = ((ArrayType) decoratedType).getComponentType();
    }
    else if (decoratedType.isCollection()) {
      //if it's a collection type, the xml type is its component type.
      Iterator<TypeMirror> actualTypeArguments = ((DeclaredType) decoratedType).getActualTypeArguments().iterator();
      if (!actualTypeArguments.hasNext()) {
        //no type arguments, java.lang.Object type.
        AnnotationProcessorEnvironment ape = Context.getCurrentEnvironment();
        typeMirror = ape.getTypeUtils().getDeclaredType(ape.getTypeDeclaration(Object.class.getName()));
      }
      else {
        typeMirror = actualTypeArguments.next();
      }
    }
    else {
      return typeMirror;
    }

    return typeMirror;
  }

}
