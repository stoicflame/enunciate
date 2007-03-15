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

import javax.xml.ws.WebFault;

/**
 * @author Ryan Heaton
 */
@WebFault (
  name = "implicit-fault",
  targetNamespace = "urn:implicit-fault",
  faultBean = "org.codehaus.enunciate.ImplicitFaultBean"
)
public class ImplicitWebFaultTwo extends ImplicitWebFault {

  private double property4;

  public double getProperty4() {
    return property4;
  }

  public void setProperty4(double property4) {
    this.property4 = property4;
  }
}
