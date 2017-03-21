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
package com.webcohesion.enunciate.modules.java_xml_client;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.api.impl.DataTypeReferenceImpl;
import com.webcohesion.enunciate.modules.jaxb.api.impl.SyntaxImpl;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.Adaptable;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.util.JAXBUtil;
import com.webcohesion.enunciate.modules.jaxb.model.util.MapType;
import com.webcohesion.enunciate.util.HasClientConvertibleType;
import freemarker.template.TemplateModelException;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.JAXBElement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends com.webcohesion.enunciate.util.freemarker.ClientClassnameForMethod {

  protected final LinkedList<String> recursiveMapStack = new LinkedList<String>();
  private final EnunciateJaxbContext jaxbContext;

  public ClientClassnameForMethod(Map<String, String> conversions, EnunciateJaxbContext context) {
    super(conversions, context.getContext());
    this.jaxbContext = context;
  }

  @Override
  public String convertUnwrappedObject(Object unwrapped) throws TemplateModelException {
    if (unwrapped instanceof Entity) {
      List<? extends MediaTypeDescriptor> mediaTypes = ((Entity) unwrapped).getMediaTypes();
      for (MediaTypeDescriptor mediaType : mediaTypes) {
        if (SyntaxImpl.SYNTAX_LABEL.equals(mediaType.getSyntax())) {
          DataTypeReference dataType = mediaType.getDataType();
          if (dataType instanceof DataTypeReferenceImpl) {
            XmlType xmlType = ((DataTypeReferenceImpl) dataType).getXmlType();
            if (xmlType instanceof XmlClassType) {
              super.convertUnwrappedObject(((XmlClassType) xmlType).getTypeDefinition());
            }
          }
        }
      }

      return "byte[]";
    }

    return super.convertUnwrappedObject(unwrapped);
  }

  @Override
  public String convert(HasClientConvertibleType element) throws TemplateModelException {
    if (element instanceof Accessor && ((Accessor)element).isXmlList() && !((Accessor)element).isAdapted() && ((Accessor)element).getBareAccessorType().isInterface()) {
      if (((Accessor)element).isCollectionType()) {
        return "java.util.List<Object>";
      }
      else {
        return "Object";
      }
    }
    else if (element instanceof Adaptable && ((Adaptable) element).isAdapted()) {
      return convert(((Adaptable) element).getAdapterType().getAdaptingType((DecoratedTypeMirror) element.getClientConvertibleType(), this.context));
    }
    else {
      return super.convert(element);
    }
  }

  @Override
  public String convert(TypeElement declaration) throws TemplateModelException {
    AdapterType adapterType = JAXBUtil.findAdapterType(declaration, this.jaxbContext);
    if (adapterType != null) {
      return convert(adapterType.getAdaptingType());
    }
    if (declaration.getKind() == ElementKind.CLASS) {
      DecoratedTypeMirror superType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(declaration.getSuperclass(), this.context.getProcessingEnvironment());
      if (superType != null && superType.isInstanceOf(JAXBElement.class.getName())) {
        //for client conversions, we're going to generalize subclasses of JAXBElement to JAXBElement
        return convert(superType);
      }
    }
    String convertedPackage = convertPackage(this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration));
    ClientName specifiedName = declaration.getAnnotation(ClientName.class);
    String simpleName = specifiedName == null ? declaration.getSimpleName().toString() : specifiedName.value();
    return convertedPackage + getPackageSeparator() + simpleName;
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    DeclaredType mapType = MapType.findMapTypeDeclaration(typeMirror, this.jaxbContext); //normalize map references...
    if (mapType != null) {
      String fqn = typeMirror.toString();
      if (this.recursiveMapStack.contains(fqn)) {
        return "java.lang.Object"; //break the recursion.
      }

      this.recursiveMapStack.push(fqn);
      try {
        return super.convert(mapType);
      }
      finally {
        this.recursiveMapStack.pop();
      }
    }
    else {
      return super.convert(typeMirror);
    }
  }
}
