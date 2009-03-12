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
    classConversions.put(Character.class.getName(), "char?");
    classConversions.put(Date.class.getName(), "DateTime?");
    classConversions.put(DataHandler.class.getName(), "byte[]");
    classConversions.put(java.awt.Image.class.getName(), "byte[]");
    classConversions.put(javax.xml.transform.Source.class.getName(), "byte[]");
    classConversions.put(QName.class.getName(), "System.Xml.XmlQualifiedName");
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
    else if (decorated.isEnum()) {
      return super.convert(typeMirror) + "?";
    }
    else if (decorated.isCollection()) {
      //collections will be converted to arrays.
      return getCollectionTypeConversion((DeclaredType) typeMirror);
    }

    return super.convert(typeMirror);
  }

  protected String getCollectionTypeConversion(DeclaredType declaredType) throws TemplateModelException {
    Collection<TypeMirror> actualTypeArguments = declaredType.getActualTypeArguments();
    if (actualTypeArguments.size() == 1) {
      return "System.Collections.Generic.List<" + convert(actualTypeArguments.iterator().next()) + ">";
    }
    else {
      return "System.Collections.ArrayList";
    }
  }

}