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
import org.codehaus.enunciate.contract.rest.RESTMethod;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Rest resource path of the given REST noun.
 *
 * @author Ryan Heaton
 */
public class UniqueContentTypesMethod implements TemplateMethodModelEx {

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The uniqueContentTypes method must have a list of methods as a parameter.");
    }

    Object object = BeansWrapper.getDefaultInstance().unwrap((TemplateModel) list.get(0));
    List<RESTMethod> methodList;
    if (object instanceof List) {
      methodList = (List<RESTMethod>) object;
    }
    else {
      throw new TemplateModelException("The uniqueContentTypes method take a list of REST methods.  Not " + object.getClass().getName());
    }

    Set<String> contentTypes = new TreeSet<String>();
    for (RESTMethod restMethod : methodList) {
      contentTypes.addAll(restMethod.getContentTypes());
    }

    return contentTypes;
  }

}