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

package org.codehaus.enunciate.template.freemarker;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.contract.jaxb.ImplicitSchemaElement;
import org.codehaus.enunciate.contract.jaxb.LocalElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;

import java.util.List;
import java.util.Map;

/**
 * A method used in templates to output the prefix for a given namespace.
 *
 * @author Ryan Heaton
 */
public class IsDefinedGloballyMethod implements TemplateMethodModelEx {

  /**
   * Returns the qname of the element that has the first parameter as the namespace, the second as the element.
   *
   * @param list The arguments.
   * @return The qname.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The isDefinedGlobally method must have a local element declaration as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    String namespace;
    String name;
    if (LocalElementDeclaration.class.isInstance(unwrapped)) {
      LocalElementDeclaration decl = (LocalElementDeclaration) unwrapped;
      namespace = decl.getNamespace();
      name = decl.getName();
    }
    else if (ImplicitSchemaElement.class.isInstance(unwrapped)) {
      ImplicitSchemaElement ise = (ImplicitSchemaElement) unwrapped;
      namespace = ise.getTargetNamespace();
      name = ise.getElementName();
    }
    else {
      throw new TemplateModelException("The isDefinedGlobally method must have a local element declaration or an implicit schema element as a parameter.");
    }

    SchemaInfo schemaInfo = getModel().getNamespacesToSchemas().get(namespace);
    if (schemaInfo != null) {
      for (RootElementDeclaration rootElementDeclaration : schemaInfo.getGlobalElements()) {
        if (rootElementDeclaration.getName().equals(name)) {
          return true;
        }
      }

      if (LocalElementDeclaration.class.isInstance(unwrapped)) {
        //local element declarations have to check implicit schema elements, too.
        for (ImplicitSchemaElement implicitSchemaElement : schemaInfo.getImplicitSchemaElements()) {
          if (implicitSchemaElement.getElementName().equals(name)) {
            return true;
          }
        }
      }
    }
    
    return false;
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