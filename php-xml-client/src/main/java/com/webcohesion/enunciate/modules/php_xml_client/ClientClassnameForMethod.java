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
package com.webcohesion.enunciate.modules.php_xml_client;

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
import com.webcohesion.enunciate.util.HasClientConvertibleType;
import freemarker.template.TemplateModelException;

import jakarta.activation.DataHandler;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.webcohesion.enunciate.javac.decorations.element.ElementUtils.isCollection;
import static com.webcohesion.enunciate.javac.decorations.element.ElementUtils.isMap;

/**
 * Conversion from java types to PHP types.
 *
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends com.webcohesion.enunciate.util.freemarker.ClientClassnameForMethod {

  private final Map<String, String> classConversions = new HashMap<String, String>();
  private final EnunciateJaxbContext jaxbContext;

  public ClientClassnameForMethod(Map<String, String> conversions, EnunciateJaxbContext jaxbContext) {
    super(conversions, jaxbContext.getContext());
    this.jaxbContext = jaxbContext;

    classConversions.put(Boolean.class.getName(), "Boolean");
    classConversions.put(AtomicBoolean.class.getName(), "Boolean");
    classConversions.put(String.class.getName(), "String");
    classConversions.put(Integer.class.getName(), "Integer");
    classConversions.put(AtomicInteger.class.getName(), "Integer");
    classConversions.put(Short.class.getName(), "Integer");
    classConversions.put(Byte.class.getName(), "Integer");
    classConversions.put(Double.class.getName(), "Double");
    classConversions.put(Long.class.getName(), "Integer");
    classConversions.put(AtomicLong.class.getName(), "Integer");
    classConversions.put(java.math.BigInteger.class.getName(), "String");
    classConversions.put(java.math.BigDecimal.class.getName(), "String");
    classConversions.put(Float.class.getName(), "Integer");
    classConversions.put(Character.class.getName(), "Integer");
    classConversions.put(Date.class.getName(), "String");
    classConversions.put(Timestamp.class.getName(), "String");
    classConversions.put(DataHandler.class.getName(), "String");
    classConversions.put(java.awt.Image.class.getName(), "String");
    classConversions.put(javax.xml.transform.Source.class.getName(), "String");
    classConversions.put(QName.class.getName(), "String");
    classConversions.put(URI.class.getName(), "String");
    classConversions.put(UUID.class.getName(), "String");
    classConversions.put(XMLGregorianCalendar.class.getName(), "String");
    classConversions.put(GregorianCalendar.class.getName(), "String");
    classConversions.put(Calendar.class.getName(), "String");
    classConversions.put(javax.xml.datatype.Duration.class.getName(), "String");
    classConversions.put(jakarta.xml.bind.JAXBElement.class.getName(), "Object");
    classConversions.put(Object.class.getName(), "Object");
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

      return "Object";
    }

    return super.convertUnwrappedObject(unwrapped);
  }

  @Override
  public String convert(TypeElement declaration) throws TemplateModelException {
    String fqn = declaration.getQualifiedName().toString();
    if (classConversions.containsKey(fqn)) {
      return classConversions.get(fqn);
    }
    else if (declaration.getKind() == ElementKind.ENUM) {
      return "String";
    }
    else if (isCollection(declaration) || isMap(declaration)) {
      return "Array";
    }

    AdapterType adapterType = JAXBUtil.findAdapterType(declaration, this.jaxbContext);
    if (adapterType != null) {
      return convert(adapterType.getAdaptingType());
    }

    String convertedPackage = convertPackage(this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration));
    ClientName specifiedName = declaration.getAnnotation(ClientName.class);
    String simpleName = specifiedName == null ? declaration.getSimpleName().toString() : specifiedName.value();
    return convertedPackage + getPackageSeparator() + simpleName;
  }

  @Override
  public String convert(HasClientConvertibleType element) throws TemplateModelException {
    if (element instanceof Adaptable && ((Adaptable) element).isAdapted()) {
      return convert(((Adaptable) element).getAdapterType().getAdaptingType((DecoratedTypeMirror) element.getClientConvertibleType(), this.context));
    }

    if (element instanceof Accessor && ((Accessor) element).isXmlIDREF()) {
      return "String";
    }

    if (element instanceof Accessor && ((Accessor) element).isXmlList()) {
      return "NSString";
    }

    return super.convert(element);
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror, this.context.getProcessingEnvironment());
    if (decorated.isPrimitive()) {
      TypeKind kind = decorated.getKind();
      switch (kind) {
        case BOOLEAN:
          return "Boolean";
        case BYTE:
        case INT:
        case SHORT:
        case CHAR:
        case FLOAT:
        case DOUBLE:
          return "Double";
        case LONG:
          return "Integer";
        default:
          return "String";
      }
    }
    else if (decorated.isEnum()) {
      return "String";
    }
    else if (decorated.isCollection()) {
      return "Array";
    }
    else if (decorated.isArray()) {
      TypeMirror componentType = ((ArrayType) decorated).getComponentType();
      if ((componentType instanceof PrimitiveType) && componentType.getKind() == TypeKind.BYTE) {
        return "String";
      }
    }

    return super.convert(typeMirror);
  }

  @Override
  public String convertDeclaredTypeArguments(List<? extends TypeMirror> actualTypeArguments) throws TemplateModelException {
    return ""; //we'll handle generics ourselves.
  }

  @Override
  public String convert(TypeVariable typeVariable) throws TemplateModelException {
    String conversion = "Object";

    if (typeVariable.getUpperBound() != null) {
      conversion = convert(typeVariable.getUpperBound());
    }

    return conversion;
  }

  @Override
  protected String getPackageSeparator() {
    return "\\";
  }

}
