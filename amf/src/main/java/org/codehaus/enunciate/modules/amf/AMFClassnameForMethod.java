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

import com.sun.mirror.type.*;
import freemarker.template.TemplateModelException;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.Date;
import java.net.URI;

import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.activation.DataHandler;

/**
 * @author Ryan Heaton
 */
public class AMFClassnameForMethod extends org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod {

  private final Map<String, String> classConversions = new HashMap<String, String>();

  public AMFClassnameForMethod(Map<String, String> conversions) {
    super(conversions);
    this.classConversions.put(UUID.class.getName(), String.class.getName());
    this.classConversions.put(XMLGregorianCalendar.class.getName(), Date.class.getName());
    this.classConversions.put(QName.class.getName(), String.class.getName());
    this.classConversions.put(URI.class.getName(), String.class.getName());
    this.classConversions.put(DataHandler.class.getName(), "byte[]");
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror);
    if ((typeMirror instanceof ArrayType) && (((ArrayType) typeMirror).getComponentType() instanceof PrimitiveType)) {
      //special case for primitive arrays.
      return super.convert(((ArrayType) typeMirror).getComponentType()) + "[]";
    }
    else if (decorated.isEnum()) {
      return String.class.getName();
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
