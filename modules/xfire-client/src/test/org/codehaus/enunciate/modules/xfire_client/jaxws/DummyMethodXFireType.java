package org.codehaus.enunciate.modules.xfire_client.jaxws;

import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;

/**
 * @author Ryan Heaton
 */
public class DummyMethodXFireType extends Type {

  public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
    return null;
  }

  public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
  }

  @Override
  public Class getTypeClass() {
    return DummyMethod.class;
  }

  @Override
  public QName getSchemaType() {
    return new QName("urn:doesntmatter", "anything");
  }
}
