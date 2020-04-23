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
package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Template method used to determine the objective-c "simple name" of an accessor.
 *
 * @author Ryan Heaton
 */
public class OperationIdForMethod implements TemplateMethodModelEx {

  private static final Map<String, String> OPERATIONID_BY_SLUG = new TreeMap<String, String>();

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The operationIdFor method must have a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = FreemarkerUtil.unwrap(from);
    if (unwrapped instanceof Method) {
      Method method = (Method) unwrapped;
      String assignment = OPERATIONID_BY_SLUG.get(method.getSlug());
      if (assignment == null) {
        int suffix = 2;
        assignment = method.getDeveloperLabel();
        String root = assignment;
        Collection<String> assignments = OPERATIONID_BY_SLUG.values();
        while (assignments.contains(assignment)) {
          assignment = root + suffix++;
        }

        OPERATIONID_BY_SLUG.put(method.getSlug(), assignment);
      }
      return assignment;
    }
    else {
      throw new IllegalStateException();
    }
  }

}
