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
 * Marks a method parameter as a context parameter.  Context parameters are parameters that can be passed
 * through the noun context. The {@link NounContext} specifies a parameter by surrounding the name
 * of that parameter with braces ("{" and "}") in the context.
 * <p/>
 * For example, a noun context of "feeds/activities/user/{userId}" defines a context parameter "userId" that
 * will be passed as the parameter annotated with @ContextParameter("userId").
 * <p/>
 * If a context parameter is defined in the noun context and there is no associated parameter in the method, the
 * context parameter will be ignored for the request that maps to that method.
 *
 * @author Ryan Heaton
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ( ElementType.PARAMETER )
public @interface ContextParameter {

  /**
   * The name of the context parameter.
   *
   * @return The name of the context parameter.
   */
  String value();

}
