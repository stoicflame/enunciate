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

package org.codehaus.enunciate.modules.swagger;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlClassType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxrs.ResourceEntityParameter;
import org.codehaus.enunciate.contract.jaxrs.ResourceRepresentationMetadata;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Template method used to determine the objective-c "simple name" of an accessor.
 *
 * @author Ryan Heaton
 */
public class DatatypeNameForMethod implements TemplateMethodModelEx {

  private final String defaultNamespace;
  private final EnunciateFreemarkerModel model;

  public DatatypeNameForMethod(EnunciateFreemarkerModel model, String defaultNamespace) {
    this.model = model;
    this.defaultNamespace = "".equals(defaultNamespace) ? null : defaultNamespace;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The datatypeNameFor method must have a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    QName qname = KnownXmlType.STRING.getQname();
    String name;
    if (unwrapped instanceof XmlType) {
      XmlType xmlType = (XmlType) unwrapped;
      if (xmlType instanceof XmlClassType && ((XmlClassType)xmlType).getTypeDefinition().isEnum()) {
        qname = KnownXmlType.STRING.getQname();
      }
      else {
        qname = xmlType.getQname();
      }
    }
    else if (unwrapped instanceof ResourceEntityParameter || unwrapped instanceof ResourceRepresentationMetadata) {
      ElementDeclaration xmlElement = (unwrapped instanceof ResourceEntityParameter) ? ((ResourceEntityParameter) unwrapped).getXmlElement() : ((ResourceRepresentationMetadata)unwrapped).getXmlElement();
      if (xmlElement instanceof RootElementDeclaration) {
        qname = new QName(((RootElementDeclaration)xmlElement).getTypeDefinition().getNamespace(), ((RootElementDeclaration)xmlElement).getTypeDefinition().getName());
      }
      else if (xmlElement instanceof LocalElementDeclaration) {
        qname = ((LocalElementDeclaration) xmlElement).getElementXmlType().getQname();
      }
    }
    else if (unwrapped instanceof TypeDefinition) {
      qname = ((TypeDefinition) unwrapped).isEnum() ? KnownXmlType.STRING.getQname() : ((TypeDefinition) unwrapped).getQname();
    }
    else if (unwrapped instanceof Accessor) {
      Accessor accessor = (Accessor) unwrapped;
      QName elementRef = accessor.getRef();
      if (elementRef != null) {
        REF_LOOP : for (SchemaInfo schemaInfo : model.getNamespacesToSchemas().values()) {
          for (RootElementDeclaration elementDecl : schemaInfo.getGlobalElements()) {
            if (elementRef.equals(elementDecl.getQname())) {
              qname = elementDecl.getTypeDefinition().getQname();
              break REF_LOOP;
            }
          }

          for (LocalElementDeclaration elementDeclaration : schemaInfo.getLocalElementDeclarations()) {
            if (elementRef.equals(elementDeclaration.getQname())) {
              qname = elementDeclaration.getElementXmlType().getQname();
              break REF_LOOP;
            }
          }
        }
      }
      else {
        qname = accessor.getBaseType().getQname();
      }
    }

    if (qname != null) {
      if (KnownXmlType.STRING.getQname().equals(qname)) {
        name = "string";
      }
      else if (KnownXmlType.BOOLEAN.getQname().equals(qname)) {
        name = "boolean";
      }
      else if (KnownXmlType.INT.getQname().equals(qname)) {
        name = "int";
      }
      else if (KnownXmlType.INTEGER.getQname().equals(qname)) {
        name = "int";
      }
      else if (KnownXmlType.LONG.getQname().equals(qname)) {
        name = "long";
      }
      else if (KnownXmlType.FLOAT.getQname().equals(qname)) {
        name = "float";
      }
      else if (KnownXmlType.DOUBLE.getQname().equals(qname)) {
        name = "double";
      }
      else if (KnownXmlType.DATE.getQname().equals(qname)) {
        name = "Date";
      }
      else if (KnownXmlType.DATE_TIME.getQname().equals(qname)) {
        name = "Date";
      }
      else if (KnownXmlType.TIME.getQname().equals(qname)) {
        name = "Date";
      }
      else {
        name = qname.getLocalPart();

        String ns = qname.getNamespaceURI();
        if ("".equals(ns)) {
          ns = null;
        }

        if ((this.defaultNamespace != null && this.defaultNamespace.equals(ns)) || (defaultNamespace == null && ns == null)) {
          String prefix = this.model.getNamespacesToPrefixes().get(ns);
          if (prefix == null) {
            prefix = "";
          }

          name = prefix + "_" + name;
        }
      }
    }
    else {
      throw new TemplateModelException("Unknown parameter type.");
    }

    return name;
  }
}