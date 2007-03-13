package org.codehaus.enunciate.modules.xfire_client.annotations;

import org.codehaus.xfire.annotations.WebParamAnnotation;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Ryan Heaton
 */
public class SerializableWebParamAnnotation extends WebParamAnnotation implements Serializable {

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeBoolean(isHeader());
    out.writeInt(getMode());
    out.writeObject(getName());
    out.writeObject(getPartName());
    out.writeObject(getTargetNamespace());
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    setHeader(in.readBoolean());
    setMode(in.readInt());
    setName((String) in.readObject());
    setPartName((String) in.readObject());
    setTargetNamespace((String) in.readObject());
  }

}
