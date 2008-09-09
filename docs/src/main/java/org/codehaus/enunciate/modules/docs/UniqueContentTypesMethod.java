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
import org.codehaus.enunciate.contract.common.rest.RESTResource;
import org.codehaus.enunciate.contract.common.rest.SupportedContentType;

import java.util.*;

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
    Collection<RESTResource> resourceList;
    if (object instanceof Collection) {
      resourceList = (Collection<RESTResource>) object;
    }
    else if (object instanceof RESTResource) {
      resourceList = Arrays.asList((RESTResource) object);
    }
    else {
      throw new TemplateModelException("The uniqueContentTypes method take a list of REST resources.  Not " + object.getClass().getName());
    }

    TreeMap<String, SupportedContentTypeAtSubcontext> supported = new TreeMap<String, SupportedContentTypeAtSubcontext>();
    for (RESTResource resource : resourceList) {
      for (SupportedContentType contentType : resource.getSupportedContentTypes()) {
        SupportedContentTypeAtSubcontext type = supported.get(contentType.getType());
        if (type == null) {
          type = new SupportedContentTypeAtSubcontext();
          type.setType(contentType.getType());
          supported.put(contentType.getType(), type);
        }

        type.setProduceable(type.isProduceable() || contentType.isProduceable());
        type.setConsumable(type.isConsumable() || contentType.isConsumable());

        if (contentType.isProduceable()) {
          Map<String, String> subcontextMap = (Map<String, String>) resource.getMetaData().get("subcontexts");
          if (subcontextMap != null) {
            type.setSubcontext(subcontextMap.get(contentType.getType()));
          }
        }
      }
    }

    return supported.values();
  }

}