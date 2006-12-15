package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;

/**
 * Interface for encapsulating callback logic for reading child elements of an element wrapper.
 *
 * @author Ryan Heaton
 * @see net.sf.enunciate.modules.xfire_client.ElementsUtil
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
