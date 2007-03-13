package org.codehaus.enunciate.modules.xfire_client;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.soap.SoapConstants;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.aegis.stax.ElementWriter;
import org.codehaus.xfire.aegis.stax.ElementReader;
import org.codehaus.xfire.aegis.type.mtom.DataHandlerType;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.util.UID;
import org.codehaus.xfire.util.Base64;
import org.codehaus.xfire.attachments.*;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Utility for handling attachments on the client-side for XFire clients.
 *
 * @author Ryan Heaton
 */
public class EnunciateClientBinaryDataUtil {

  /**
   * Indicates binary data should be written as a swa ref attachment.
   */
  public static final int SWAREF = 1;

  /**
   * Indicates binary data should be written as an MTOM attachment.
   */
  public static final int MTOM = 2;

  /**
   * Indicates binary data should be written as base 64 encoded text.
   */
  public static final int BASE64 = 3;

  private EnunciateClientBinaryDataUtil() {
  }

  /**
   * Writes binary data to the out message.
   *
   * @param data      The data to write.
   * @param mimeType  The mime type, defaults to "application/octet-stream" if null.
   * @param mechanism The mechanism to use (swaref, mtom, or base64).
   * @param writer    The element writer to which to write the data.
   * @param context   The message context.
   */
  public static void writeBinaryData(byte[] data, String mimeType, int mechanism, MessageWriter writer, MessageContext context) throws XFireFault {
    if (data == null) {
      throw new IllegalArgumentException("No data to write.");
    }

    if (mimeType == null) {
      mimeType = "application/octet-stream";
    }

    ByteDataSource source = new ByteDataSource(data);
    source.setContentType(mimeType);
    DataHandler handler = new DataHandler(source);
    writeBinaryData(handler, mimeType, mechanism, writer, context);
  }

  /**
   * Writes an image to the out message.
   *
   * @param image     The image.
   * @param mimeType  The mime type, defaults to "application/octet-stream" if null.
   * @param mechanism The mechanism to use (swaref, mtom, or base64).
   * @param writer    The element writer to which to write the image.
   * @param context   The message context.
   */
  public static void writeBinaryData(java.awt.Image image, String mimeType, int mechanism, MessageWriter writer, MessageContext context) throws XFireFault {
    throw new UnsupportedOperationException("No client-side serialization support yet for java.awt.Image.");
  }

  /**
   * Writes a source to the out message.
   *
   * @param source    The source.
   * @param mimeType  The suggested mime type.
   * @param mechanism The mechanism to use (swaref, mtom, or base64).
   * @param writer    The element writer to which to write data.
   * @param context   The message context.
   */
  public static void writeBinaryData(javax.xml.transform.Source source, String mimeType, int mechanism, MessageWriter writer, MessageContext context) throws XFireFault {
    throw new UnsupportedOperationException("No client-side serialization support yet for javax.xml.transform.Source.");
  }

  /**
   * Writes binary data to the out message.
   *
   * @param dataHandler The data handler.
   * @param mimeType    The suggested mime type.
   * @param mechanism   The mechanism to use (swaref, mtom, or base64).
   * @param writer      The element writer to which to write the data.
   * @param context     The message context.
   */
  public static void writeBinaryData(DataHandler dataHandler, String mimeType, int mechanism, MessageWriter writer, MessageContext context) throws XFireFault {
    if (!(writer instanceof ElementWriter)) {
      throw new IllegalArgumentException("Binary data can only be written to an element writer...");
    }

    boolean mtomEnabled = Boolean.valueOf(String.valueOf(context.getContextualProperty(SoapConstants.MTOM_ENABLED))).booleanValue();
    if ((!mtomEnabled) && (mechanism == MTOM)) {
      //disable MTOM as requested.
      mechanism = BASE64;
    }

    String namespace = ((ElementWriter) writer).getNamespace();
    Attachments attachments = context.getOutMessage().getAttachments();
    if (attachments == null) {
      attachments = new JavaMailAttachments();
      context.getOutMessage().setAttachments(attachments);
    }

    if (mechanism == MTOM) {
      DataHandlerType handlerType = new DataHandlerType();
      handlerType.setSchemaType(new QName(namespace, "nothing"));
      handlerType.writeObject(dataHandler, writer, context);
    }
    else if (mechanism == SWAREF) {
      String id = UID.generate() + "@" + dataHandler.getName();
      writer.writeValue(id);
      attachments.addPart(new SimpleAttachment(id, dataHandler));
    }
    else {
      writer.writeValue(Base64.encode(readBytes(dataHandler)));
    }
  }

  /**
   * Gets the attachment from the specified message context.
   *
   * @param dataType  The expected class of the binary data.
   * @param mechanism The mechanism to use (swaref, mtom, or base64).
   * @param reader    The reader from which to read the data.
   * @param context   The context.
   * @return The attachment that was read.
   */
  public static Object readBinaryData(Class dataType, int mechanism, MessageReader reader, MessageContext context) throws XFireFault {
    if (!(reader instanceof ElementReader)) {
      throw new IllegalArgumentException("Binary data can only be written to an element reader.");
    }

    ElementReader elementReader = (ElementReader) reader;
    DataHandler dataHandler = null;
    byte[] bytes = null;
    if ((mechanism == MTOM) && (elementReader.hasMoreElementReaders())) {
      DataHandlerType handlerType = new DataHandlerType();
      handlerType.setSchemaType(new QName(elementReader.getNamespace(), elementReader.getLocalName()));
      dataHandler = (DataHandler) handlerType.readObject(elementReader, context);
    }
    else if (mechanism == SWAREF) {
      String attachmentId = elementReader.getValue();
      Attachment attachment = AttachmentUtil.getAttachment(attachmentId, context.getInMessage());
      if (attachment == null) {
        throw new XFireFault("Unknown attachment ref: " + attachmentId, XFireFault.RECEIVER);
      }
      dataHandler = attachment.getDataHandler();
    }
    else {
      bytes = Base64.decode(elementReader.getValue());
    }

    if (DataHandler.class.isAssignableFrom(dataType)) {
      if (bytes != null) {
        return new DataHandler(new ByteArrayDataSource(bytes, null));
      }

      return dataHandler;
    }
    else if ((dataType.isArray()) && (dataType.getComponentType() == Byte.TYPE)) {
      if (dataHandler != null) {
        return readBytes(dataHandler);
      }

      return bytes;
    }
    else if (java.awt.Image.class.isAssignableFrom(dataType)) {
      throw new UnsupportedOperationException("No client-side serialization support yet for java.awt.Image.");
    }
    else if (javax.xml.transform.Source.class.isAssignableFrom(dataType)) {
      throw new UnsupportedOperationException("No client-side serialization support yet for javax.xml.transform.Source.");
    }
    else {
      throw new UnsupportedOperationException("No client-side serialization support for " + dataType.getName());
    }
  }

  /**
   * Reads the bytes of the specified data handler to a byte array.
   *
   * @param dataHandler The data handler to read.
   * @return The bytes that were read.
   */
  public static byte[] readBytes(DataHandler dataHandler) throws XFireFault {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      InputStream in = dataHandler.getInputStream();
      byte[] inBytes = new byte[1024 * 10]; //10 K?
      int len;
      while ((len = in.read(inBytes)) > 0) {
        out.write(inBytes, 0, len);
      }

      return out.toByteArray();
    }
    catch (IOException e) {
      throw new XFireFault(e, XFireFault.RECEIVER);
    }
  }

}
