package net.sf.enunciate.modules.xfire_client;

import net.sf.enunciate.samples.petclinic.Person;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.aegis.type.TypeMapping;
import org.codehaus.xfire.fault.XFireFault;

/**
 * Logic for reading and writing a Something to an xml stream.
 *
 * @author Ryan Heaton
 */
public class ExampleSimpleXFireType extends Type {

  /**
   * Reads a Something from the value given..
   *
   * @param reader  The reader to read from.
   * @param context The context.
   * @return The Something.
   */
  public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
    Person instance = new Person();

    Type type = getTypeMapping().getType(String.class);
    Object value = type.readObject(reader, context);
    instance.setFirstName((String) value);

    return instance;
  }

  /**
   * Writes the value for a Something to a writer.
   *
   * @param object  The Something.
   * @param writer  The writer.
   * @param context The context.
   */
  public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
    Person person = (Person) object;

    Object value = person.getCity();

    //if it's an XmlList:
    int[] values = new int[10];
    TypeMapping typeMapping = getTypeMapping();
    Type componentType = typeMapping.getType(Integer.class);
    ListWriter listWriter = new ListWriter((Object[]) null, null, null);
    for (int i = 0; i < values.length; i++) {
      Integer item = new Integer(values[i]);
      componentType.writeObject(item, listWriter, context);
    }
    value = listWriter.getValue();

    //but if nillable, write nill.
    writer.writeValue(String.valueOf(value));
    writer.writeXsiNil();
  }
}
