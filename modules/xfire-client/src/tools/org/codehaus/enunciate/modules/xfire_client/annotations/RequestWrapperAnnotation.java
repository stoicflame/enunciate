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
 * JDK 1.4-usable metadata for a request wrapper.
 *
 * @author Ryan Heaton
 * @see javax.xml.ws.RequestWrapper
 */
public class RequestWrapperAnnotation implements Serializable {

  private String localName;
  private String targetNamespace;
  private String className;

  public RequestWrapperAnnotation(String localName, String targetNamespace, String className) {
    this.localName = localName;
    this.targetNamespace = targetNamespace;
    this.className = className;
  }

  public String localName() {
    return this.localName;
  }

  public String targetNamespace() {
    return this.targetNamespace;
  }

  public String className() {
    return this.className;
  }

}
