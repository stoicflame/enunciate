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

import org.codehaus.enunciate.contract.jaxb.ElementDeclaration;
import org.codehaus.enunciate.contract.json.JsonType;
import org.codehaus.enunciate.contract.json.JsonTypeDefinition;

/**
 * Payload for a REST resource.
 * 
 * @author Ryan Heaton
 */
public interface RESTResourcePayload {

  /**
   * The documentation value for the payload.
   *
   * @return The documentation value for the payload.
   */
  String getDocValue();

  /**
   * The XML element associated with the payload, or null if none (or unknown).
   *
   * @return The XML element associated with the payload, or null if none (or unknown).
   */
  ElementDeclaration getXmlElement();

  /**
   * The JSON element associated with the payload, or null if none (or unknown).
   * @return The JSON element associated with the payload, or null if none (or unknown).
   */
  JsonType getJsonType();
}
