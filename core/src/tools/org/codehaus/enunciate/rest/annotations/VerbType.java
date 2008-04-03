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

package org.codehaus.enunciate.rest.annotations;

/**
 * The possible verbs by which a noun is accessible.
 *
 * @author Ryan Heaton
 */
public enum VerbType {

  create,

  read,

  update,

  delete,

  get(read),

  put(create),

  post(update);

  private final VerbType alias;

  VerbType() {
    this.alias = null;
  }

  VerbType(VerbType alias) {
    this.alias = alias;
  }

  /**
   * The alias of the verb.
   *
   * @return The alias of the verb, or null if none.
   */
  public VerbType getAlias() {
    return alias;
  }
}
