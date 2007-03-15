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

import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;

/**
 * Interface for encapsulating callback logic for reading child elements of an element wrapper.
 *
 * @author Ryan Heaton
 * @see org.codehaus.enunciate.modules.xfire_client.ElementsUtil
 */
public interface WrappedItemCallback {

  /**
   * Logic for handling the child element of a wrapper.
   *
   * @param name The qname of the child element.
   * @param elementReader The reader for the child element.
   * @param context The context.
   */
  void handleChildElement(QName name, MessageReader elementReader, MessageContext context) throws XFireFault;

}
