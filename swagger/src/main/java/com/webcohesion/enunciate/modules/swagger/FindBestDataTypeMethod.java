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

import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.api.datatype.Example;
import com.webcohesion.enunciate.api.resources.*;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.util.*;

/**
 * @author Ryan Heaton
 */
public class FindBestDataTypeMethod implements TemplateMethodModelEx {

  static DataTypeReference GENERIC_STRING_BASED_DATATYPE_REFERENCE = new DataTypeReference() {
    @Override
    public String getLabel() {
      return null;
    }

    @Override
    public String getSlug() {
      return null;
    }

    @Override
    public List<ContainerType> getContainers() {
      return null;
    }

    @Override
    public DataType getValue() {
      return null;
    }

    @Override
    public BaseType getBaseType() {
      return BaseType.string;
    }

    @Override
    public Example getExample() {
      return null;
    }
  };

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The responsesOf method must have a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = new BeansWrapperBuilder(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).build().unwrap(from);
    if (unwrapped instanceof Entity) {
      return findBestDataType((Entity) unwrapped);
    }

    throw new TemplateModelException("No responses for: " + unwrapped);
  }

  protected static DataTypeReference findBestDataType(Entity entity) {
    if (entity == null) {
      return null;
    }

    return findBestDataType(entity.getMediaTypes());
  }

  protected static DataTypeReference findBestDataType(List<? extends MediaTypeDescriptor> mediaTypes) {
    if (mediaTypes == null || mediaTypes.isEmpty()) {
      return null;
    }

    float highestQuality = Float.MIN_VALUE;
    for (MediaTypeDescriptor mediaTypeDescriptor : mediaTypes) {
      highestQuality = Math.max(highestQuality, mediaTypeDescriptor.getQualityOfSourceFactor());
    }

    //first filter out all the media types of lower quality:
    mediaTypes = new ArrayList<MediaTypeDescriptor>(mediaTypes);
    Iterator<? extends MediaTypeDescriptor> iterator = mediaTypes.iterator();
    while (iterator.hasNext()) {
      MediaTypeDescriptor mediaTypeDescriptor = iterator.next();
      if (mediaTypeDescriptor.getQualityOfSourceFactor() < highestQuality) {
        iterator.remove();
      }
    }

    //return the first JSON-based media type.
    for (MediaTypeDescriptor mediaTypeDescriptor : mediaTypes) {
      if (mediaTypeDescriptor.getSyntax() != null && mediaTypeDescriptor.getSyntax().toLowerCase().contains("json")) {
        return mediaTypeDescriptor.getDataType();
      }
    }

    //return the first text-based media type.
    for (MediaTypeDescriptor mediaTypeDescriptor : mediaTypes) {
      String mt = mediaTypeDescriptor.getMediaType();
      if (mt != null) {
        mt = mt.toLowerCase();
        if (mt.startsWith("text") || mt.endsWith("json") || mt.endsWith("xml")) {
          DataTypeReference dataType = mediaTypeDescriptor.getDataType();
          return dataType == null || dataType.getValue() == null ? GENERIC_STRING_BASED_DATATYPE_REFERENCE : dataType;
        }
      }
    }

    //didn't find any text-based media types; try any other media types.
    for (MediaTypeDescriptor mediaTypeDescriptor : mediaTypes) {
      if (mediaTypeDescriptor.getDataType() != null) {
        return mediaTypeDescriptor.getDataType();
      }
    }

    return null;
  }

}