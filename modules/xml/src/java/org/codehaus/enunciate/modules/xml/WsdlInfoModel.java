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

package org.codehaus.enunciate.modules.xml;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class WsdlInfoModel extends StringModel {

  private final WsdlInfo wsdlInfo;

  public WsdlInfoModel(WsdlInfo wsdlInfo, BeansWrapper wrapper) {
    super(wsdlInfo, wrapper);
    this.wsdlInfo = wsdlInfo;
  }

  @Override
  public TemplateModel get(String key) throws TemplateModelException {
    if (("filename".equals(key)) || ("location".equals(key)) || ("inlineSchema".equals(key))) {
      return wrap(wsdlInfo.getProperty(key));
    }
    else if ("importedNamespaces".equals(key)) {
      Set<String> importedNamespaces = wsdlInfo.getImportedNamespaces();
      SchemaInfo associatedSchema = wsdlInfo.getAssociatedSchema();
      Boolean inlineSchema = (Boolean) wsdlInfo.getProperty("inlineSchema");
      if (associatedSchema != null && inlineSchema != null && inlineSchema) {
        importedNamespaces.addAll(associatedSchema.getReferencedNamespaces());
      }
      return wrap(importedNamespaces);
    }
    else if ("importedSchemas".equals(key)) {
      TreeSet<SchemaInfo> schemas = new TreeSet<SchemaInfo>(new SchemaInfoComparator());
      schemas.addAll(wsdlInfo.getImportedSchemas());
      SchemaInfo associatedSchema = wsdlInfo.getAssociatedSchema();
      Boolean inlineSchema = (Boolean) wsdlInfo.getProperty("inlineSchema");
      if (associatedSchema != null && inlineSchema != null && inlineSchema) {
        schemas.addAll(associatedSchema.getImportedSchemas());
      }
      return wrap(schemas);
    }

    return super.get(key);
  }
}
