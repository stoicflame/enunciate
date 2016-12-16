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
package com.webcohesion.enunciate.modules.jackson1.api.impl;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.datatype.Example;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;

import java.util.Collections;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class MediaTypeDescriptorImpl implements MediaTypeDescriptor {

  private final String mediaType;
  private final DataTypeReference dataType;

  public MediaTypeDescriptorImpl(String mediaType, DataTypeReference dataType) {
    this.mediaType = mediaType;
    this.dataType = dataType;
  }

  @Override
  public String getMediaType() {
    return this.mediaType;
  }

  @Override
  public DataTypeReference getDataType() {
    return this.dataType;
  }

  @Override
  public String getSyntax() {
    return "JSON";
  }

  @Override
  public float getQualityOfSourceFactor() {
    return 1.0F;
  }

  @Override
  public Map<String, String> getMediaTypeParams() {
    return Collections.emptyMap();
  }

  @Override
  public Example getExample() {
    return this.dataType == null ? null : this.dataType.getExample();
  }
}
