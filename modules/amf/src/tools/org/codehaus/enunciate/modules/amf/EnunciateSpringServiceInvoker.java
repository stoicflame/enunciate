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

package org.codehaus.enunciate.modules.amf;

import org.granite.config.flex.Destination;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvoker;

/**
 * A service invoker for Enunciate.
 *
 * @author Ryan Heaton
 */
public class EnunciateSpringServiceInvoker extends ServiceInvoker<EnunciateSpringServiceFactory> {

  protected EnunciateSpringServiceInvoker(Destination destination, EnunciateSpringServiceFactory factory, ServiceBean serviceBean) throws ServiceException {
    super(destination, factory);
    this.invokee = serviceBean;
  }
}
