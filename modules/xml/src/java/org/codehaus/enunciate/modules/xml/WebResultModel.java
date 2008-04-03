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
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.contract.jaxws.WebResult;

/**
 * @author Ryan Heaton
 */
public class WebResultModel implements TemplateHashModel {

  private WebResult result;
  private BeansWrapper wrapper;

  public WebResultModel(WebResult result, BeansWrapper wrapper) {
    this.result = result;
    this.wrapper = wrapper;
  }

  public TemplateModel get(String key) throws TemplateModelException {
    if ("name".equals(key)) {
      return this.wrapper.wrap(result.getName());
    }
    else if ("targetNamespace".equals(key)) {
      return this.wrapper.wrap(result.getTargetNamespace());
    }
    else if ("partName".equals(key)) {
      return this.wrapper.wrap(result.getPartName());
    }
    else if ("webMethod".equals(key)) {
      return this.wrapper.wrap(result.getWebMethod());
    }
    else {
      return ((TemplateHashModel) this.wrapper.wrap(result.getType())).get(key);
    }
  }

  public boolean isEmpty() throws TemplateModelException {
    return false;
  }

}
