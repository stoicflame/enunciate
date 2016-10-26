/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.HasAnnotations;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.validation.constraints.*;
import java.util.List;

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
    BeansWrapper wrpper = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build();
    Object unwrapped = wrpper.unwrap(from);
    boolean array = false;
    if (list.size() > 1) {
      array = (Boolean) wrpper.unwrap((TemplateModel) list.get(1));
    }

    StringBuilder builder = new StringBuilder();
    if (unwrapped instanceof HasAnnotations) {
      HasAnnotations el = (HasAnnotations) unwrapped;

      Max max = el.getAnnotation(Max.class);
      DecimalMax decimalMax = el.getAnnotation(DecimalMax.class);
      if (max != null) {
        builder.append("\"maximum\" : ").append(max.value()).append(",");
      }
      else if (decimalMax != null) {
        builder.append("\"maximum\" : ").append(decimalMax.value()).append(",");
        builder.append("\"exclusiveMaximum\" : ").append(!decimalMax.inclusive()).append(",");
      }

      Min min = el.getAnnotation(Min.class);
      DecimalMin decimalMin = el.getAnnotation(DecimalMin.class);
      if (min != null) {
        builder.append("\"minimum\" : ").append(min.value()).append(",");
      }
      else if (decimalMin != null) {
        builder.append("\"minimum\" : ").append(decimalMin.value()).append(",");
        builder.append("\"exclusiveMinimum\" : ").append(!decimalMin.inclusive()).append(",");
      }

      Size size = el.getAnnotation(Size.class);
      if (size != null) {
        if (array) {
          builder.append("\"maxItems\" : ").append(size.max()).append(",");
          builder.append("\"minItems\" : ").append(size.min()).append(",");
        }
        else {
          builder.append("\"maxLength\" : ").append(size.max()).append(",");
          builder.append("\"minLength\" : ").append(size.min()).append(",");
        }
      }

      Pattern mustMatchPattern = el.getAnnotation(Pattern.class);
      if (mustMatchPattern != null) {
        builder.append("\"pattern\" : \"").append(mustMatchPattern.regexp()).append("\",");
      }
    }
    return builder.toString();

  }
}