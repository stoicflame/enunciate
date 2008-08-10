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
 * Indicates support for <a href="http://ajaxian.com/archives/jsonp-json-with-padding">JSONP</a> callbacks.
 * When applied to a method, the method must be a read method.  When applied to a class or interface, all "read"
 * methods will support JSONP.  When applied to a package, all methods of all classes will support
 * the specified JSONP parameter.
 *
 * @author Ryan Heaton
 */
@Retention (
  RetentionPolicy.RUNTIME
)
@Target (
  { ElementType.METHOD, ElementType.TYPE, ElementType.PACKAGE }
)
public @interface JSONP {

  /**
   * The parameter name of the JSONP callback.
   *
   * @return The parameter name.
   */
  String paramName() default "callback";
}
