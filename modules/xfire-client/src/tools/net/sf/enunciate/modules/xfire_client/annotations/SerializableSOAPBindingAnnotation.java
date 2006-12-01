package net.sf.enunciate.modules.xfire_client.annotations;

import org.codehaus.xfire.annotations.soap.SOAPBindingAnnotation;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Ryan Heaton
 */
public class SerializableSOAPBindingAnnotation extends SOAPBindingAnnotation implements Serializable {

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeInt(getParameterStyle());
    out.writeInt(getStyle());
    out.writeInt(getUse());
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    setParameterStyle(in.readInt());
    setStyle(in.readInt());
    setUse(in.readInt());
  }

}
