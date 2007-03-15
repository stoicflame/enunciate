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
import org.codehaus.xfire.exchange.OutMessage;
import org.codehaus.xfire.soap.SoapConstants;
import org.codehaus.xfire.transport.http.CommonsHttpMessageSender;

import java.io.IOException;

/**
 * @author Ryan Heaton
 */
public class EnunciatedHttpMessageSender extends CommonsHttpMessageSender {

  public EnunciatedHttpMessageSender(OutMessage message, MessageContext context) {
    super(message, context);
  }

  public void open() throws IOException, XFireException {
    super.open();

    MessageContext context = getMessageContext();
    boolean mtomEnabled = Boolean.valueOf(String.valueOf(context.getContextualProperty(SoapConstants.MTOM_ENABLED))).booleanValue();

    String acceptHeaderValue = "*/*";

    if (mtomEnabled) {
      acceptHeaderValue = "application/xop+xml, " + acceptHeaderValue;
    }

    getMethod().setRequestHeader("Accept", acceptHeaderValue);
  }
}
