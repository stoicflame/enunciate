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
import com.webcohesion.enunciate.api.resources.Parameter;
import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class ValidParametersMethod implements TemplateMethodModelEx {

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The responsesOf method must have a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = FreemarkerUtil.unwrap(from);
    if (unwrapped instanceof Method) {
      Method method = (Method) unwrapped;
      ArrayList<Parameter> params = new ArrayList<Parameter>();

      for (Parameter parameter : method.getParameters()) {
        String type = parameter.getTypeLabel().toLowerCase();
        if (type.contains("path")) {
          params.add(new SwaggerParameter(parameter, "path"));
        }
        else if (type.contains("form")) {
          params.add(new SwaggerParameter(parameter, "formData"));
        }
        else if (type.contains("query")) {
          params.add(new SwaggerParameter(parameter, "query"));
        }
        else if (type.contains("header")) {
          params.add(new SwaggerParameter(parameter, "header"));
        }
      }

      return params;
    }

    throw new TemplateModelException("No parameters for: " + unwrapped);
  }

}