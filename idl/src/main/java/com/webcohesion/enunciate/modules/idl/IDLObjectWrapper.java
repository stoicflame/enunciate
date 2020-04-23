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

import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Special wrapper for processing the XML templates.
 *
 * @author Ryan Heaton
 */
public class IDLObjectWrapper extends DefaultObjectWrapper {

  private final Map<String, String> namespacePrefixes;

  public IDLObjectWrapper(Map<String, String> namespacePrefixes) {
    super(FreemarkerUtil.VERSION);
    this.namespacePrefixes = namespacePrefixes;
  }

  @Override
  public TemplateModel wrap(Object obj) throws TemplateModelException {
    if (obj instanceof QName) {
      return new QNameModel((QName) obj, this, this.namespacePrefixes);
    }
//    else if (obj instanceof WebResult) {
//      return new WebResultModel((WebResult) obj, this);
//    }
    else {
      return super.wrap(obj);
    }
  }
}
