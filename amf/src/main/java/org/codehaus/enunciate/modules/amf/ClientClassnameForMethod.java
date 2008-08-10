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

package org.codehaus.enunciate.modules.amf;

import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.TypeMirror;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;
import java.net.URI;

/**
 * Conversion from java types to AS3 types.
 *
 * @link http://livedocs.adobe.com/flex/2/docs/wwhelp/wwhimpl/common/html/wwhelp.htm?context=LiveDocs_Parts&file=00001104.html#270405
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod {

  private final Map<String, String> classConversions = new HashMap<String, String>();

  public ClientClassnameForMethod(Map<String, String> conversions) {
    super(conversions);
    setJdk15(false);
    classConversions.put(Boolean.class.getName(), "Boolean");
    classConversions.put(String.class.getName(), "String");
    classConversions.put(Integer.class.getName(), "int");
    classConversions.put(Short.class.getName(), "int");
    classConversions.put(Byte.class.getName(), "int");
    classConversions.put(Double.class.getName(), "Number");
    classConversions.put(Long.class.getName(), "Number");
    classConversions.put(Float.class.getName(), "Number");
    classConversions.put(Character.class.getName(), "String");
    classConversions.put(Date.class.getName(), "Date");
    classConversions.put(DataHandler.class.getName(), "flash.utils.ByteArray");
    classConversions.put(QName.class.getName(), "String");
    classConversions.put(URI.class.getName(), "String");
    classConversions.put(UUID.class.getName(), "String");
    classConversions.put(XMLGregorianCalendar.class.getName(), "Date");
    classConversions.put(Calendar.class.getName(), "Date");
    classConversions.put(Object.class.getName(), "Object");
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror);
    if (decorated.isPrimitive()) {
      switch (((PrimitiveType)decorated).getKind()) {
        case BOOLEAN:
          return "Boolean";
        case BYTE:
          return "int";
        case CHAR:
          return "String";
        case DOUBLE:
          return "Number";
        case FLOAT:
          return "Number";
        case INT:
          return "int";
        case LONG:
          return "Number";
        case SHORT:
          return "int";
      }
    }
    else if (decorated.isArray()) {
      ArrayType arrayType = (ArrayType) decorated;
      DecoratedTypeMirror decoratedComponentType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(arrayType.getComponentType());
      if (((decoratedComponentType.isPrimitive()) && (((PrimitiveType) decoratedComponentType).getKind() == PrimitiveType.Kind.CHAR))
        || (decoratedComponentType.isInstanceOf(Character.class.getName()))) {
        return "String";
      }
      else if (((decoratedComponentType.isPrimitive()) && (((PrimitiveType) decoratedComponentType).getKind() == PrimitiveType.Kind.BYTE))
        || (decoratedComponentType.isInstanceOf(Byte.class.getName()))) {
        return "flash.utils.ByteArray";
      }
      return "Array";
    }
    else if (decorated.isCollection()) {
      return "mx.collections.ArrayCollection";
    }
    else if (decorated.isInstanceOf("java.util.Map")) {
      return "Object";
    }
    else if (decorated.isEnum()) {
      return "String";
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
