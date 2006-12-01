package net.sf.enunciate.modules.xfire_client.annotations;

import org.codehaus.xfire.annotations.WebResultAnnotation;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Ryan Heaton
 */
public class SerializableWebResultAnnotation extends WebResultAnnotation implements Serializable {
  
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeBoolean(isHeader());
    out.writeObject(getName());
    out.writeObject(getPartName());
    out.writeObject(getTargetNamespace());
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    setHeader(in.readBoolean());
    setName((String) in.readObject());
    setPartName((String) in.readObject());
    setTargetNamespace((String) in.readObject());
  }

}
