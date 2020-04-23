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

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.webcohesion.enunciate.api.datatype.BaseTypeFormat;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;

import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Defined the swagger sub-format for a type.
 *
 * @author Jesper Skov
 */
public class DataFormatNameForMethod implements TemplateMethodModelEx {

  private static final Map<BaseTypeFormat, String> baseformat2swaggerformat = new EnumMap<BaseTypeFormat, String>(BaseTypeFormat.class);
  static {
    baseformat2swaggerformat.put(BaseTypeFormat.INT32, "int32");
    baseformat2swaggerformat.put(BaseTypeFormat.INT64, "int64");
  }

  @SuppressWarnings("rawtypes")
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The dataFormatNameFor method must have a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = FreemarkerUtil.unwrap(from);

    if (!DataTypeReference.class.isAssignableFrom(unwrapped.getClass())) {
      return null;
    }
    DataTypeReference reference = DataTypeReference.class.cast(unwrapped);

    return BaseTypeToSwagger.toSwaggerFormat(reference.getBaseTypeFormat());
  }

}