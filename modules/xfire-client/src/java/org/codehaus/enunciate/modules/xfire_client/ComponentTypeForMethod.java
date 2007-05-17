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

package org.codehaus.enunciate.modules.xfire_client;

import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.jaxb.Accessor;
import org.codehaus.enunciate.contract.jaxb.adapters.Adaptable;
import org.codehaus.enunciate.contract.jaxws.ImplicitChildElement;

import java.util.Iterator;
import java.util.Map;

/**
 * Converts a fully-qualified class name to its alternate client fully-qualified class name.
 *
 * @author Ryan Heaton
 */
public class ComponentTypeForMethod extends ClientClassnameForMethod {

  public ComponentTypeForMethod(Map<String, String> conversions) {
    super(conversions);
  }

  @Override
  protected String convert(ImplicitChildElement childElement) throws TemplateModelException {
    if ((!isJdk15()) && (childElement instanceof Adaptable) && (((Adaptable)childElement).isAdapted())) {
      //the adapting type is already unwrapped...
      return convert(((Adaptable)childElement).getAdapterType().getAdaptingType());
    }
    else {
      return convert(childElement.getType());
    }

  }

  @Override
  protected String convert(Accessor accessor) throws TemplateModelException {
    if (!isJdk15() && accessor.isAdapted()) {
      //if the type is adapted, the adapting type is already unwrapped.
      return convert(accessor.getAdapterType().getAdaptingType());
    }
    else {
      return convert(accessor.getAccessorType());
    }
  }

  @Override
  protected String convert(TypeMirror typeMirror) throws TemplateModelException {
    if (typeMirror instanceof ArrayType) {
      TypeMirror componentType = ((ArrayType) typeMirror).getComponentType();
      if (!(componentType instanceof PrimitiveType) || (((PrimitiveType)componentType).getKind() != PrimitiveType.Kind.BYTE)) {
        return super.convert(componentType);
      }
    }
    else if (typeMirror instanceof DeclaredType) {
      DecoratedTypeMirror decoratedTypeMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror);
      if (decoratedTypeMirror.isCollection()) {
        DeclaredType declaredType = (DeclaredType) typeMirror;
        Iterator<TypeMirror> actualTypeArguments = declaredType.getActualTypeArguments().iterator();
        if (actualTypeArguments.hasNext()) {
          return super.convert(actualTypeArguments.next());
        }
      }
    }

    return super.convert(typeMirror);
  }

}
