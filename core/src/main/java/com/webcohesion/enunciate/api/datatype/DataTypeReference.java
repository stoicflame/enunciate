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
package com.webcohesion.enunciate.api.datatype;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public interface DataTypeReference {

  enum ContainerType {
    array,

    collection,

    list,

    map;

    public boolean isMap() {
      return this == map;
    }
  }

  String getLabel();

  String getSlug();

  List<ContainerType> getContainers();

  DataType getValue();

  BaseType getBaseType();

  String getBaseTypeFormat();

  Example getExample();
}
