package net.sf.enunciate.modules.xfire_client;

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
