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

package org.codehaus.enunciate.modules.objc;

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

    classConversions.put(Boolean.class.getName(), "BOOL");
    classConversions.put(String.class.getName(), "NSString");
    classConversions.put(Integer.class.getName(), "int");
    classConversions.put(Short.class.getName(), "short");
    classConversions.put(Byte.class.getName(), "unsigned char");
    classConversions.put(Double.class.getName(), "double");
    classConversions.put(Long.class.getName(), "long");
    classConversions.put(java.math.BigInteger.class.getName(), "NSNumber");
    classConversions.put(java.math.BigDecimal.class.getName(), "NSDecimalNumber");
    classConversions.put(Float.class.getName(), "float");
    classConversions.put(Character.class.getName(), "xmlChar");
    classConversions.put(Date.class.getName(), "NSDate");
    classConversions.put(DataHandler.class.getName(), "NSData");
    classConversions.put(java.awt.Image.class.getName(), "NSData");
    classConversions.put(javax.xml.transform.Source.class.getName(), "NSData");
    classConversions.put(QName.class.getName(), "NSString");
    classConversions.put(URI.class.getName(), "NSURL");
    classConversions.put(UUID.class.getName(), "NSString");
    classConversions.put(XMLGregorianCalendar.class.getName(), "NSCalendarDate");
    classConversions.put(GregorianCalendar.class.getName(), "NSCalendarDate");
    classConversions.put(Calendar.class.getName(), "NSCalendarDate");
    classConversions.put(javax.xml.datatype.Duration.class.getName(), "NSString");
    classConversions.put(javax.xml.bind.JAXBElement.class.getName(), "JAXBBasicXMLNode");
    classConversions.put(Object.class.getName(), "NSObject");
    classConversions.putAll(conversions);
  }

  @Override
  public String convert(TypeDeclaration declaration) throws TemplateModelException {
    String fqn = declaration.getQualifiedName();
    if (classConversions.containsKey(fqn)) {
      return classConversions.get(fqn);
    }
    else if (isCollection(declaration)) {
      return "NSArray";
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
    if (accessor.isXmlList()) {
      return "NSString";
    }
    else if (accessor.isXmlIDREF() && !accessor.isCollectionType()) {
      return "NSString";
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
          return "long";
        default:
          return "NSString";
      }
    }
    else if (decorated.isCollection()) {
      return "NSArray";
    }
    else if (decorated.isArray()) {
      TypeMirror componentType = ((ArrayType) decorated).getComponentType();
      if (componentType instanceof PrimitiveType) {
        if (((PrimitiveType) componentType).getKind() == PrimitiveType.Kind.BYTE) {
          return "NSData";
        }
      }

      return "NSArray";
    }

    return super.convert(typeMirror);
  }
}