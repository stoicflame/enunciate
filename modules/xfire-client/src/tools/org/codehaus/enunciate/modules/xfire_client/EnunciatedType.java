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

import org.codehaus.xfire.aegis.MessageWriter;

import javax.xml.namespace.QName;

/**
 * Marker interface for enunciated xfire types.
 *
 * @author Ryan Heaton
 */
public interface EnunciatedType {

  /**
   * The qname of the xml root element.
   *
   * @return The qname of the xml root element.
   * @throws UnsupportedOperationException If this type isn't a root element.
   */
  public QName getRootElementName();
  
  /**
   * Write the value of the xml id of the specified object to the specified writer.
   *
   * @param instance The instance.
   * @param writer The writer.
   * @throws UnsupportedOperationException If this type doesn't have an xml id.
   * @throws ClassCastException if <code>instance</code> is of the wrong type for this xfire type.
   */
  void writeXmlID(Object instance, MessageWriter writer);
}
