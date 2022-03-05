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

      javax.validation.constraints.Max max = el.getAnnotation(javax.validation.constraints.Max.class);
      jakarta.validation.constraints.Max max2 = el.getAnnotation(jakarta.validation.constraints.Max.class);
      javax.validation.constraints.DecimalMax decimalMax = el.getAnnotation(javax.validation.constraints.DecimalMax.class);
      jakarta.validation.constraints.DecimalMax decimalMax2 = el.getAnnotation(jakarta.validation.constraints.DecimalMax.class);
      if (max != null || max2 != null) {
        constraints.put("maximum", (max != null ? max.value() : max2.value()));
      }
      else if (decimalMax != null || decimalMax2 != null) {
        constraints.put("maximum", (decimalMax != null ? decimalMax.value() : decimalMax2.value()));
        constraints.put("exclusiveMaximum", !(decimalMax != null ? decimalMax.inclusive() : decimalMax2.inclusive()));
      }

      javax.validation.constraints.Min min = el.getAnnotation(javax.validation.constraints.Min.class);
      jakarta.validation.constraints.Min min2 = el.getAnnotation(jakarta.validation.constraints.Min.class);
      javax.validation.constraints.DecimalMin decimalMin = el.getAnnotation(javax.validation.constraints.DecimalMin.class);
      jakarta.validation.constraints.DecimalMin decimalMin2 = el.getAnnotation(jakarta.validation.constraints.DecimalMin.class);
      if (min != null || min2 != null) {
        constraints.put("minimum", (min != null ? min.value() : min2.value()));
      }
      else if (decimalMin != null || decimalMin2 != null) {
        constraints.put("minimum", (decimalMin != null ? decimalMin.value() : decimalMin2.value()));
        constraints.put("exclusiveMinimum", !(decimalMin != null ? decimalMin.inclusive() : decimalMin2.inclusive()));
      }

      javax.validation.constraints.Size size = el.getAnnotation(javax.validation.constraints.Size.class);
      jakarta.validation.constraints.Size size2 = el.getAnnotation(jakarta.validation.constraints.Size.class);
      if (size != null || size2 != null) {
            int mx = size != null ? size.max() : size2.max();
            int mn = size != null ? size.min() : size2.min();
        if (array) {
          constraints.put("maxItems", mx);
          constraints.put("minItems", mn);
        }
        else {
          constraints.put("maxLength", mx);
          constraints.put("minLength", mn);
        }
      }

      javax.validation.constraints.Pattern mustMatchPattern = el.getAnnotation(javax.validation.constraints.Pattern.class);
      jakarta.validation.constraints.Pattern mustMatchPattern2 = el.getAnnotation(jakarta.validation.constraints.Pattern.class);
      if (mustMatchPattern != null || mustMatchPattern2 != null) {
        constraints.put("pattern", (mustMatchPattern != null ? mustMatchPattern.regexp() : mustMatchPattern2.regexp()));
      }
    }

    return constraints;
  }
}