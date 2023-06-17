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
package com.webcohesion.enunciate.modules.gwt_json_overlay;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.metadata.qname.XmlQNameEnumRef;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;
import com.webcohesion.enunciate.modules.jackson.api.impl.SyntaxImpl;
import com.webcohesion.enunciate.util.HasClientConvertibleType;
import freemarker.template.TemplateModelException;

import javax.activation.DataHandler;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;

import static com.webcohesion.enunciate.javac.decorations.element.ElementUtils.isCollection;

/**
 * Conversion from java types to GWT json overlay types.
 *
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends com.webcohesion.enunciate.util.freemarker.ClientClassnameForMethod {

  private final Map<String, String> classConversions = new HashMap<String, String>();
  private final JsonContext jsonContext;

  public ClientClassnameForMethod(Map<String, String> conversions, JsonContext context) {
    super(conversions, context.getContext());
    this.jsonContext = context;
    classConversions.put(BigDecimal.class.getName(), String.class.getName());
    classConversions.put(BigInteger.class.getName(), String.class.getName());
    classConversions.put(Date.class.getName(), Long.class.getName());
    classConversions.put(Timestamp.class.getName(), Long.class.getName());
    classConversions.put(Calendar.class.getName(), Long.class.getName());
    classConversions.put(DataHandler.class.getName(), String.class.getName());
    classConversions.put(QName.class.getName(), String.class.getName());
    classConversions.put(URI.class.getName(), String.class.getName());
    classConversions.put(UUID.class.getName(), String.class.getName());
    classConversions.put(XMLGregorianCalendar.class.getName(), "double");
    classConversions.put(javax.xml.bind.JAXBElement.class.getName(), "com.google.gwt.core.client.JavaScriptObject");
  }

  @Override
  public String convertUnwrappedObject(Object unwrapped) throws TemplateModelException {
    if (unwrapped instanceof Entity) {
      List<? extends MediaTypeDescriptor> mediaTypes = ((Entity) unwrapped).getMediaTypes();
      for (MediaTypeDescriptor mediaType : mediaTypes) {
        if (SyntaxImpl.SYNTAX_LABEL.equals(mediaType.getSyntax())) {
          DataTypeReference dataType = mediaType.getDataType();
          return super.convertUnwrappedObject(this.jsonContext.findType(dataType));
        }
      }

      return "com.google.gwt.core.client.JavaScriptObject";
    }

    return super.convertUnwrappedObject(unwrapped);
  }

  @Override
  public String convert(HasClientConvertibleType element) throws TemplateModelException {
    TypeMirror adaptingType = this.jsonContext.findAdaptingType(element);
    if (adaptingType != null) {
      return convert(adaptingType);
    }
    else if (element instanceof Element && ((Element) element).getAnnotation(XmlQNameEnumRef.class) != null) {
      return "String";
    }
    else {
      return super.convert(element);
    }
  }

  @Override
  public String convert(TypeElement declaration) throws TemplateModelException {
    if (classConversions.containsKey(declaration.getQualifiedName().toString())) {
      return classConversions.get(declaration.getQualifiedName().toString());
    }
    else if (isCollection(declaration)) {
      return "com.google.gwt.core.client.JsArray";
    }

    TypeMirror adaptingType = jsonContext.findAdaptingType(declaration);
    if (adaptingType != null) {
      return convert(adaptingType);
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
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror, this.context.getProcessingEnvironment());
    if (decorated.isArray()) {
      DecoratedTypeMirror componentType = (DecoratedTypeMirror) ((ArrayType) decorated).getComponentType();
      if (componentType.isPrimitive()) {
        switch (componentType.getKind()) {
          case BOOLEAN:
            return "com.google.gwt.core.client.JsArrayBoolean";
          case BYTE:
            return "java.lang.String";//byte arrays serialized as base64-encoded strings.
          case CHAR:
          case INT:
          case SHORT:
            return "com.google.gwt.core.client.JsArrayInteger";
          case DOUBLE:
          case FLOAT:
          case LONG:
            return "com.google.gwt.core.client.JsArrayNumber";
          default:
            return "com.google.gwt.core.client.JsArray";
        }
      }
      else if (componentType.isInstanceOf(String.class.getName())) {
        return "com.google.gwt.core.client.JsArrayString";
      }
      else {
        return "com.google.gwt.core.client.JsArray<" + convert(componentType) + ">";
      }
    }
    else if (decorated.isCollection()) {
      List<? extends TypeMirror> typeArgs = ((DecoratedDeclaredType) decorated).getTypeArguments();
      if (typeArgs != null && typeArgs.size() == 1) {
        DecoratedTypeMirror componentType = (DecoratedTypeMirror) typeArgs.iterator().next();
        if (componentType.isInstanceOf(String.class.getName())) {
          return "com.google.gwt.core.client.JsArrayString";
        }
        else if (componentType.isInstanceOf(Boolean.class.getName())) {
          return "com.google.gwt.core.client.JsArrayBoolean";
        }
        else if (componentType.isInstanceOf(Integer.class.getName())
          || componentType.isInstanceOf(Character.class.getName())
          || componentType.isInstanceOf(Short.class.getName())) {
          return "com.google.gwt.core.client.JsArrayInteger";
        }
        else if (componentType.isInstanceOf(Double.class.getName())
          || componentType.isInstanceOf(Float.class.getName())
          || componentType.isInstanceOf(Long.class.getName())) {
          return "com.google.gwt.core.client.JsArrayNumber";
        }
        else {
          return "com.google.gwt.core.client.JsArray<" + convert(componentType) + ">";
        }
      }
      return "com.google.gwt.core.client.JsArray";
    }
    else if (decorated.isDeclared()) {
      DeclaredType declaredType = ((DeclaredType) decorated);
      String fqn = ((TypeElement)declaredType.asElement()).getQualifiedName().toString();
      if (classConversions.containsKey(fqn)) {
        return classConversions.get(fqn);
      }
    }
    else if (decorated.isPrimitive()) {
      if (decorated.getKind() == TypeKind.LONG) {
        return Long.class.getName();
      }
    }

    return super.convert(typeMirror);
  }

}