package net.sf.enunciate.modules.xfire_client.annotations;

import org.codehaus.xfire.annotations.WebServiceAnnotation;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Ryan Heaton
 */
public class SerializableWebServiceAnnotation extends WebServiceAnnotation implements Serializable {

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeObject(getEndpointInterface());
    out.writeObject(getName());
    out.writeObject(getPortName());
    out.writeObject(getServiceName());
    out.writeObject(getTargetNamespace());
    out.writeObject(getWsdlLocation());
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    setEndpointInterface((String) in.readObject());
    setName((String) in.readObject());
    setPortName((String) in.readObject());
    setServiceName((String) in.readObject());
    setTargetNamespace((String) in.readObject());
    setWsdlLocation((String) in.readObject());
  }

}
