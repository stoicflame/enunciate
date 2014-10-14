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
import org.codehaus.enunciate.contract.jaxrs.ResourceMethodMediaType;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.rest.MimeType;

import java.util.*;

import net.sf.jelly.apt.freemarker.FreemarkerModel;

/**
 * Get the set of all content types applicable to a set of resource methods.
 *
 * @author Ryan Heaton
 */
public class UniqueContentTypesMethod implements TemplateMethodModelEx {

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The uniqueContentTypes method must have a list of methods as a parameter.");
    }

    Object object = BeansWrapper.getDefaultInstance().unwrap((TemplateModel) list.get(0));
    Collection<ResourceMethod> resourceList;
    if (object instanceof Collection) {
      resourceList = (Collection<ResourceMethod>) object;
    }
    else if (object instanceof ResourceMethod) {
      resourceList = Arrays.asList((ResourceMethod) object);
    }
    else {
      throw new TemplateModelException("The uniqueContentTypes method take a list of REST resources.  Not " + object.getClass().getName());
    }

    LinkedHashMap<String, ResourceMethodMediaType> supported = new LinkedHashMap<String, ResourceMethodMediaType>();
    for (ResourceMethod resource : resourceList) {
      for (ResourceMethodMediaType mediaType : resource.getApplicableMediaTypes()) {
        for (String applicableType : findAllKnownTypesAcceptableTo(mediaType)) {
          ResourceMethodMediaType type = supported.get(applicableType);
          if (type == null) {
            type = new ResourceMethodMediaType();
            type.setType(applicableType);
            supported.put(applicableType, type);
          }

          type.setProduceable(type.isProduceable() || mediaType.isProduceable());
          type.setConsumable(type.isConsumable() || mediaType.isConsumable());

          if (mediaType.isProduceable()) {
            //if the media type is produceable, we want to add any subcontexts
            //where the type might be mounted. This will only happen for
            //the media types that have ids.
            Map<String, Set<String>> subcontextMap = (Map<String, Set<String>>) resource.getMetaData().get("subcontexts");
            if (subcontextMap != null) {
              type.setSubcontexts(subcontextMap.get(applicableType));
            }
          }
        }
      }
    }

    return supported.values();
  }

  /**
   * Find all types that the models knows about that are acceptable to the specified media type.
   *
   * @param mediaType The content type.
   * @return The supported types.
   */
  protected List<String> findAllKnownTypesAcceptableTo(ResourceMethodMediaType mediaType) {
    Set<String> allKnownContentTypes = ((EnunciateFreemarkerModel)FreemarkerModel.get()).getContentTypesToIds().keySet();
    String typeValue = mediaType.getType();
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