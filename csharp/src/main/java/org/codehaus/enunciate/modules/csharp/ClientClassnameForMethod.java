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

package org.codehaus.enunciate.modules.csharp;

import com.sun.mirror.type.*;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;
import java.net.URI;

import org.codehaus.enunciate.contract.jaxb.Accessor;

/**
 * Conversion from java types to C# types.
 *
 * @link http://livedocs.adobe.com/flex/2/docs/wwhelp/wwhimpl/common/html/wwhelp.htm?context=LiveDocs_Parts&file=00001104.html#270405
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod {

  private final Map<String, String> classConversions = new HashMap<String, String>();

  public ClientClassnameForMethod(Map<String, String> conversions) {
    super(conversions);
    setJdk15(false); //we'll control the generics.

    classConversions.put(Boolean.class.getName(), "Boolean");
    classConversions.put(String.class.getName(), "string");
    classConversions.put(Integer.class.getName(), "Int32");
    classConversions.put(Short.class.getName(), "Int16");
    classConversions.put(Byte.class.getName(), "Byte");
    classConversions.put(Double.class.getName(), "Double");
    classConversions.put(Long.class.getName(), "Int64");
    classConversions.put(java.math.BigInteger.class.getName(), "Int64");
    classConversions.put(java.math.BigDecimal.class.getName(), "Decimal");
    classConversions.put(Float.class.getName(), "float");
    classConversions.put(Character.class.getName(), "Char");
    classConversions.put(Date.class.getName(), "System.DateTime");
    classConversions.put(DataHandler.class.getName(), "byte[]");
    classConversions.put(java.awt.Image.class.getName(), "byte[]");
    classConversions.put(javax.xml.transform.Source.class.getName(), "byte[]");
    classConversions.put(QName.class.getName(), "System.Xml.XmlQualifiedName");
    classConversions.put(URI.class.getName(), "string");
    classConversions.put(UUID.class.getName(), "string");
    classConversions.put(XMLGregorianCalendar.class.getName(), "System.DateTime");
    classConversions.put(GregorianCalendar.class.getName(), "System.DateTime");
    classConversions.put(Calendar.class.getName(), "System.DateTime");
    classConversions.put(javax.xml.datatype.Duration.class.getName(), "TimeSpan");
    classConversions.put(Object.class.getName(), "object");
  }

  @Override
  public String convert(TypeDeclaration declaration) throws TemplateModelException {
    String fqn = declaration.getQualifiedName();
    if (classConversions.containsKey(fqn)) {
      return classConversions.get(fqn);
    }
    else if (isCollection(declaration)) {
      return "System.Collections.ArrayList";
    }

    return super.convert(declaration);
  }

  @Override
  public String convert(Accessor accessor) throws TemplateModelException {
    if (accessor.isXmlIDREF()) {
      return "string";//C# doesn't support strict object reference resolution via IDREF.  The best we can do is (de)serialize the ID.
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
          return "bool"; //boolean as 'bool'
        default:
          return kind.toString().toLowerCase();
      }
    }
    else if (decorated.isCollection()) {
      //collections will be converted to arrays.
      DeclaredType declaredType = (DeclaredType) typeMirror;
      Collection<TypeMirror> actualTypeArguments = declaredType.getActualTypeArguments();
      if (actualTypeArguments.size() == 1) {
        return convert(actualTypeArguments.iterator().next()) + "[]";
      }
    }

    return super.convert(typeMirror);
  }

}