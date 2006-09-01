package net.sf.enunciate.modules.xfire_client;

import net.sf.enunciate.samples.petclinic.PetType;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.MessageReader;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.fault.XFireFault;

/**
 * Reads and writes a PetType to/from an XML stream.
 *
 * @author Ryan Heaton
 */
public class ExampleEnumXFireType extends Type {

  /**
   * Reads a PetType from an XML stream.
   *
   * @param reader  The reader.
   * @param context The context.
   * @return The PetType.
   */
  public Object readObject(MessageReader reader, MessageContext context) throws XFireFault {
    String value = reader.getValue();

    if ("BIRD".equals(value)) {
      return PetType.BIRD;
    }
    else if ("".equals(value)) {
      return PetType.CAT;
    }
    else {
      throw new IllegalArgumentException("Unknown PetType: " + value);
    }
  }

  /**
   * Writes a PetType to an XML stream.
   *
   * @param object  The PetType to write.
   * @param writer  The writer.
   * @param context The context.
   */
  public void writeObject(Object object, MessageWriter writer, MessageContext context) throws XFireFault {
    switch ((PetType) object) {
      case BIRD:
        writer.writeValue("BIRD");
        return;
      case CAT:
        writer.writeValue("CAT");
        return;
      default:
        throw new IllegalArgumentException("No constant value known for PetType." + object);
    }
  }
}
