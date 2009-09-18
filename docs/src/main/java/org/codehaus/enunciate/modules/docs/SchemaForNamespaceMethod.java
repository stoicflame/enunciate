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

package org.codehaus.enunciate.modules.docs;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.contract.jaxb.*;
import org.codehaus.enunciate.contract.common.rest.RESTResourcePayload;

import java.util.List;
import java.util.Map;

/**
 * Rest resource path of the given REST noun.
 *
 * @author Ryan Heaton
 */
public class SchemaForNamespaceMethod implements TemplateMethodModelEx {

  private final Map<String, SchemaInfo> namespaces2Schemas;

  public SchemaForNamespaceMethod(Map<String, SchemaInfo> namespaces2Schemas) {
    this.namespaces2Schemas = namespaces2Schemas;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The uniqueContentTypes method must have a list of methods as a parameter.");
    }

    Object object = BeansWrapper.getDefaultInstance().unwrap((TemplateModel) list.get(0));
    String namespace;
    if (object instanceof ElementRef) {
      namespace = ((ElementRef) object).getRef().getNamespaceURI();
    }
    else if (object instanceof Accessor) {
      namespace = ((Accessor) object).getBaseType().getNamespace();
    }
    else if (object instanceof TypeDefinition) {
      namespace = ((TypeDefinition) object).getNamespace();
    }
    else if (object instanceof ElementDeclaration) {
      namespace = ((ElementDeclaration)object).getNamespace();
    }
    else if (object instanceof RESTResourcePayload) {
      ElementDeclaration element = ((RESTResourcePayload) object).getXmlElement();
      if (element == null) {
        return null;
      }

      namespace = element.getNamespace();
    }
    else {
      return null;
    }

    return namespaces2Schemas.get(namespace);
  }

}