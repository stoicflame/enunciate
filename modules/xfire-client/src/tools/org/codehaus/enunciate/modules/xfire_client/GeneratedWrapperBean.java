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

package org.codehaus.enunciate.modules.xfire_client;

import javax.xml.namespace.QName;

/**
 * A marker interface for generated wrapper beans (i.e. request wrappers, response wrappers, implicit fault beans).
 * <p/>
 * A generated wrapper bean must also conform to the following conventions in order to be correctly (de)serialized:
 *
 * <ul>
 *   <li>The beans conform to the JAXWS specification for request/response/fault bean wrappers.</li>
 *   <li>If the method has a collection or an array as a parameter, there is a special "addTo<i>Property</i>" method that can be used to add items
 *       to the collection or the array</li>
 *   <li>The supplied metadata contains the property order for each of the request/response beans so the binding can (de)serialize the parameters in
 *       the correct order.</li>
 * </ul>
 *
 * @author Ryan Heaton
 */
public interface GeneratedWrapperBean {

  /**
   * The qname of the wrapper thread.
   *
   * @return The qname of the wrapper thread.
   */
  public QName getWrapperQName();

}
