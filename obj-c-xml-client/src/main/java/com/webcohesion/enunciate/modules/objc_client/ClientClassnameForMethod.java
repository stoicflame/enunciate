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
package com.webcohesion.enunciate.modules.objc_client;

import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.Adaptable;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jaxb.model.util.JAXBUtil;
import com.webcohesion.enunciate.util.HasClientConvertibleType;
import freemarker.template.TemplateModelException;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;

import jakarta.activation.DataHandler;

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
 * Conversion from java types to Objective-C types.
 *
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends com.webcohesion.enunciate.util.freemarker.ClientClassnameForMethod {

  private final Map<String, String> classConversions = new HashMap<String, String>();
  private final EnunciateJaxbContext jaxbContext;

  public ClientClassnameForMethod(Map<String, String> conversions, EnunciateJaxbContext jaxbContext) {
    super(conversions, jaxbContext.getContext());
    this.jaxbContext = jaxbContext;

    classConversions.put(Boolean.class.getName(), "BOOL");
    classConversions.put(AtomicBoolean.class.getName(), "BOOL");
    classConversions.put(String.class.getName(), "NSString");
    classConversions.put(Integer.class.getName(), "int");
    classConversions.put(AtomicInteger.class.getName(), "int");
    classConversions.put(Short.class.getName(), "short");
    classConversions.put(Byte.class.getName(), "unsigned char");
    classConversions.put(Double.class.getName(), "double");
    classConversions.put(Long.class.getName(), "long long");
    classConversions.put(AtomicLong.class.getName(), "long long");
    classConversions.put(java.math.BigInteger.class.getName(), "NSNumber");
    classConversions.put(java.math.BigDecimal.class.getName(), "NSDecimalNumber");
    classConversions.put(Float.class.getName(), "float");
    classConversions.put(Character.class.getName(), "unsigned short");
    classConversions.put(Date.class.getName(), "NSDate");
    classConversions.put(Timestamp.class.getName(), "NSDate");
    classConversions.put(DataHandler.class.getName(), "NSData");
    classConversions.put(java.awt.Image.class.getName(), "NSData");
    classConversions.put(javax.xml.transform.Source.class.getName(), "NSData");
    classConversions.put(QName.class.getName(), "QName");
    classConversions.put(URI.class.getName(), "NSURL");
    classConversions.put(UUID.class.getName(), "NSString");
    classConversions.put(XMLGregorianCalendar.class.getName(), "NSCalendarDate");
    classConversions.put(GregorianCalendar.class.getName(), "NSCalendarDate");
    classConversions.put(Calendar.class.getName(), "NSCalendarDate");
    classConversions.put(javax.xml.datatype.Duration.class.getName(), "NSString");
    classConversions.put(jakarta.xml.bind.JAXBElement.class.getName(), "JAXBBasicXMLNode");
    classConversions.put(Object.class.getName(), "NSObject");
    classConversions.putAll(conversions);
  }

  @Override
  public String convert(TypeElement declaration) throws TemplateModelException {
    String fqn = declaration.getQualifiedName().toString();
    if (classConversions.containsKey(fqn)) {
      return classConversions.get(fqn);
    }
    else if (isCollection(declaration)) {
      return "NSArray";
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
    if (element instanceof Accessor) {
      Accessor accessor = (Accessor) element;
      if (accessor.isXmlList()) {
        return "NSString";
      }

      if (accessor.isXmlIDREF() && !accessor.isCollectionType()) {
        return "NSString";
      }
    }

    if (element instanceof Adaptable && ((Adaptable) element).isAdapted()) {
      return convert(((Adaptable) element).getAdapterType().getAdaptingType((DecoratedTypeMirror) element.getClientConvertibleType(), this.context));
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
          return "BOOL";
        case BYTE:
          return "unsigned char";
        case INT:
          return "int";
        case SHORT:
          return "short";
        case FLOAT:
          return "float";
        case DOUBLE:
          return "double";
        case LONG:
          return "long long";
        case CHAR:
          return "unsigned short";
        default:
          return "NSString";
      }
    }
    else if (decorated.isCollection()) {
      return "NSArray";
    }
    else if (decorated.isWildcard()) {
      WildcardType wildcardType = (WildcardType) decorated;
      return convert(wildcardType.getExtendsBound());
    }
    else if (decorated.isArray()) {
      TypeMirror componentType = ((ArrayType) decorated).getComponentType();
      if (componentType instanceof PrimitiveType) {
        if (componentType.getKind() == TypeKind.BYTE) {
          return "NSData";
        }
      }

      return "NSArray";
    }

    return super.convert(typeMirror);
  }

  @Override
  public String convertDeclaredTypeArguments(List<? extends TypeMirror> actualTypeArguments) throws TemplateModelException {
    return ""; //we'll handle generics ourselves.
  }

  @Override
  public String convert(TypeVariable typeVariable) throws TemplateModelException {
    String conversion = "NSObject";

    if (typeVariable.getUpperBound() != null) {
      conversion = convert(typeVariable.getUpperBound());
    }

    return conversion;
  }

}
