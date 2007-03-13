package org.codehaus.enunciate.modules.rest;

import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.activation.DataHandler;

/**
 * The Enunciate REST mechanism doesn't support attachments yet.
 *
 * @author Ryan Heaton
 */
public class RESTAttachmentUnmarshaller extends AttachmentUnmarshaller {
  
  public static final RESTAttachmentUnmarshaller INSTANCE = new RESTAttachmentUnmarshaller();

  /**
   * @throws UnsupportedOperationException
   */
  public DataHandler getAttachmentAsDataHandler(String string) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public byte[] getAttachmentAsByteArray(String string) {
    throw new UnsupportedOperationException();
  }
}
