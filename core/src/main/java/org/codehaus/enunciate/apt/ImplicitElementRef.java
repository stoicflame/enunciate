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

package org.codehaus.enunciate.apt;

import org.codehaus.enunciate.contract.jaxb.ImplicitSchemaElement;
import org.codehaus.enunciate.contract.jaxb.Element;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxb.types.XmlClassType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;

import javax.xml.namespace.QName;

/**
 * An implicit element reference.
 *
 * @author Ryan Heaton
 */
public class ImplicitElementRef implements ImplicitSchemaElement {

  protected final Element element;

  public ImplicitElementRef(Element element) {
    this.element = element;
  }

  public String getElementName() {
    return element.getName();
  }

  public String getTargetNamespace() {
    return element.getNamespace();
  }

  public String getElementDocs() {
    return element.getJavaDoc() != null ? element.getJavaDoc().toString() : null;
  }

  public QName getTypeQName() {
    XmlType baseType = element.getBaseType();
    if (baseType.isAnonymous()) {
      return null;
    }
    else {
      return baseType.getQname();
    }
  }

  public TypeDefinition getAnonymousTypeDefinition() {
    XmlType baseType = element.getBaseType();
    if ((baseType.isAnonymous()) && (baseType instanceof XmlClassType)) {
      return ((XmlClassType) baseType).getTypeDefinition();
    }

    return null;
  }
}
