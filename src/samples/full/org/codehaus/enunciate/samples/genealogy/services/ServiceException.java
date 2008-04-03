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

package org.codehaus.enunciate.samples.genealogy.services;

import javax.xml.ws.WebFault;

/**
 * Generic fault for the genealogy API.
 *
 * @author Ryan Heaton
 */
@WebFault (
  targetNamespace = "http://enunciate.codehaus.org/samples/full"
)
public class ServiceException extends Exception {

  private String anotherMessage;

  public ServiceException(String message, String anotherMessage) {
    super(message);
    this.anotherMessage = anotherMessage;
  }

  /**
   * Some other message to pass in addition to the original message.
   *
   * @return Some other message to pass in addition to the original message.
   */
  public String getAnotherMessage() {
    return anotherMessage;
  }

  /**
   * Some other message to pass in addition to the original message.
   *
   * @param anotherMessage Some other message to pass in addition to the original message.
   */
  public void setAnotherMessage(String anotherMessage) {
    this.anotherMessage = anotherMessage;
  }
}
