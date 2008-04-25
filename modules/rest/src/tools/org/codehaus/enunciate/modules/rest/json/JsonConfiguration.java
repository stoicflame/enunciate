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

package org.codehaus.enunciate.modules.rest.json;

/**
 * JSON handler configuration options.
 *
 * @author Ryan Heaton
 */
public class JsonConfiguration {

  private JsonSerializationMethod defaultSerializationMethod = JsonSerializationMethod.xmlMapped;
  private XStreamReferenceAction xstreamReferenceAction = null;

  /**
   * The default serialization method.
   *
   * @return The default serialization method.
   */
  public JsonSerializationMethod getDefaultSerializationMethod() {
    return defaultSerializationMethod;
  }

  /**
   * The default serialization method.
   *
   * @param defaultSerializationMethod The default serialization method.
   */
  public void setDefaultSerializationMethod(JsonSerializationMethod defaultSerializationMethod) {
    this.defaultSerializationMethod = defaultSerializationMethod;
  }

  /**
   * The action to take on an XStream reference.
   *
   * @return The action to take on an XStream reference.
   */
  public XStreamReferenceAction getXstreamReferenceAction() {
    return xstreamReferenceAction;
  }

  /**
   * The action to take on an XStream reference.
   *
   * @param xstreamReferenceAction The action to take on an XStream reference.
   */
  public void setXstreamReferenceAction(XStreamReferenceAction xstreamReferenceAction) {
    this.xstreamReferenceAction = xstreamReferenceAction;
  }
}
