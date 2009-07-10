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

package org.codehaus.enunciate.modules.ruby;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.ArrayType;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import javax.activation.DataHandler;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.*;

import org.codehaus.enunciate.contract.jaxb.Accessor;

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

    classConversions.put(Boolean.class.getName(), "Boolean");
    classConversions.put(String.class.getName(), "String");
    classConversions.put(Integer.class.getName(), "Fixnum");
    classConversions.put(Short.class.getName(), "Fixnum");
    classConversions.put(Byte.class.getName(), "Fixnum");
    classConversions.put(Double.class.getName(), "Float");
    classConversions.put(Long.class.getName(), "Bignum");
    classConversions.put(java.math.BigInteger.class.getName(), "Bignum");
    classConversions.put(java.math.BigDecimal.class.getName(), "Float");
    classConversions.put(Float.class.getName(), "Float");
    classConversions.put(Character.class.getName(), "String");
    classConversions.put(Date.class.getName(), "Time");
    classConversions.put(DataHandler.class.getName(), "String");
    classConversions.put(java.awt.Image.class.getName(), "String");
    classConversions.put(javax.xml.transform.Source.class.getName(), "String");
    classConversions.put(QName.class.getName(), "String");
    classConversions.put(URI.class.getName(), "String");
    classConversions.put(UUID.class.getName(), "String");
    classConversions.put(XMLGregorianCalendar.class.getName(), "String");
    classConversions.put(GregorianCalendar.class.getName(), "Time");
    classConversions.put(Calendar.class.getName(), "Time");
    classConversions.put(javax.xml.datatype.Duration.class.getName(), "String");
    classConversions.put(javax.xml.bind.JAXBElement.class.getName(), "Object");
    classConversions.put(Object.class.getName(), "Object");
  }

  @Override
  public String convert(TypeDeclaration declaration) throws TemplateModelException {
    String fqn = declaration.getQualifiedName();
    if (classConversions.containsKey(fqn)) {
      return classConversions.get(fqn);
    }
    else if (declaration instanceof EnumDeclaration) {
      return "String";
    }
    else if (isCollection(declaration)) {
      return "Array";
    }

    return super.convert(declaration);
  }

  @Override
  public String convert(Accessor accessor) throws TemplateModelException {
    if (accessor.isXmlIDREF()) {
      return "String";
    }
    
    return super.convert(accessor);
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
    if (decorated.isPrimitive()) {
      PrimitiveType.Kind kind = ((PrimitiveType) decorated).getKind();
      switch (kind) {
        case BOOLEAN:
          return "Boolean";
        case BYTE:
        case INT:
        case SHORT:
          return "Fixnum";
        case FLOAT:
        case DOUBLE:
          return "Float";
        case LONG:
          return "Bignum";
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
      if ((componentType instanceof PrimitiveType) && (((PrimitiveType) componentType).getKind() == PrimitiveType.Kind.BYTE)) {
        return "String";
      }
    }

    return super.convert(typeMirror);
  }

  @Override
  protected String getPackageSeparator() {
    return "::";
  }
}