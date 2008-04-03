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

package org.codehaus.enunciate.modules.spring_app.config;

/**
 * Configuration of a handler interceptor.
 *
 * @author Ryan Heaton
 */
public class HandlerInterceptor {

  private Class interceptorClass;
  private String beanName;

  /**
   * The class of the handler interceptor.
   *
   * @return The class of the handler interceptor.
   */
  public Class getInterceptorClass() {
    return interceptorClass;
  }

  /**
   * The class of the handler interceptor.
   *
   * @param clazz The class of the handler interceptor.
   */
  public void setInterceptorClass(Class clazz) {
    this.interceptorClass = clazz;
  }

  /**
   * The bean name of the handler interceptor.
   *
   * @return The bean name of the handler interceptor.
   */
  public String getBeanName() {
    return beanName;
  }

  /**
   * The bean name of the handler interceptor.
   *
   * @param beanName The bean name of the handler interceptor.
   */
  public void setBeanName(String beanName) {
    this.beanName = beanName;
  }

}
