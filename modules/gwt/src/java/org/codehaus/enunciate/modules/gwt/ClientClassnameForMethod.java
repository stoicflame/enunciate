/*
 * Copyright 2006 Web Cohesion
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

import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.EnumType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;

import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;

import freemarker.template.TemplateModelException;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends org.codehaus.enunciate.template.freemarker.ClientClassnameForMethod {

  private final Map<String, String> classConversions = new HashMap<String, String>();

  public ClientClassnameForMethod(Map<String, String> conversions) {
    super(conversions);
    setJdk15(false);
    classConversions.put(BigDecimal.class.getName(), String.class.getName());
    classConversions.put(BigInteger.class.getName(), String.class.getName());
    classConversions.put(Calendar.class.getName(), Date.class.getName());
    classConversions.put(DataHandler.class.getName(), "byte[]");
    classConversions.put(QName.class.getName(), String.class.getName());
    classConversions.put(URI.class.getName(), String.class.getName());
    classConversions.put(UUID.class.getName(), String.class.getName());
    classConversions.put(XMLGregorianCalendar.class.getName(), Date.class.getName());
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    if (typeMirror instanceof DeclaredType) {
      DeclaredType declaredType = ((DeclaredType) typeMirror);
      String fqn = declaredType.getDeclaration().getQualifiedName();
      if (classConversions.containsKey(fqn)) {
        return classConversions.get(fqn);
      }
    }

    if (typeMirror instanceof EnumType) {
      return String.class.getName();
    }

    return super.convert(typeMirror);
  }

  @Override
  public String convert(TypeDeclaration declaration) {
    if (classConversions.containsKey(declaration.getQualifiedName())) {
      return classConversions.get(declaration.getQualifiedName());
    }
    
    return declaration instanceof EnumDeclaration ? String.class.getName() : super.convert(declaration);
  }
}
