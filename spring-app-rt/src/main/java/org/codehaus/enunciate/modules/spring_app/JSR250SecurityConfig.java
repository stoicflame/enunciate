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

package org.codehaus.enunciate.modules.spring_app;

import org.springframework.security.SecurityConfig;

import javax.annotation.security.PermitAll;
import javax.annotation.security.DenyAll;

/**
 * Security config applicable as a JSR 250 annotation attribute.
 *
 * @author Ryan Heaton
 */
public class JSR250SecurityConfig extends SecurityConfig {

  public static final JSR250SecurityConfig PERMIT_ALL_ATTRIBUTE = new JSR250SecurityConfig(PermitAll.class.getName());
  public static final JSR250SecurityConfig DENY_ALL_ATTRIBUTE = new JSR250SecurityConfig(DenyAll.class.getName());

  public JSR250SecurityConfig(String role) {
    super(role);
  }

}
