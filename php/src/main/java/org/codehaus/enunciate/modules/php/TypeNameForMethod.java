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

package org.codehaus.enunciate.modules.php;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.*;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.jaxb.Accessor;

import javax.activation.DataHandler;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;

/**
 * Conversion from java types to PHP types.
 *
 * @author Ryan Heaton
 */
public class TypeNameForMethod extends org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod {

  private final Map<String, String> classConversions = new HashMap<String, String>();

  public TypeNameForMethod(Map<String, String> conversions) {
    super(conversions);
    setJdk15(false); //we'll control the generics.

    classConversions.put(Boolean.class.getName(), "boolean");
    classConversions.put(String.class.getName(), "string");
    classConversions.put(Integer.class.getName(), "integer");
    classConversions.put(Short.class.getName(), "integer");
    classConversions.put(Byte.class.getName(), "integer");
    classConversions.put(Double.class.getName(), "double");
    classConversions.put(Long.class.getName(), "integer");
    classConversions.put(java.math.BigInteger.class.getName(), "integer");
    classConversions.put(java.math.BigDecimal.class.getName(), "integer");
    classConversions.put(Float.class.getName(), "double");
    classConversions.put(Character.class.getName(), "string");
    classConversions.put(Date.class.getName(), "integer");
    classConversions.put(Timestamp.class.getName(), "integer");
    classConversions.put(DataHandler.class.getName(), "byte[]");
    classConversions.put(java.awt.Image.class.getName(), "byte[]");
    classConversions.put(javax.xml.transform.Source.class.getName(), "string");
    classConversions.put(QName.class.getName(), "string");
    classConversions.put(URI.class.getName(), "string");
    classConversions.put(UUID.class.getName(), "string");
    classConversions.put(XMLGregorianCalendar.class.getName(), "integer");
    classConversions.put(GregorianCalendar.class.getName(), "integer");
    classConversions.put(Calendar.class.getName(), "integer");
    classConversions.put(javax.xml.datatype.Duration.class.getName(), "string");
    classConversions.put(javax.xml.bind.JAXBElement.class.getName(), "mixed");
    classConversions.put(Object.class.getName(), "mixed");
  }

  @Override
  public String convert(TypeDeclaration declaration) throws TemplateModelException {
    String fqn = declaration.getQualifiedName();
    if (classConversions.containsKey(fqn)) {
      return classConversions.get(fqn);
    }
    else if (declaration instanceof EnumDeclaration) {
      return "string";
    }
    else if (isCollection(declaration) || isMap(declaration)) {
      return "array";
    }
    return "\\" + super.convert(declaration);
  }

  @Override
  public String convert(Accessor accessor) throws TemplateModelException {
    if (accessor.isXmlIDREF()) {
      return "string";
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

  protected boolean isMap(TypeDeclaration declaration) {
    String fqn = declaration.getQualifiedName();
    if (Map.class.getName().equals(fqn)) {
      return true;
    }
    else {
      if (declaration instanceof ClassDeclaration) {
        DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(((ClassDeclaration)declaration).getSuperclass());
        if (decorated.isInstanceOf(Map.class.getName())) {
          return true;
        }
      }

      for (InterfaceType interfaceType : declaration.getSuperinterfaces()) {
        DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(interfaceType);
        if (decorated.isInstanceOf(Map.class.getName())) {
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
          return "boolean";
        case BYTE:
        case INT:
        case SHORT:
        case LONG:
          return "integer";
        case FLOAT:
        case DOUBLE:
          return "double";
        default:
          return "string";
      }
    }
    else if (decorated.isEnum()) {
      return "string";
    }
    else if (decorated.isCollection()) {
      Collection<TypeMirror> actualTypeArguments = ((DeclaredType)decorated).getActualTypeArguments();
      if (actualTypeArguments.size() == 1) {
        String conversion = convert(actualTypeArguments.iterator().next());
        return "mixed".equals(conversion) ? conversion : conversion + "[]";
      }
      else {
        return "array";
      }
    }
    else if (decorated.isArray()) {
      String conversion = convert(((ArrayType) decorated).getComponentType());
      return "mixed".equals(conversion) ? conversion : conversion + "[]";
    }

    return super.convert(typeMirror);
  }

  @Override
  protected String getPackageSeparator() {
    return "\\";
  }
}