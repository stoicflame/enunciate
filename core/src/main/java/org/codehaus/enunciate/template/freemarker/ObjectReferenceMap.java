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

import java.util.Map;
import java.util.Collection;

/**
 * Freemarker map for keys that are objects.
 *
 * @author Ryan Heaton
 */
public class ObjectReferenceMap {

  private final Map map;

  public ObjectReferenceMap(Map map) {
    this.map = map;
  }

  public Object get(Object ref) {
    return this.map.get(ref);
  }

  public Collection<Object> keys() {
    return this.map.keySet();
  }

  public Collection<Object> values() {
    return this.map.values();
  }
}
