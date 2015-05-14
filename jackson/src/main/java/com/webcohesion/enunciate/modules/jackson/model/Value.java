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

package com.webcohesion.enunciate.modules.jackson.model;

import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;

/**
 * An accessor that is marshalled in json to an json value.
 *
 * @author Ryan Heaton
 */
public class Value extends Accessor {

  public Value(javax.lang.model.element.Element delegate, TypeDefinition typedef, EnunciateJacksonContext context) {
    super(delegate, typedef, context);
  }

  /**
   * There's no name of a value accessor
   *
   * @return null.
   */
  public String getName() {
    return null;
  }

  @Override
  public boolean isValue() {
    return true;
  }

}
