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

package org.codehaus.enunciate.template.freemarker;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.TypeMirror;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.ClientName;
import org.codehaus.enunciate.contract.jaxb.Accessor;
import org.codehaus.enunciate.contract.jaxb.ImplicitChildElement;
import org.codehaus.enunciate.contract.jaxb.adapters.Adaptable;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterType;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterUtil;

import javax.xml.bind.JAXBElement;
import java.util.Map;

/**
 * Converts a fully-qualified class name to its alternate client fully-qualified class name.
 *
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends ClassnameForMethod {

  public ClientClassnameForMethod(Map<String, String> conversions) {
    super(conversions);
  }

  @Override
  public String convert(ImplicitChildElement childElement) throws TemplateModelException {
    TypeMirror elementType = childElement.getType();
    if ((childElement instanceof Adaptable) && (((Adaptable) childElement).isAdapted())) {
      elementType = (((Adaptable) childElement).getAdapterType().getAdaptingType(childElement.getType()));
    }

    return convert(elementType);
  }

  @Override
  public String convert(Accessor accessor) throws TemplateModelException {
    TypeMirror accessorType = accessor.getAccessorType();

    if (accessor.isAdapted()) {
      accessorType = accessor.getAdapterType().getAdaptingType(accessor.getAccessorType());
    }

    return convert(accessorType);
  }

  @Override
  public String convert(TypeDeclaration declaration) throws TemplateModelException {
    AdapterType adapterType = AdapterUtil.findAdapterType(declaration);
    if (adapterType != null) {
      return convert(adapterType.getAdaptingType());
    }
    if (declaration instanceof ClassDeclaration) {
      DecoratedTypeMirror superType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(((ClassDeclaration) declaration).getSuperclass());
      if (superType != null && superType.isInstanceOf(JAXBElement.class.getName())) {
        //for client conversions, we're going to generalize subclasses of JAXBElement to JAXBElement
        return convert(superType);
      }
    }
    String convertedPackage = convertPackage(declaration.getPackage());
    ClientName specifiedName = isUseClientNameConversions() ? declaration.getAnnotation(ClientName.class) : null;
    String simpleName = specifiedName == null ? declaration.getSimpleName() : specifiedName.value();
    return convertedPackage + getPackageSeparator() + simpleName;
  }
}
