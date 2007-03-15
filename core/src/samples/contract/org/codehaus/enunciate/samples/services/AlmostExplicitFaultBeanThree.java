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

package org.codehaus.enunciate.samples.services;

import org.codehaus.enunciate.samples.schema.BeanThree;

import javax.xml.ws.WebFault;

/**
 * @author Ryan Heaton
 */
@WebFault (
  name = "ignored-name",
  targetNamespace = "urn:ignored",
  faultBean = "org.codehaus.enunciate.IgnoredFaultBean"
)
public class AlmostExplicitFaultBeanThree extends Exception {

  private BeanThree faultInfo;

  public AlmostExplicitFaultBeanThree(String message, BeanThree faultInfo) {
    super(message);
    this.faultInfo = faultInfo;
  }

  protected AlmostExplicitFaultBeanThree(String message, BeanThree faultInfo, Throwable throwable) {
    super(message, throwable);
    this.faultInfo = faultInfo;
  }

  public BeanThree getFaultInfo() {
    return faultInfo;
  }

}
