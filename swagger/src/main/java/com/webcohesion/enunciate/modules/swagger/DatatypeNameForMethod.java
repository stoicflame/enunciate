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

package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.datatype.BaseType;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.List;

/**
 * Template method used to determine the objective-c "simple name" of an accessor.
 *
 * @author Ryan Heaton
 */
public class DatatypeNameForMethod implements TemplateMethodModelEx {

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The datatypeNameFor method must have a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build().unwrap(from);
    BaseType baseType = null;
    if (unwrapped instanceof DataType) {
      DataType dataType = (DataType) unwrapped;
      baseType = dataType.getBaseType();
    }
    else if (unwrapped instanceof DataTypeReference) {
      DataTypeReference reference = (DataTypeReference) unwrapped;
      baseType = reference.getBaseType();
    }

    if (baseType == null) {
      throw new TemplateModelException("No data type name for: " + unwrapped);
    }

    switch (baseType) {
      case bool:
        return "boolean";
      case number:
        return "number";
      case string:
        return "string";
      default:
        return "object";
    }
  }
}