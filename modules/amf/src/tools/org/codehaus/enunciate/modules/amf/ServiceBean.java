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

package org.codehaus.enunciate.modules.amf;

/**
 * Utility class for holding a service interface with its associated service bean.
 *
 * @author Ryan Heaton
 */
public class ServiceBean {

  private final Class serviceInterface;
  private final Object bean;

  public ServiceBean(Class serviceInterface, Object bean) {
    this.serviceInterface = serviceInterface;
    this.bean = bean;
  }

  /**
   * The service interface.
   *
   * @return The service interface.
   */
  public Class getServiceInterface() {
    return serviceInterface;
  }

  /**
   * The service bean.
   *
   * @return The service bean.
   */
  public Object getBean() {
    return bean;
  }
}
