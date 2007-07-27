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

package org.codehaus.enunciate.rest.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Customizes the context of all nouns on a given REST endpoint.
 *
 * @author Ryan Heaton
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ( ElementType.TYPE )
public @interface NounContext {

  /**
   * The context of a noun.  Used to group nouns under a specific context.
   *
   * @return The noun context.
   */
  String value();

}
