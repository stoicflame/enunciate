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

package com.webcohesion.enunciate.modules.objc_client;

import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Special wrapper for processing the XML templates.
 *
 * @author Ryan Heaton
 */
public class ObjCClientObjectWrapper extends DefaultObjectWrapper {

  public ObjCClientObjectWrapper() {
    super(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
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
