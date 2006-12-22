package net.sf.enunciate.modules.rest;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentMarshaller;

/**
 * The Enunciate REST mechanism doesn't support attachments yet.
 *
 * @author Ryan Heaton
 */
public class RESTAttachmentMarshaller extends AttachmentMarshaller {

  public static final RESTAttachmentMarshaller INSTANCE = new RESTAttachmentMarshaller();

  /**
   * @return false;
   */
  @Override
  public boolean isXOPPackage() {
    return false;
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String addMtomAttachment(DataHandler dataHandler, String string, String string1) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String addMtomAttachment(byte[] bytes, int i, int i1, String string, String string1, String string2) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String addSwaRefAttachment(DataHandler dataHandler) {
    throw new UnsupportedOperationException();
  }

}
