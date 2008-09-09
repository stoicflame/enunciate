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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Same semantics as @XmlSeeAlso for JAXB 2.1, but can be applied to REST methods and endpoints to use to initialize the context.
 *
 * @author Ryan Heaton
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ( { ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD } )
public @interface RESTSeeAlso {

  /**
   * List of additional classes to use to initialize the JAXB context.
   *
   * @return List of additional classes to use to initialize the JAXB context.
   */
  Class[] value() default {};
}
