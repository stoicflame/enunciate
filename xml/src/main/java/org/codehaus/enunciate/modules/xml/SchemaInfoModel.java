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

package org.codehaus.enunciate.modules.xml;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.config.SchemaInfo;

/**
 * The model for a {@link org.codehaus.enunciate.config.SchemaInfo}, taking into account the custom properties set
 * by the configuration for the xml module.
 *
 * @author Ryan Heaton
 */
public class SchemaInfoModel extends StringModel {

  private final SchemaInfo schemaInfo;

  public SchemaInfoModel(SchemaInfo schemaInfo, BeansWrapper wrapper) {
    super(schemaInfo, wrapper);
    this.schemaInfo = schemaInfo;
  }

  @Override
  public TemplateModel get(String key) throws TemplateModelException {
    if (("filename".equals(key)) || ("location".equals(key)) || "appinfo".equals(key) || "jaxbBindingVersion".equals(key)) {
      return wrap(schemaInfo.getProperty(key));
    }
    else if ("alreadyExists".equals(key)) {
      return wrap(schemaInfo.getProperty("file") != null);
    }

    return super.get(key);
  }
}
