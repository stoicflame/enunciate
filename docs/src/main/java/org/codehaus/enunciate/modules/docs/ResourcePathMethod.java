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
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.config.EnunciateConfiguration;
import org.codehaus.enunciate.contract.rest.RESTNoun;

import java.util.List;

/**
 * Rest resource path of the given REST noun.
 *
 * @author Ryan Heaton
 */
public class ResourcePathMethod implements TemplateMethodModelEx {

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 2) {
      throw new TemplateModelException("The resourcePath method must have an rest noun and content type as a parameter.");
    }

    Object object = BeansWrapper.getDefaultInstance().unwrap((TemplateModel) list.get(0));
    RESTNoun restNoun;
    if (object instanceof RESTNoun) {
      restNoun = ((RESTNoun) object);
    }
    else {
      throw new TemplateModelException("The resourcePath method must be an RESTNoun.  Not " + object.getClass().getName());
    }

    String contentType = String.valueOf(BeansWrapper.getDefaultInstance().unwrap((TemplateModel) list.get(1)));

    EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();
    EnunciateConfiguration config = model.getEnunciateConfig();
    return getSubcontext(config) + restNoun.toString();
  }

  protected String getSubcontext(EnunciateConfiguration config) {
    return config.getDefaultRestSubcontext();
  }

}