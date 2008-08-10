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

package org.codehaus.enunciate.modules.xfire_client.annotations;

import java.io.Serializable;

/**
 * JDK 1.4-usable metadata for a web fault.
 *
 * @author Ryan Heaton
 * @see javax.xml.ws.WebFault
 */
public class WebFaultAnnotation implements Serializable {

  private String name;
  private String targetNamespace;
  private String faultBean;
  private boolean implicitFaultBean;

  public WebFaultAnnotation(String name, String targetNamespace, String faultBean, boolean implicitFaultBean) {
    this.name = name;
    this.targetNamespace = targetNamespace;
    this.faultBean = faultBean;
    this.implicitFaultBean = implicitFaultBean;
  }

  /**
   * The local name of the web fault.
   *
   * @return The local name of the web fault.
   */
  public String name() {
    return this.name;
  }

  /**
   * The namespace of the web fault.
   *
   * @return The namespace of the web fault.
   */
  public String targetNamespace() {
    return this.targetNamespace;
  }

  /**
   * Whether the fault bean is implicit.
   *
   * @return true if the fault bean is implicit, false if it is explicit.
   */
  public boolean implicitFaultBean() {
    return this.implicitFaultBean;
  }

  /**
   * The fault bean.
   *
   * @return The fault bean.
   */
  public String faultBean() {
    return this.faultBean;
  }
}
