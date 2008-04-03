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

package org.codehaus.enunciate.samples.petclinic.services;

import javax.xml.ws.WebFault;

///CLOVER:OFF 

/**
 * @author Ryan Heaton
 */
@WebFault (
  targetNamespace = "http://org.codehaus.enunciate/samples/petclinic"
)
public class ServiceException extends Exception {

  /**
   * Constructs a ServiceException with no detail message.
   */
  public ServiceException() {
    super();
  }

  /**
   * Constructs a ServiceException with the specified detail message.
   *
   * @param message the detail message
   */
  public ServiceException(String message) {
    super(message);
  }

  /**
   * Constructs a ServiceException with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the cause
   */
  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a ServiceException with the specified cause.
   *
   * @param cause the cause
   */
  public ServiceException(Throwable cause) {
    super(cause);
  }
}