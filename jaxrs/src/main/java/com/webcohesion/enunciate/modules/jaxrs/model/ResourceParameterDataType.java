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
package com.webcohesion.enunciate.modules.jaxrs.model;

import com.webcohesion.enunciate.api.datatype.BaseTypeFormat;

/**
 * @author Ryan Heaton
 */
public enum ResourceParameterDataType {
  BOOLEAN("boolean", null),
  INT32("integer", BaseTypeFormat.INT32),
  INT64("integer", BaseTypeFormat.INT64),
  DOUBLE("number", BaseTypeFormat.DOUBLE),
  FLOAT("number", BaseTypeFormat.FLOAT),
  STRING("string", null),
  FILE("file", null);

  public final String name;
  public final BaseTypeFormat format;

  private ResourceParameterDataType(String name, BaseTypeFormat format) {
    this.name = name;
    this.format = format;
  }
}
