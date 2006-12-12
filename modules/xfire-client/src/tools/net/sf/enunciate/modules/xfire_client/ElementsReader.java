package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;

/**
 * A reader for a wrapper element (has a bunch of items)...
 *
 * @author Ryan Heaton
 */
public class ElementsReader {

  /**
   * Read each child element of the specified wrapper reader and issue the callback command for each.
   *
   * @param wrapperReader The wrapper reader.
   * @param context The context.
   * @param itemCallback The callback.
   */
  public static void readElements(MessageReader wrapperReader, MessageContext context, WrappedItemCallback itemCallback) throws XFireFault {
    while (wrapperReader.hasMoreElementReaders()) {
      MessageReader elementReader = wrapperReader.getNextElementReader();
      QName name = elementReader.getName();
      itemCallback.handleChildElement(name, elementReader, context);
      elementReader.readToEnd();
    }
  }
}
