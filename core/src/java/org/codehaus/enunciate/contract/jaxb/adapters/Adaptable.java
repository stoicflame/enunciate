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

package org.codehaus.enunciate.contract.jaxb.adapters;

/**
 * Marks an element that has a type that is possibly adaptable according to the JAXB specification.
 * 
 * @author Ryan Heaton
 */
public interface Adaptable {

  /**
   * Whether this element is adapted according to the JAXB spec.
   *
   * @return Whether the element is adapted.
   */
  boolean isAdapted();

  /**
   * The class type of the adaptor.
   *
   * @return The class type of the adaptor.
   */
  AdapterType getAdapterType();

}
