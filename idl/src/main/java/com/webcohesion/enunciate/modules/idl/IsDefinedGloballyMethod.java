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
package com.webcohesion.enunciate.modules.idl;

import com.webcohesion.enunciate.modules.jaxb.model.ImplicitSchemaElement;
import com.webcohesion.enunciate.modules.jaxb.model.LocalElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.RootElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.List;

/**
 * A method used in templates to output the prefix for a given namespace.
 *
 * @author Ryan Heaton
 */
public class IsDefinedGloballyMethod implements TemplateMethodModelEx {

  private final SchemaInfo schemaInfo;

  public IsDefinedGloballyMethod(SchemaInfo schemaInfo) {
    this.schemaInfo = schemaInfo;
  }

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
    Object unwrapped = FreemarkerUtil.unwrap(from);
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

    namespace = namespace == null ? "" : namespace;
    String schemaNamespace = schemaInfo.getNamespace() == null ? "" : schemaInfo.getNamespace();
    if (namespace.equals(schemaNamespace)) {
      for (RootElementDeclaration rootElementDeclaration : schemaInfo.getRootElements()) {
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

}