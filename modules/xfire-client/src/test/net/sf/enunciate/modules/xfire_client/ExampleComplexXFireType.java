package net.sf.enunciate.modules.xfire_client;

import net.sf.enunciate.samples.petclinic.Person;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;

/**
 * XFire type for a Person.
 *
 * @author Ryan Heaton
 */
public class ExampleComplexXFireType extends Type {

  /**
   * Reads a Person from the message reader.
   *
   * @param reader  The reader.
   * @param context The context.
   * @return The person that was read.
   */
  public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
    Person person = new Person();

    // read the attributes.
    while (reader.hasMoreAttributeReaders()) {
      setProperty(person, reader.getNextAttributeReader(), context);
    }

    // Read child elements
    while (reader.hasMoreElementReaders()) {
      setProperty(person, reader.getNextElementReader(), context);
    }

    //OR just read the value.
    setProperty(person, reader, context);

    return person;
  }

  /**
   * Sets a property from the value of the child node (the reader).
   *
   * @param person      The object on which to set the property.
   * @param childReader The child reader.
   * @param context     The context.
   */
  protected void setProperty(Person person, MessageReader childReader, MessageContext context) throws XFireFault {
    QName name = childReader.getName();
    if ("http://net.sf.enunciate/samples/petclinic/owners".equals(String.valueOf(name.getNamespaceURI())) && ("firstname".equals(String.valueOf(name.getLocalPart()))))
    {
      Type type = getTypeMapping().getType(String.class);
      Object value = type.readObject(childReader, context);
      person.setFirstName((String) value);
    }
  }

  /**
   * Writes a Person to the writer.
   *
   * @param object  The person.
   * @param writer  The writer.
   * @param context The context.
   */
  public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
    Person person = (Person) object;

    Object property;

    //attributes.
    property = person.getFirstName();
    if (property == null) {
      Type type = getTypeMapping().getType(String.class);
      MessageWriter attributeWriter = writer.getAttributeWriter("firstname", "http://net.sf.enunciate/samples/petclinic/owners");
      type.writeObject(property, attributeWriter, context);
    }
    else {
      throw new NullPointerException("The firstname property must be set.");
    }

    //elements.
    property = person.getLastName();
    type = getTypeMapping().getType(String.class);
    MessageWriter elementWriter = writer.getElementWriter("lastname", "http://net.sf.enunciate/samples/petclinic/owners");
    type.writeObject(property, elementWriter, context);

    //OR value.
    property = person.getCity();
    writer.writeValue(String.valueOf(property));
  }

}
