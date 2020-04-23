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

import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.util.freemarker.FreemarkerUtil;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class DefinitionIdForMethod implements TemplateMethodModelEx {

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The definitionId method must have a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = FreemarkerUtil.unwrap(from);

    if (unwrapped instanceof DataType) {
      return definitionIdFromSlug(((DataType) unwrapped).getSlug());
    }
    else if (unwrapped instanceof DataTypeReference) {
      return definitionIdFromSlug(((DataTypeReference) unwrapped).getSlug());
    }

    return null;
  }

  private static String definitionIdFromSlug(String slug) {
    //we just want to strip the "_json" for now. See https://github.com/stoicflame/enunciate/issues/944
    //we can get more sophisticated later.
    if (StringUtils.startsWith(slug, "json_")) {
      return slug.substring(5);
    }
    else {
      return slug;
    }
  }
}