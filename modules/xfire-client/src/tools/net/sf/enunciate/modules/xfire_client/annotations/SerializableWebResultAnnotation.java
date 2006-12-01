package net.sf.enunciate.modules.xfire_client.annotations;

import org.codehaus.xfire.annotations.WebResultAnnotation;

import java.io.*;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;

/**
 * @author Ryan Heaton
 */
public class SerializableWebResultAnnotation extends WebResultAnnotation implements Serializable {
  
  private SerializableWebServiceAnnotation temp;

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    XMLEncoder encoder = new XMLEncoder(bytesOut);
    encoder.writeObject(this);
    encoder.close();
    out.writeObject(bytesOut.toByteArray());
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    byte[] bytesIn = (byte[]) in.readObject();
    XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(bytesIn));
    this.temp = (SerializableWebServiceAnnotation) decoder.readObject();
    decoder.close();
  }

  Object readResolve() throws ObjectStreamException {
    return temp;
  }

}
