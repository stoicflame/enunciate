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

package org.codehaus.enunciate.samples.xfire_client;

import javax.xml.ws.WebFault;

/**
 * @author Ryan Heaton
 */
@WebFault (
  name = "bf2",
  targetNamespace = "urn:bf2",
  faultBean = "net.nothing.BasicFault2"
)
public class BasicFaultTwo extends Exception {

  private String anotherMessage;

  public String getAnotherMessage() {
    return anotherMessage;
  }

  public void setAnotherMessage(String anotherMessage) {
    this.anotherMessage = anotherMessage;
  }
}
