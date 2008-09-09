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

package org.codehaus.enunciate.contract.common.rest;

import java.util.Set;
import java.util.List;
import java.util.Map;

/**
 * A rest resource (common interface between different REST implementations).
 *
 * @author Ryan Heaton
 */
public interface RESTResource {

  /**
   * The path to the resource, including path parameters and relative to the base deployment address.
   *
   * @return The path to the resource, including path parameters and relative to the base deployment address.
   */
  String getPath();

  /**
   * The supported operations.
   *
   * @return The supported operations.
   */
  Set<String> getSupportedOperations();

  /**
   * The supported content types for this resource.
   *
   * @return The supported content types for this resource.
   */
  List<SupportedContentType> getSupportedContentTypes();

  /**
   * The parameters to the resource.
   *
   * @return The parameters to the resource.
   */
  List<? extends RESTResourceParameter> getResourceParameters();

  /**
   * The input payload for this resource.
   *
   * @return The input payload for this resource.
   */
  RESTResourcePayload getInputPayload();

  /**
   * The output payload for this resource.
   *
   * @return The output payload for this resource.
   */
  RESTResourcePayload getOutputPayload();

  /**
   * The potential errors.
   *
   * @return The potential errors.
   */
  List<? extends RESTResourceError> getResourceErrors();

  /**
   * The metadata associated with this resource.
   *
   * @return The metadata associated with this resource.
   */
  Map<String, Object> getMetaData();
}
