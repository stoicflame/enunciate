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

package org.codehaus.enunciate.modules.c;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxb.types.XmlClassType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * Method used to determine a function identifier for a given XML name/namespace.
 *
 * @author Ryan Heaton
 */
public class XmlFunctionIdentifierMethod implements TemplateMethodModelEx {

  /**
   * Returns the qname of the element that has the first parameter as the namespace, the second as the element.
   *
   * @param list The arguments.
   * @return The qname.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The xmlFunctionIdentifier method must have a qname as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);

    if (unwrapped instanceof XmlType) {
      if (unwrapped instanceof XmlClassType && ((XmlType)unwrapped).isAnonymous()) {
        unwrapped = ((XmlClassType) unwrapped).getTypeDefinition();
      }
      else {
        unwrapped = ((XmlType) unwrapped).getQname();
      }
    }

    if (unwrapped instanceof TypeDefinition) {
      if (((TypeDefinition) unwrapped).isAnonymous()) {
        unwrapped = new QName(((TypeDefinition)unwrapped).getNamespace(), "anonymous" + ((TypeDefinition)unwrapped).getSimpleName());
      }
      else {
        unwrapped = ((TypeDefinition) unwrapped).getQname();
      }
    }
    
    if (!(unwrapped instanceof QName)) {
      throw new TemplateModelException("The xmlFunctionIdentifier method must have a qname, type definition, or xml type as a parameter.");
    }

    QName qname = (QName) unwrapped;
    String namespace = qname.getNamespaceURI();
    if ("".equals(namespace)) {
      namespace = null;
    }
    
    String prefix = lookupPrefix(namespace).replace('-', '_');
    if (prefix == null) {
      throw new TemplateModelException("No prefix specified for {" + namespace + "}");
    }

    String localName = qname.getLocalPart();
    if ("".equals(localName)) {
      localName = "anonymous";
    }
    localName = localName.replace('-', '_');
    StringBuilder identifier = new StringBuilder();
    identifier.append(Character.toLowerCase(prefix.charAt(0)));
    identifier.append(prefix.substring(1));
    identifier.append(Character.toUpperCase(localName.charAt(0)));
    identifier.append(localName.substring(1));
    return identifier.toString();
  }

  /**
   * Convenience method to lookup a namespace prefix given a namespace.
   *
   * @param namespace The namespace for which to lookup the prefix.
   * @return The namespace prefix.
   */
  protected String lookupPrefix(String namespace) {
    return getNamespacesToPrefixes().get(namespace);
  }

  /**
   * The namespace to prefix map.
   *
   * @return The namespace to prefix map.
   */
  protected static Map<String, String> getNamespacesToPrefixes() {
    return getModel().getNamespacesToPrefixes();
  }

  /**
   * Get the current root model.
   *
   * @return The current root model.
   */
  protected static EnunciateFreemarkerModel getModel() {
    return ((EnunciateFreemarkerModel) FreemarkerModel.get());
  }

}