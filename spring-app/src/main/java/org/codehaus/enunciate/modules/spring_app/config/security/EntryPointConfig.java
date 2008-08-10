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

package org.codehaus.enunciate.modules.spring_app.config.security;

/**
 * Configuration for a security entry point.
 *
 * @author Ryan Heaton
 */
public class EntryPointConfig {

  private String redirectTo;
  private BeanReference useEntryPoint;

  /**
   * The entry point should redirect to the specified URL.
   * 
   * @return The entry point should redirect to the specified URL.
   */
  public String getRedirectTo() {
    return redirectTo;
  }

  /**
   * The entry point should redirect to the specified URL.
   *
   * @param redirectTo The entry point should redirect to the specified URL.
   */
  public void setRedirectTo(String redirectTo) {
    this.redirectTo = redirectTo;
  }

  /**
   * The bean to use as the entry point.
   *
   * @return The bean to use as the entry point.
   */
  public BeanReference getUseEntryPoint() {
    return useEntryPoint;
  }

  /**
   * The bean to use as the entry point.
   *
   * @param useEntryPoint The bean to use as the entry point.
   */
  public void setUseEntryPoint(BeanReference useEntryPoint) {
    this.useEntryPoint = useEntryPoint;
  }
}
