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

package org.codehaus.enunciate.service;

/**
 * Factory for creating Enunciate services.
 *
 * @author Ryan Heaton
 */
public interface EnunciateServiceFactory {

  /**
   * Gets an instance of the specified service.
   *
   * @param implClass The base implementation class.
   * @return The instance.
   */
  Object getInstance(Class implClass) throws IllegalAccessException, InstantiationException;
  
  /**
   * Gets an instance of the specified service.
   *
   * @param impl The base implementation object.
   * @return The instance.
   */
  Object getInstance(Object impl);

}
