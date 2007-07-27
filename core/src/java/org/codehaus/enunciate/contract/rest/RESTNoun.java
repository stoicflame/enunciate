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

package org.codehaus.enunciate.contract.rest;

/**
 * A REST noun, consisting of the noun name and context.
 *
 * @author Ryan Heaton
 */
public class RESTNoun {

  private final String name;
  private final String context;

  RESTNoun(String name, String context) {
    this.name = name;
    if (context.startsWith("/")) {
      context = context.substring(1);
    }
    if (context.endsWith("/")) {
      context = context.substring(0, context.length() - 1);
    }
    this.context = context;
  }

  /**
   * The name of the noun.
   *
   * @return The name of the noun.
   */
  public String getName() {
    return name;
  }

  /**
   * The context of the noun.
   *
   * @return The context of the noun.
   */
  public String getContext() {
    return context;
  }

  @Override
  public String toString() {
    if (this.context.length() == 0) {
      return this.name;
    }
    else {
      return this.context + "/" + this.name;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RESTNoun restNoun = (RESTNoun) o;
    return context.equals(restNoun.context) && name.equals(restNoun.name);
  }

  @Override
  public int hashCode() {
    int result;
    result = name.hashCode();
    result = 31 * result + context.hashCode();
    return result;
  }
}
