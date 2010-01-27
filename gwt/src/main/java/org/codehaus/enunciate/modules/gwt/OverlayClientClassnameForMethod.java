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

package org.codehaus.enunciate.modules.gwt;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.*;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.decorations.type.DecoratedDeclaredType;

import javax.activation.DataHandler;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.*;

/**
 * Conversion from java types to C# types.
 *
 * @link http://livedocs.adobe.com/flex/2/docs/wwhelp/wwhimpl/common/html/wwhelp.htm?context=LiveDocs_Parts&file=00001104.html#270405
 * @author Ryan Heaton
 */
public class OverlayClientClassnameForMethod extends org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod {

  private final Map<String, String> classConversions = new HashMap<String, String>();

  public OverlayClientClassnameForMethod(Map<String, String> conversions) {
    super(conversions);
    classConversions.put(BigDecimal.class.getName(), String.class.getName());
    classConversions.put(BigInteger.class.getName(), String.class.getName());
    classConversions.put(Calendar.class.getName(), Integer.class.getName());
    classConversions.put(DataHandler.class.getName(), String.class.getName());
    classConversions.put(QName.class.getName(), String.class.getName());
    classConversions.put(URI.class.getName(), String.class.getName());
    classConversions.put(UUID.class.getName(), String.class.getName());
    classConversions.put(XMLGregorianCalendar.class.getName(), Integer.class.getName());
    classConversions.put(javax.xml.bind.JAXBElement.class.getName(), "com.google.gwt.core.client.JavaScriptObject");
  }

  @Override
  public String convert(TypeDeclaration declaration) throws TemplateModelException {
    if (classConversions.containsKey(declaration.getQualifiedName())) {
      return classConversions.get(declaration.getQualifiedName());
    }
    else if (isCollection(declaration)) {
      return "com.google.gwt.core.client.JsArray";
    }

    return super.convert(declaration);
  }

  protected boolean isCollection(TypeDeclaration declaration) {
    String fqn = declaration.getQualifiedName();
    if (Collection.class.getName().equals(fqn)) {
      return true;
    }
    else if (Object.class.getName().equals(fqn)) {
      return false;
    }
    else {
      if (declaration instanceof ClassDeclaration) {
        DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(((ClassDeclaration)declaration).getSuperclass());
        if (decorated.isCollection()) {
          return true;
        }
      }

      for (InterfaceType interfaceType : declaration.getSuperinterfaces()) {
        DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(interfaceType);
        if (decorated.isCollection()) {
          return true;
        }
      }
    }

    return false;
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror);
    if (decorated.isArray()) {
      DecoratedTypeMirror componentType = (DecoratedTypeMirror) ((ArrayType) decorated).getComponentType();
      if (componentType.isPrimitive()) {
        switch (((PrimitiveType)componentType).getKind()) {
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
        return "com.google.gwt.core.client.JsArray<" + super.convert(componentType) + ">";
      }
    }
    else if (decorated.isCollection()) {
      Collection<TypeMirror> typeArgs = ((DecoratedDeclaredType) decorated).getActualTypeArguments();
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
          return "com.google.gwt.core.client.JsArray<" + super.convert(componentType) + ">";
        }
      }
      return "com.google.gwt.core.client.JsArray";
    }
    else if (decorated.isDeclared()) {
      DeclaredType declaredType = ((DeclaredType) decorated);
      String fqn = declaredType.getDeclaration().getQualifiedName();
      if (classConversions.containsKey(fqn)) {
        return classConversions.get(fqn);
      }
    }

    return super.convert(typeMirror);
  }

}