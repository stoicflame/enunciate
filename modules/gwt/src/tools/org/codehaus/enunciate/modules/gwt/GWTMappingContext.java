/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.modules.gwt;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A mapping context for mapping GWT contexts.
 *
 * @author Ryan Heaton
 */
public class GWTMappingContext {

  private final Map<Object, Object> mappedObjects = new IdentityHashMap<Object, Object>();

  public Map<Object, Object> getMappedObjects() {
    return Collections.unmodifiableMap(mappedObjects);
  }

  public void objectMapped(Object from, Object to) {
    this.mappedObjects.put(from, to);
  }
}
