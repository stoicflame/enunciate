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

package org.codehaus.enunciate.main.webapp;

import java.util.Set;
import java.util.Map;

/**
 * Component for web application, e.g. filter, servlet, listener.
 *
 * @author Ryan Heaton
 */
public class WebAppComponent {

  private String name;
  private String classname;
  private Set<String> urlMappings;
  private Map<String, String> initParams;

  /**
   * The servlet name.
   *
   * @return The servlet name.
   */
  public String getName() {
    return name;
  }

  /**
   * The servlet name.
   *
   * @param name The servlet name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The classname of the component.
   *
   * @return The classname of the component.
   */
  public String getClassname() {
    return classname;
  }

  /**
   * The classname of the component.
   *
   * @param classname The classname of the component.
   */
  public void setClassname(String classname) {
    this.classname = classname;
  }

  /**
   * The URL mappings of the component.
   *
   * @return The URL mappings of the component.
   */
  public Set<String> getUrlMappings() {
    return urlMappings;
  }

  /**
   * The URL mappings of the component.
   *
   * @param urlMappings The URL mappings of the component.
   */
  public void setUrlMappings(Set<String> urlMappings) {
    this.urlMappings = urlMappings;
  }

  /**
   * The init params of the component.
   *
   * @return The init params of the component.
   */
  public Map<String, String> getInitParams() {
    return initParams;
  }

  /**
   * The init params of the component.
   *
   * @param initParams The init params of the component.
   */
  public void setInitParams(Map<String, String> initParams) {
    this.initParams = initParams;
  }
}
