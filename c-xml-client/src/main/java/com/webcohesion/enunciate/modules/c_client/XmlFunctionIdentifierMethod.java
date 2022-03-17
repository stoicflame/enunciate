/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.c_client;

import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.Element;
import com.webcohesion.enunciate.modules.jaxb.model.ElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.types.*;
import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * Method used to determine a function identifier for a given XML name/namespace.
 *
 * @author Ryan Heaton
 */
public class XmlFunctionIdentifierMethod implements TemplateMethodModelEx {

  private final Map<String, String> ns2prefix;

  public XmlFunctionIdentifierMethod(Map<String, String> ns2prefix) {
    this.ns2prefix = ns2prefix;
  }

  /**
   * Returns the qname of the element that has the first parameter as the namespace, the second as the element.
   *
   * @param list The arguments.
   * @return The qname.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The xmlFunctionIdentifier method must have a qname, type definition, or xml type as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = FreemarkerUtil.unwrap(from);

    if (unwrapped instanceof Accessor) {
      DecoratedTypeMirror accessorType = ((Accessor) unwrapped).getBareAccessorType();

      if (accessorType.isInstanceOf(JAXBElement.class.getName())) {
        unwrapped = KnownXmlType.ANY_TYPE.getQname();
      }
      else if (unwrapped instanceof Element && ((Element) unwrapped).getRef() != null) {
        unwrapped = ((Element) unwrapped).getRef();
      }
      else {
        XmlType xmlType = ((Accessor) unwrapped).getBaseType();
        if (xmlType instanceof SpecifiedXmlType) {
          //bypass specified types for client code generation.
          unwrapped = XmlTypeFactory.getXmlType(((Accessor) unwrapped).getAccessorType(), ((Accessor) unwrapped).getContext());
        } else {
          unwrapped = xmlType;
        }
      }
    }

    if (unwrapped instanceof XmlType) {
      if (unwrapped instanceof XmlClassType && ((XmlType) unwrapped).isAnonymous()) {
        unwrapped = ((XmlClassType) unwrapped).getTypeDefinition();
      }
      else {
        unwrapped = ((XmlType) unwrapped).getQname();
      }
    }

    if (unwrapped instanceof TypeDefinition) {
      if (((TypeDefinition) unwrapped).isAnonymous()) {
        //if anonymous, we have to come up with a unique (albeit nonstandard) name for the xml type.
        unwrapped = new QName(((TypeDefinition) unwrapped).getNamespace(), "anonymous" + ((TypeDefinition) unwrapped).getSimpleName());
      }
      else {
        unwrapped = ((TypeDefinition) unwrapped).getQname();
      }
    }

    if (unwrapped instanceof ElementDeclaration) {
      unwrapped = ((ElementDeclaration) unwrapped).getQname();
    }

    if (!(unwrapped instanceof QName)) {
      throw new TemplateModelException("The xmlFunctionIdentifier method must have a qname, type definition, or xml type as a parameter.");
    }

    QName qname = (QName) unwrapped;
    String namespace = qname.getNamespaceURI();
    if ("".equals(namespace)) {
      namespace = null;
    }

    String prefix = this.ns2prefix.get(namespace);
    if (prefix == null || prefix.isEmpty()) {
      prefix = "_";
    }
    prefix = prefix.replace('-', '_');

    String localName = qname.getLocalPart();
    if ("".equals(localName)) {
      return null;
    }
    StringBuilder identifier = new StringBuilder();
    identifier.append(Character.toLowerCase(prefix.charAt(0)));
    identifier.append(prefix.substring(1));
    identifier.append(Character.toUpperCase(localName.charAt(0)));
    identifier.append(localName.substring(1));
    return CXMLClientModule.scrubIdentifier(identifier.toString());
  }

}