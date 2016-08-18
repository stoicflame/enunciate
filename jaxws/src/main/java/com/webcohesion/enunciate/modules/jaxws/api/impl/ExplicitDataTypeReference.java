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
package com.webcohesion.enunciate.modules.jaxws.api.impl;

import com.webcohesion.enunciate.api.datatype.BaseType;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class ExplicitDataTypeReference implements DataTypeReference {

  private final DataType dataType;

  public ExplicitDataTypeReference(DataType dataType) {
    this.dataType = dataType;
  }

  @Override
  public String getLabel() {
    return this.dataType.getLabel();
  }

  @Override
  public String getSlug() {
    return this.dataType.getSlug();
  }

  @Override
  public BaseType getBaseType() {
    return this.dataType.getBaseType();
  }

  @Override
  public List<ContainerType> getContainers() {
    return null;
  }

  @Override
  public DataType getValue() {
    return this.dataType;
  }
}
