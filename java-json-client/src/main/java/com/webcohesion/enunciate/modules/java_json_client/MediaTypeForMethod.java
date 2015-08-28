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

package com.webcohesion.enunciate.modules.java_json_client;

import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class MediaTypeForMethod implements TemplateMethodModelEx {

  /**
   * Returns the qname of the element that has the first parameter as the namespace, the second as the element.
   *
   * @param list The arguments.
   * @return The qname.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The MediaTypeForMethod must have a entity as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = DeepUnwrap.unwrap(from);

    if (unwrapped instanceof Entity) {
      List<? extends MediaTypeDescriptor> mediaTypes = ((Entity) unwrapped).getMediaTypes();
      if (mediaTypes != null && !mediaTypes.isEmpty()) {
        for (MediaTypeDescriptor mediaType : mediaTypes) {
          if (mediaType.getMediaType().contains("json")) {
            return mediaType.getMediaType();
          }
        }
      }
    }

    return "application/json";
  }

}