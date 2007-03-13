package org.codehaus.enunciate.modules.xfire_client.annotations;

import org.codehaus.xfire.annotations.WebMethodAnnotation;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Ryan Heaton
 */
public class SerializableWebMethodAnnotation extends WebMethodAnnotation implements Serializable {

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeBoolean(isExclude());
    out.writeObject(getAction());
    out.writeObject(getOperationName());
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    setExclude(in.readBoolean());
    setAction((String) in.readObject());
    setOperationName((String) in.readObject());
  }

}
