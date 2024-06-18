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
package com.webcohesion.enunciate.modules.csharp_client;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
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
import jakarta.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.webcohesion.enunciate.javac.decorations.element.ElementUtils.isCollection;

/**
 * Conversion from java types to C# types.
 *
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends com.webcohesion.enunciate.util.freemarker.ClientClassnameForMethod {

  private final EnunciateJaxbContext jaxbContext;
  private final Map<String, String> classConversions = new HashMap<String, String>();

  public ClientClassnameForMethod(Map<String, String> conversions, EnunciateJaxbContext jaxbContext) {
    super(conversions, jaxbContext.getContext());
    this.jaxbContext = jaxbContext;

    classConversions.put(Boolean.class.getName(), "bool?");
    classConversions.put(AtomicBoolean.class.getName(), "bool?");
    classConversions.put(String.class.getName(), "string");
    classConversions.put(Integer.class.getName(), "int?");
    classConversions.put(AtomicInteger.class.getName(), "int?");
    classConversions.put(Short.class.getName(), "short?");
    classConversions.put(Byte.class.getName(), "sbyte?");
    classConversions.put(Double.class.getName(), "double?");
    classConversions.put(Long.class.getName(), "long?");
    classConversions.put(AtomicLong.class.getName(), "long?");
    classConversions.put(java.math.BigInteger.class.getName(), "long?");
    classConversions.put(java.math.BigDecimal.class.getName(), "decimal?");
    classConversions.put(Float.class.getName(), "float?");
    classConversions.put(Character.class.getName(), "ushort?");
    classConversions.put(Date.class.getName(), "DateTime?");
    classConversions.put(Timestamp.class.getName(), "DateTime?");
    classConversions.put(DataHandler.class.getName(), "byte[]");
    classConversions.put(java.awt.Image.class.getName(), "byte[]");
    classConversions.put(javax.xml.transform.Source.class.getName(), "byte[]");
    classConversions.put(QName.class.getName(), "global::System.Xml.XmlQualifiedName");
    classConversions.put(URI.class.getName(), "string");
    classConversions.put(UUID.class.getName(), "string");
    classConversions.put(XMLGregorianCalendar.class.getName(), "DateTime?");
    classConversions.put(GregorianCalendar.class.getName(), "DateTime?");
    classConversions.put(Calendar.class.getName(), "DateTime?");
    classConversions.put(javax.xml.datatype.Duration.class.getName(), "TimeSpan?");
    classConversions.put(jakarta.xml.bind.JAXBElement.class.getName(), "object");
    classConversions.put(Object.class.getName(), "object");
    classConversions.put(Record.class.getName(), "object");
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
  public String convert(TypeElement declaration) throws TemplateModelException {
    String fqn = declaration.getQualifiedName().toString();
    if (classConversions.containsKey(fqn)) {
      return classConversions.get(fqn);
    }
    else if (isCollection(declaration)) {
      return "global::System.Collections.ArrayList";
    }

    AdapterType adapterType = JAXBUtil.findAdapterType(declaration, this.jaxbContext);
    if (adapterType != null) {
      return convert(adapterType.getAdaptingType());
    }
    if (ElementUtils.isClassOrRecord(declaration)) {
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
  public String convert(HasClientConvertibleType element) throws TemplateModelException {
    if (element instanceof Adaptable && ((Adaptable) element).isAdapted()) {
      return convert(((Adaptable) element).getAdapterType().getAdaptingType((DecoratedTypeMirror) element.getClientConvertibleType(), this.context));
    }
    else if (element instanceof Accessor && ((Accessor)element).isXmlIDREF()) {
      return "string";//C# doesn't support strict object reference resolution via IDREF.  The best we can do is (de)serialize the ID.
    }

    return super.convert(element);
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror, context.getProcessingEnvironment());
    if (decorated.isPrimitive()) {
      TypeKind kind = decorated.getKind();
      switch (kind) {
        case BOOLEAN:
          return "bool"; //boolean as 'bool'
        case CHAR:
          return "ushort";
        default:
          return kind.toString().toLowerCase();
      }
    }
    else if (decorated.isCollection()) {
      return getCollectionTypeConversion((DeclaredType) typeMirror);
    }

    return super.convert(typeMirror);
  }

  @Override
  public String convertDeclaredTypeArguments(List<? extends TypeMirror> actualTypeArguments) throws TemplateModelException {
    return ""; //we'll handle generics ourselves.
  }

  @Override
  public String convert(TypeVariable typeVariable) throws TemplateModelException {
    String conversion = "object";

    if (typeVariable.getUpperBound() != null) {
      conversion = convert(typeVariable.getUpperBound());
    }

    return conversion;
  }

  protected String getCollectionTypeConversion(DeclaredType declaredType) throws TemplateModelException {
    List<? extends TypeMirror> actualTypeArguments = declaredType.getTypeArguments();
    if (actualTypeArguments.size() == 1) {
      TypeMirror typeArg = actualTypeArguments.iterator().next();
      if (typeArg instanceof WildcardType) {
        WildcardType wildcardType = (WildcardType) typeArg;
        if (wildcardType.getExtendsBound() == null) {
          return "global::System.Collections.ArrayList";
        }
        else {
          return "global::System.Collections.Generic.List<" + convert(wildcardType.getExtendsBound()) + ">";
        }
      }
      else if (typeArg instanceof TypeVariable) {
        TypeMirror bound = ((TypeVariable) typeArg).getUpperBound();
        if (bound == null) {
          return "global::System.Collections.ArrayList";
        }
        else {
          return "global::System.Collections.Generic.List<" + convert(bound) + ">";
        }
      }
      else {
        return "global::System.Collections.Generic.List<" + convert(typeArg) + ">";
      }
    }
    else {
      return "global::System.Collections.ArrayList";
    }
  }

}
