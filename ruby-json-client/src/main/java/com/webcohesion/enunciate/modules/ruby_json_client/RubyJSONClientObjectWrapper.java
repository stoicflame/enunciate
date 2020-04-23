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
package com.webcohesion.enunciate.modules.ruby_json_client;

import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Special wrapper for processing the XML templates.
 *
 * @author Ryan Heaton
 */
public class RubyJSONClientObjectWrapper extends DefaultObjectWrapper {

  public RubyJSONClientObjectWrapper() {
    super(FreemarkerUtil.VERSION);
  }

  @Override
  public TemplateModel wrap(Object obj) throws TemplateModelException {
    if (obj instanceof JavaDoc.JavaDocTagList) {
      return super.wrap(obj.toString());
    }
    else {
      return super.wrap(obj);
    }
  }
}
