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
package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceEntityParameter;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;

import javax.lang.model.element.AnnotationMirror;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class RequestEntityImpl implements Entity {

  private final ResourceMethod resourceMethod;
  private final ResourceEntityParameter entityParameter;

  public RequestEntityImpl(ResourceMethod resourceMethod, ResourceEntityParameter entityParameter) {
    this.resourceMethod = resourceMethod;
    this.entityParameter = entityParameter;
  }

  @Override
  public String getDescription() {
    return this.entityParameter == null ? null : this.entityParameter.getDocValue();
  }

  @Override
  public List<? extends MediaTypeDescriptor> getMediaTypes() {
    Set<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType> consumes = this.resourceMethod.getConsumesMediaTypes();
    ArrayList<MediaTypeDescriptor> mts = new ArrayList<MediaTypeDescriptor>(consumes.size());
    for (com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType mt : consumes) {
      boolean descriptorFound = false;
      if (this.entityParameter != null) {
        DecoratedTypeMirror type = (DecoratedTypeMirror) this.entityParameter.getType();
        for (Syntax syntax : this.resourceMethod.getContext().getContext().getApiRegistry().getSyntaxes()) {
          MediaTypeDescriptor descriptor = syntax.findMediaTypeDescriptor(mt.getMediaType(), type, mt.getQualityOfSource());
          if (descriptor != null) {
            mts.add(descriptor);
            descriptorFound = true;
          }
        }
      }

      if (!descriptorFound) {
        mts.add(new CustomMediaTypeDescriptor(mt.getMediaType(), mt.getQualityOfSource()));
      }
    }
    return mts;
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.entityParameter == null ? Collections.<String,AnnotationMirror>emptyMap() : this.entityParameter.getAnnotations();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.entityParameter == null ? null : this.entityParameter.getJavaDoc();
  }
}
