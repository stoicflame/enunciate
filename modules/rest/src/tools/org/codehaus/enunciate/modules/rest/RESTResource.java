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

package org.codehaus.enunciate.modules.rest;

import org.codehaus.enunciate.rest.annotations.VerbType;

import java.util.EnumMap;
import java.util.Set;
import java.lang.reflect.Method;

/**
 * A REST resource is composed of the following:
 *
 * <ul>
 *   <li>A noun in its context.</li>
 *   <li>The set of verbs that are applicable to the noun.</li>
 * </ul>
 *
 * @author Ryan Heaton
 */
public class RESTResource implements Comparable<RESTResource> {

  private final String noun;
  private final String nounContext;
  private final EnumMap<VerbType, RESTOperation> operations = new EnumMap<VerbType, RESTOperation>(VerbType.class);

  /**
   * Construct a REST resource for the specified noun, assuming the empty context.
   *
   * @param noun The noun for this REST resource.
   */
  public RESTResource(String noun) {
    this(noun, "");
  }

  /**
   * Construct a REST resource for the specified noun and noun context.
   * @param noun The noun.
   * @param nounContext The noun context.
   */
  public RESTResource(String noun, String nounContext) {
    this.noun = noun;
    this.nounContext = nounContext;
  }

  /**
   * Adds an operation to this REST resource.
   *
   * @param verb The verb for the operation.
   * @param endpoint The endpoint on which to invoke the operation.
   * @param method The method to invoke on the endpoint.
   * @return Whether the operation was successfully added.  (false if the specified verb was already added).
   */
  public boolean addOperation(VerbType verb, Object endpoint, Method method) {
    return !operations.containsKey(verb) && operations.put(verb, new RESTOperation(verb, endpoint, method)) == null;
  }

  /**
   * Gets the REST operation for the specified verb.
   *
   * @param verb The verb.
   * @return The REST operation.
   */
  public RESTOperation getOperation(VerbType verb) {
    return operations.get(verb);
  }

  /**
   * The noun for this resource.
   *
   * @return The noun for this resource.
   */
  public String getNoun() {
    return noun;
  }

  /**
   * The noun context for this resource.
   *
   * @return The noun context for this resource.
   */
  public String getNounContext() {
    return nounContext;
  }

  /**
   * Gets the supported verbs for this resource.
   *
   * @return The supported verbs.
   */
  public Set<VerbType> getSupportedVerbs() {
    return operations.keySet();
  }

  /**
   * Compares the two REST resources.
   *
   * @param other The resource to compare to this one.
   * @return The comparison.
   */
  public int compareTo(RESTResource other) {
    return this.noun.compareTo(other.noun);
  }

  @Override
  public String toString() {
    return noun;
  }
}
