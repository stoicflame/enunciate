package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.aegis.type.TypeMapping;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;
import java.lang.reflect.Array;
import java.util.Collection;

/**
 * A utility used to write a list of child elements to a parent writer.
 *
 * @author Ryan Heaton
 */
public class ElementsWriter {

  /**
   * Special qname to pass to the elements writer to indicate that the element name should be looked up and used for the
   * qname of the element.
   */
  public static final QName ELEMENT_NAME = new QName("", "");

  private ElementsWriter() {
  }

  /**
   * Write the items in the specified collection as child elements of the specified parent writer.
   *
   * @param items        The collection of items.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  public static void writeElements(Collection items, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    writeElements(items == null ? (Object) null : items.toArray(), elementName, parentWriter, typeMapping, context, false);
  }

  /**
   * Write the items in the specified array as child elements of the specified parent writer.
   *
   * @param items        The items.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  public static void writeElements(Object[] items, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    writeElements((Object) items, elementName, parentWriter, typeMapping, context, false);
  }

  /**
   * Write the xml ids of the items in the specified collection as child elements of the specified parent writer.
   *
   * @param items        The collection of items.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  public static void writeXmlIDs(Collection items, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    writeElements(items == null ? (Object) null : items.toArray(), elementName, parentWriter, typeMapping, context, true);
  }

  /**
   * Write the xml ids of the items in the specified array as child elements of the specified parent writer.
   *
   * @param items        The items.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  public static void writeXmlIDs(Object[] items, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    writeElements((Object) items, elementName, parentWriter, typeMapping, context, true);
  }

  /**
   * Write the boolean items in the specified array as child elements of the specified parent writer.
   *
   * @param items        The items.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  public static void writeElements(boolean[] items, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    writeElements((Object) items, elementName, parentWriter, typeMapping, context, false);
  }

  /**
   * Write the byte items in the specified array as child elements of the specified parent writer.
   *
   * @param items        The items.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  public static void writeElements(byte[] items, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    writeElements((Object) items, elementName, parentWriter, typeMapping, context, false);
  }

  /**
   * Write the char items in the specified array as child elements of the specified parent writer.
   *
   * @param items        The items.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  public static void writeElements(char[] items, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    writeElements((Object) items, elementName, parentWriter, typeMapping, context, false);
  }

  /**
   * Write the double items in the specified array as child elements of the specified parent writer.
   *
   * @param items        The items.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  public static void writeElements(double[] items, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    writeElements((Object) items, elementName, parentWriter, typeMapping, context, false);
  }

  /**
   * Write the float items in the specified array as child elements of the specified parent writer.
   *
   * @param items        The items.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  public static void writeElements(float[] items, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    writeElements((Object) items, elementName, parentWriter, typeMapping, context, false);
  }

  /**
   * Write the int items in the specified array as child elements of the specified parent writer.
   *
   * @param items        The items.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  public static void writeElements(int[] items, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    writeElements((Object) items, elementName, parentWriter, typeMapping, context, false);
  }

  /**
   * Write the int items in the specified array as child elements of the specified parent writer.
   *
   * @param items        The items.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  public static void writeElements(long[] items, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    writeElements((Object) items, elementName, parentWriter, typeMapping, context, false);
  }

  /**
   * Write the int items in the specified array as child elements of the specified parent writer.
   *
   * @param items        The items.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  public static void writeElements(short[] items, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    writeElements((Object) items, elementName, parentWriter, typeMapping, context, false);
  }

  /**
   * Write the items in the specified array as child elements of the specified parent writer.
   *
   * @param array        The array.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @param writeOnlyXmlID Whether to write only the xml id instead of the entire object.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  private static void writeElements(Object array, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context, boolean writeOnlyXmlID) throws XFireFault {
    if (array != null) {
      for (int i = 0; i < Array.getLength(array); i++) {
        Object item = Array.get(array, i);
        Type componentType = typeMapping.getType(item.getClass());

        QName itemName = elementName;
        if (ELEMENT_NAME == elementName) {
          try {
            itemName = ((EnunciatedType) componentType).getRootElementName();
          }
          catch (ClassCastException e) {
            throw new XFireFault(item.getClass().getName() + " is not a root element, but it's being serialized as an element ref.", XFireFault.RECEIVER);
          }
        }

        MessageWriter elementWriter = parentWriter.getElementWriter(itemName);
        if (!writeOnlyXmlID) {
          componentType.writeObject(item, elementWriter, context);
        }
        else {
          try {
            ((EnunciatedType) componentType).writeXmlID(item, elementWriter);
          }
          catch (ClassCastException e) {
            throw new XFireFault(item.getClass().getName() + " does not have an xml id.", XFireFault.RECEIVER);
          }
        }
        elementWriter.close();
      }
    }
  }

}
