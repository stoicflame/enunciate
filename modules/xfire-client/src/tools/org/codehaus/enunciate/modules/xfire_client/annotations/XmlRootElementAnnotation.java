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

package org.codehaus.enunciate.modules.xfire_client.annotations;

import java.io.Serializable;

/**
 * JDK 1.4-usable metadata for an xml root element.
 *
 * @author Ryan Heaton
 */
public class XmlRootElementAnnotation implements Serializable {

  private String name;
  private String namespace;

  public XmlRootElementAnnotation(String namespace, String name) {
    this.namespace = namespace;
    this.name = name;
  }

  /**
   * The name of the element.
   *
   * @return The name of the element.
   */
  public String name() {
    return this.name;
  }

  /**
   * The namespace of the element.
   *
   * @return The namespace of the element.
   */
  public String namespace() {
    return this.namespace;
  }
}
