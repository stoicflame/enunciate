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
    writeElements(items == null ? (Object) null : items.toArray(), elementName, parentWriter, typeMapping, context);
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
    writeElements((Object) items, elementName, parentWriter, typeMapping, context);
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
    writeElements((Object) items, elementName, parentWriter, typeMapping, context);
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
    writeElements((Object) items, elementName, parentWriter, typeMapping, context);
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
    writeElements((Object) items, elementName, parentWriter, typeMapping, context);
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
    writeElements((Object) items, elementName, parentWriter, typeMapping, context);
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
    writeElements((Object) items, elementName, parentWriter, typeMapping, context);
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
    writeElements((Object) items, elementName, parentWriter, typeMapping, context);
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
    writeElements((Object) items, elementName, parentWriter, typeMapping, context);
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
    writeElements((Object) items, elementName, parentWriter, typeMapping, context);
  }

  /**
   * Write the items in the specified array as child elements of the specified parent writer.
   *
   * @param array        The array.
   * @param elementName  The qname of the child element.
   * @param parentWriter The parent writer.
   * @param typeMapping  The type mapping.
   * @param context      The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  private static void writeElements(Object array, QName elementName, MessageWriter parentWriter, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    if (array != null) {
      for (int i = 0; i < Array.getLength(array); i++) {
        Object item = Array.get(array, i);
        Type componentType = typeMapping.getType(item.getClass());
        MessageWriter elementWriter = parentWriter.getElementWriter(elementName);
        componentType.writeObject(item, elementWriter, context);
        elementWriter.close();
      }
    }
  }

}
