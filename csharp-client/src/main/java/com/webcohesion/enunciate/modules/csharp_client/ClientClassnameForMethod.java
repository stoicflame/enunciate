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

package com.webcohesion.enunciate.modules.csharp_client;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.util.HasClientConvertibleType;
import freemarker.template.TemplateModelException;

import javax.activation.DataHandler;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.xml.namespace.QName;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.util.*;
import java.net.URI;

/**
 * Conversion from java types to C# types.
 *
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends com.webcohesion.enunciate.util.freemarker.ClientClassnameForMethod {

  private final Map<String, String> classConversions = new HashMap<String, String>();

  public ClientClassnameForMethod(Map<String, String> conversions, EnunciateContext context) {
    super(conversions, context);

    classConversions.put(Boolean.class.getName(), "bool?");
    classConversions.put(String.class.getName(), "string");
    classConversions.put(Integer.class.getName(), "int?");
    classConversions.put(Short.class.getName(), "short?");
    classConversions.put(Byte.class.getName(), "sbyte?");
    classConversions.put(Double.class.getName(), "double?");
    classConversions.put(Long.class.getName(), "long?");
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
    classConversions.put(javax.xml.bind.JAXBElement.class.getName(), "object");
    classConversions.put(Object.class.getName(), "object");
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

    return super.convert(declaration);
  }

  @Override
  public String convert(HasClientConvertibleType element) throws TemplateModelException {
    if (element instanceof Accessor && ((Accessor)element).isXmlIDREF()) {
      return "string";//C# doesn't support strict object reference resolution via IDREF.  The best we can do is (de)serialize the ID.
    }

    return super.convert(element);
  }

  protected boolean isCollection(TypeElement declaration) {
    String fqn = declaration.getQualifiedName().toString();
    if (Collection.class.getName().equals(fqn)) {
      return true;
    }
    else if (Object.class.getName().equals(fqn)) {
      return false;
    }
    else {
      DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(declaration.getSuperclass(), context.getProcessingEnvironment());
      if (decorated.isCollection()) {
        return true;
      }

      for (TypeMirror interfaceType : declaration.getInterfaces()) {
        decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(interfaceType, context.getProcessingEnvironment());
        if (decorated.isCollection()) {
          return true;
        }
      }
    }
    
    return false;
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
