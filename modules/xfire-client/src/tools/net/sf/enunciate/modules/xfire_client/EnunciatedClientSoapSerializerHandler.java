package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.soap.handler.SoapSerializerHandler;
import org.codehaus.xfire.soap.AbstractSoapBinding;
import org.codehaus.xfire.soap.SoapSerializer;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFireException;

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

    context.getOutMessage().setSerializer(new SoapSerializer(binding.getSerializer()));
  }
}
