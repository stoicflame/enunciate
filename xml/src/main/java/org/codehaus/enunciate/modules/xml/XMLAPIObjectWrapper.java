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

import freemarker.ext.beans.StringModel;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.config.SchemaInfo;
import org.codehaus.enunciate.config.WsdlInfo;
import net.sf.jelly.apt.decorations.JavaDoc;

import javax.xml.namespace.QName;

/**
 * Special wrapper for processing the XML templates.
 *
 * @author Ryan Heaton
 */
public class XMLAPIObjectWrapper extends DefaultObjectWrapper {

  @Override
  public TemplateModel wrap(Object obj) throws TemplateModelException {
    if (obj instanceof QName) {
      return new QNameModel((QName) obj, this);
    }
    else if (obj instanceof SchemaInfo) {
      return new SchemaInfoModel((SchemaInfo) obj, this);
    }
    else if (obj instanceof WsdlInfo) {
      return new WsdlInfoModel((WsdlInfo) obj, this);
    }
    else if (obj instanceof JavaDoc) {
      return new StringModel(obj, this);
    }
    else {
      return super.wrap(obj);
    }
  }
}
