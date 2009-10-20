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

package org.codehaus.enunciate.template.freemarker;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.codehaus.enunciate.contract.common.rest.RESTResource;
import org.codehaus.enunciate.contract.common.rest.SupportedContentType;
import org.codehaus.enunciate.contract.common.rest.SupportedContentTypeAtSubcontext;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.rest.MimeType;

import java.util.*;

import net.sf.jelly.apt.freemarker.FreemarkerModel;

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

    LinkedHashMap<String, SupportedContentTypeAtSubcontext> supported = new LinkedHashMap<String, SupportedContentTypeAtSubcontext>();
    for (RESTResource resource : resourceList) {
      for (SupportedContentType contentType : resource.getSupportedContentTypes()) {
        List<String> supportedTypeSet = findAllSupportedTypes(contentType);
        for (String supportedType : supportedTypeSet) {
          SupportedContentTypeAtSubcontext type = supported.get(supportedType);
          if (type == null) {
            type = new SupportedContentTypeAtSubcontext();
            type.setType(supportedType);
            supported.put(supportedType, type);
          }

          type.setProduceable(type.isProduceable() || contentType.isProduceable());
          type.setConsumable(type.isConsumable() || contentType.isConsumable());

          if (contentType.isProduceable()) {
            Map<String, Set<String>> subcontextMap = (Map<String, Set<String>>) resource.getMetaData().get("subcontexts");
            if (subcontextMap != null) {
              type.setSubcontexts(subcontextMap.get(supportedType));
            }
          }
        }
      }
    }

    return supported.values();
  }

  /**
   * Finds all the supported types for the specified content type.
   *
   * @param contentType The content type.
   * @return The supported types.
   */
  protected List<String> findAllSupportedTypes(SupportedContentType contentType) {
    Set<String> allKnownContentTypes = ((EnunciateFreemarkerModel)FreemarkerModel.get()).getContentTypesToIds().keySet();
    String typeValue = contentType.getType();
    List<String> typeSet = new ArrayList<String>();
    typeSet.add(typeValue);
    try {
      MimeType mimeType = MimeType.parse(typeValue);
      for (String knownType : allKnownContentTypes) {
        try {
          MimeType knownMimeType = MimeType.parse(knownType);
          if (mimeType.isAcceptable(knownMimeType)) {
            typeSet.add(knownType);
          }
        }
        catch (Exception e) {
          //fall through...
        }
      }
    }
    catch (Exception e) {
      //fall through...
    }
    return typeSet;
  }

}