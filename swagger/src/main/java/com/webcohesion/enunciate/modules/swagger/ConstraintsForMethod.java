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

import com.webcohesion.enunciate.api.HasAnnotations;
import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Output swagger constraints in JSON format.
 *
 * @author Ryan Heaton
 */
public class ConstraintsForMethod implements TemplateMethodModelEx {

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The constraintsFor method must have a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = FreemarkerUtil.unwrap(from);
    boolean array = false;
    if (list.size() > 1) {
      array = (Boolean) FreemarkerUtil.unwrap((TemplateModel) list.get(1));
    }

    Map<String, Object> constraints = new HashMap<String, Object>();
    if (unwrapped instanceof HasAnnotations) {
      HasAnnotations el = (HasAnnotations) unwrapped;

      jakarta.validation.constraints.Max max = el.getAnnotation(jakarta.validation.constraints.Max.class);
      jakarta.validation.constraints.DecimalMax decimalMax = el.getAnnotation(jakarta.validation.constraints.DecimalMax.class);
      if (max != null) {
        constraints.put("maximum", max.value());
      }
      else if (decimalMax != null) {
        constraints.put("maximum", decimalMax.value());
        constraints.put("exclusiveMaximum", !decimalMax.inclusive());
      }

      jakarta.validation.constraints.Min min = el.getAnnotation(jakarta.validation.constraints.Min.class);
      jakarta.validation.constraints.DecimalMin decimalMin = el.getAnnotation(jakarta.validation.constraints.DecimalMin.class);
      if (min != null) {
        constraints.put("minimum", min.value());
      }
      else if (decimalMin != null) {
        constraints.put("minimum", decimalMin.value());
        constraints.put("exclusiveMinimum", !decimalMin.inclusive());
      }

      jakarta.validation.constraints.Size size = el.getAnnotation(jakarta.validation.constraints.Size.class);
      if (size != null) {
        int mx = size.max();
        int mn = size.min();
        if (array) {
          constraints.put("maxItems", mx);
          constraints.put("minItems", mn);
        }
        else {
          constraints.put("maxLength", mx);
          constraints.put("minLength", mn);
        }
      }

      jakarta.validation.constraints.Pattern mustMatchPattern = el.getAnnotation(jakarta.validation.constraints.Pattern.class);
      if (mustMatchPattern != null) {
        constraints.put("pattern", mustMatchPattern.regexp());
      }
    }

    return constraints;
  }
}