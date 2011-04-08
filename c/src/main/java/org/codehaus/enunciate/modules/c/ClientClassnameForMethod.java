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

package org.codehaus.enunciate.modules.c;

import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.*;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.jaxb.Accessor;

import javax.activation.DataHandler;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.*;

/**
 * Conversion from java types to Ruby types.
 *
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod {

  private final Map<String, String> classConversions = new HashMap<String, String>();

  public ClientClassnameForMethod(Map<String, String> conversions) {
    super(conversions);
    setJdk15(false); //we'll control the generics.

    classConversions.put(Boolean.class.getName(), "int");
    classConversions.put(String.class.getName(), "xmlChar");
    classConversions.put(Integer.class.getName(), "int");
    classConversions.put(Short.class.getName(), "short");
    classConversions.put(Byte.class.getName(), "unsigned char");
    classConversions.put(Double.class.getName(), "double");
    classConversions.put(Long.class.getName(), "long");
    classConversions.put(java.math.BigInteger.class.getName(), "xmlChar");
    classConversions.put(java.math.BigDecimal.class.getName(), "xmlChar");
    classConversions.put(Float.class.getName(), "float");
    classConversions.put(Character.class.getName(), "xmlChar");
    classConversions.put(Date.class.getName(), "struct tm");
    classConversions.put(DataHandler.class.getName(), "unsigned char");
    classConversions.put(java.awt.Image.class.getName(), "unsigned char");
    classConversions.put(javax.xml.transform.Source.class.getName(), "unsigned char");
    classConversions.put(QName.class.getName(), "struct QName");
    classConversions.put(URI.class.getName(), "xmlChar");
    classConversions.put(UUID.class.getName(), "xmlChar");
    classConversions.put(XMLGregorianCalendar.class.getName(), "struct tm");
    classConversions.put(GregorianCalendar.class.getName(), "struct tm");
    classConversions.put(Calendar.class.getName(), "struct tm");
    classConversions.put(javax.xml.datatype.Duration.class.getName(), "xmlChar");
    classConversions.put(javax.xml.bind.JAXBElement.class.getName(), "struct xmlBasicNode");
    classConversions.put(Object.class.getName(), "struct xmlBasicNode");
    classConversions.putAll(conversions);
  }

  @Override
  public String convert(TypeDeclaration declaration) throws TemplateModelException {
    String fqn = declaration.getQualifiedName();
    if (classConversions.containsKey(fqn)) {
      return classConversions.get(fqn);
    }
    else if (isCollection(declaration)) {
      return "xmlNode";
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
  public String convert(Accessor accessor) throws TemplateModelException {
    if (accessor.isXmlIDREF()) {
      return "xmlChar";
    }
    else if (accessor.isXmlList()) {
      return "xmlChar";
    }
    
    return super.convert(accessor);
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror);
    if (decorated.isPrimitive()) {
      PrimitiveType.Kind kind = ((PrimitiveType) decorated).getKind();
      switch (kind) {
        case BOOLEAN:
          return "int";
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
          return "long";
        default:
          return "xmlChar";
      }
    }
    else if (decorated.isCollection()) {
      if (decorated instanceof DeclaredType) {
        Collection<TypeMirror> typeArgs = ((DeclaredType) typeMirror).getActualTypeArguments();
        if (typeArgs.size() == 1) {
          return convert(typeArgs.iterator().next());
        }
      }
      return "xmlNode";
    }
    else if (decorated.isArray()) {
      TypeMirror componentType = ((ArrayType) decorated).getComponentType();
      if ((componentType instanceof PrimitiveType) && (((PrimitiveType) componentType).getKind() == PrimitiveType.Kind.BYTE)) {
        return "unsigned char";
      }

      return convert(componentType);
    }

    return super.convert(typeMirror);
  }
}