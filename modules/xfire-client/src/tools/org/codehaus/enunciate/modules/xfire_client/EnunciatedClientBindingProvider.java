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

import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.aegis.type.TypeMappingRegistry;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.MessagePartContainer;

/**
 * An enunciate-specific client binding provider.
 * 
 * @author Ryan Heaton
 */
public class EnunciatedClientBindingProvider extends AegisBindingProvider {

  public EnunciatedClientBindingProvider(TypeMappingRegistry registry) {
    super(registry);
  }

  /**
   * No-op.  Message parts are not initialized because enunciate uses the JAXWS-specified logic for
   * (de)serializing messages, which is to deserialize the request/response wrappers.
   *
   * @param service The service.
   * @param container The container.
   * @param type The type of the message.
   */
  protected void initializeMessage(Service service, MessagePartContainer container, int type) {
    //no-op...
  }


}
