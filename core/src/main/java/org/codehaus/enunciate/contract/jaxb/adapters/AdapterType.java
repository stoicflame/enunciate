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

package org.codehaus.enunciate.contract.jaxb.adapters;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.*;
import com.sun.mirror.util.Types;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.type.DecoratedClassType;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Collection;
import java.util.Iterator;

import org.codehaus.enunciate.contract.validation.ValidationException;

/**
 * A type mirror that mirrors an {@link javax.xml.bind.annotation.adapters.XmlAdapter}.
 * 
 * @author Ryan Heaton
 */
public class AdapterType extends DecoratedClassType {

  private final ReferenceType adaptedType;
  private final TypeMirror adaptingType;

  public AdapterType(ClassType adapterType) {
    super(adapterType);

    ClassDeclaration adapterDeclaration = adapterType.getDeclaration();

    ClassType adaptorInterfaceType = findXmlAdapterType(adapterDeclaration);
    if (adaptorInterfaceType == null) {
      throw new ValidationException(adapterDeclaration.getPosition(), adapterDeclaration.getQualifiedName() + " is not an instance of javax.xml.bind.annotation.adapters.XmlAdapter.");
    }

    Collection<TypeMirror> adaptorTypeArgs = adaptorInterfaceType.getActualTypeArguments();
    if ((adaptorTypeArgs == null) || (adaptorTypeArgs.size() != 2)) {
      throw new ValidationException(adapterDeclaration.getPosition(), adapterDeclaration.getQualifiedName() +
        " must specify both a value type and a bound type.");
    }

    Iterator<TypeMirror> formalTypeIt = adaptorTypeArgs.iterator();
    this.adaptingType = TypeMirrorDecorator.decorate(formalTypeIt.next());
    TypeMirror boundTypeMirror = formalTypeIt.next();
    if (!(boundTypeMirror instanceof ReferenceType)) {
      throw new ValidationException(adapterDeclaration.getPosition(), adapterDeclaration.getQualifiedName() + ": illegal XML adapter: not adapting a reference type (" + boundTypeMirror + ").");
    }

    this.adaptedType = TypeMirrorDecorator.decorate((ReferenceType) boundTypeMirror);
  }

  /**
   * Finds the interface type that declares that the specified declaration implements XmlAdapter.
   *
   * @param declaration The declaration.
   * @return The interface type, or null if none found.
   */
  private static ClassType findXmlAdapterType(ClassDeclaration declaration) {
    if (Object.class.getName().equals(declaration.getQualifiedName())) {
      return null;
    }

    ClassType superClass = declaration.getSuperclass();
    if (XmlAdapter.class.getName().equals(superClass.getDeclaration().getQualifiedName())) {
      return superClass;
    }

    return findXmlAdapterType(superClass.getDeclaration());
  }

  /**
   * Whether this adapter can adapt the specified type.
   *
   * @param type The type.
   * @return Whether this adapter can adapt the specified type.
   */
  public boolean canAdapt(ReferenceType type) {
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(type);
    ReferenceType adaptedType = getAdaptedType();
    if (adaptedType instanceof DeclaredType) {
      return ((DeclaredType)adaptedType).getDeclaration() != null && decorated.isInstanceOf(((DeclaredType)adaptedType).getDeclaration().getQualifiedName());
    }
    else if (adaptedType instanceof ArrayType) {
      if (decorated instanceof ArrayType) {
        TypeMirror adaptedComponentType = ((ArrayType) adaptedType).getComponentType();
        while (adaptedComponentType instanceof DecoratedTypeMirror) {
          adaptedComponentType = ((DecoratedTypeMirror) adaptedComponentType).getDelegate();
        }
        TypeMirror componentType = ((ArrayType) decorated).getComponentType();
        while (componentType instanceof DecoratedTypeMirror) {
          componentType = ((DecoratedTypeMirror) componentType).getDelegate();
        }
        Types typeUtils = Context.getCurrentEnvironment().getTypeUtils();
        return typeUtils.isAssignable(componentType, adaptedComponentType);
      }
    }

    return false;
  }

  /**
   * The type that is being adapted by this adapter.
   *
   * @return The type that is being adapted by this adapter.
   */
  public ReferenceType getAdaptedType() {
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
