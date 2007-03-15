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

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFireException;
import org.codehaus.xfire.soap.AbstractSoapBinding;
import org.codehaus.xfire.soap.handler.SoapSerializerHandler;

/**
 * A serializer handler that sets the serializer for the out message by consulting the binding.
 *
 * @author Ryan Heaton
 */
public class EnunciatedClientSoapSerializerHandler extends SoapSerializerHandler {

  public void invoke(MessageContext context) throws Exception {
    AbstractSoapBinding binding = (AbstractSoapBinding) context.getBinding();
    if (binding == null) {
      throw new XFireException("Couldn't find the binding!");
    }

    context.getOutMessage().setSerializer(binding.getSerializer());
  }
}
