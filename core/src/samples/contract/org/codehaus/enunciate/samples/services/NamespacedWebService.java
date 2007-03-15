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

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "http://enunciate.codehaus.org/samples/contract",
  name = "annotated-web-service"
)
public class NamespacedWebService {

  private boolean myPrivateMethod() {
    return false;
  }

  protected boolean myProtectedMethod() {
    return myPrivateMethod();
  }

  public boolean myPublicMethod() {
    return myProtectedMethod();
  }

  @WebMethod (
    exclude = true
  )
  public boolean myExcludedMethod() {
    return myPublicMethod();
  }
}
