package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.aegis.AbstractMessageWriter;
import org.codehaus.xfire.aegis.MessageWriter;
import org.codehaus.xfire.aegis.type.Type;
import org.codehaus.xfire.aegis.type.TypeMapping;
import org.codehaus.xfire.fault.XFireFault;

import javax.xml.namespace.QName;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * A writer used to compile a list of simple xml types.
 * <p/>
 * Every time a value is written to this ListWriter, it's string form is appended
 * to a whitespace-separated list.  The value of this list can be retrieved from
 * the {@link #getValue} method.
 *
 * @author Ryan Heaton
 */
public class ListWriter extends AbstractMessageWriter {

  private final ArrayList items = new ArrayList();

  /**
   * Construct a list writer for a collection of items.
   *
   * @param items       The items.
   * @param typeMapping The type mapping, used to lookup the type for the components.
   * @param context     The context.
   */
  public ListWriter(Collection items, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    this(items == null ? (Object) null : items.toArray(), typeMapping, context);
  }

  /**
   * Construct a list writer for an array of items.
   *
   * @param items       The items.
   * @param typeMapping The type mapping, used to lookup the type for the components.
   * @param context     The context.
   */
  public ListWriter(Object[] items, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    this((Object) items, typeMapping, context);
  }

  /**
   * Construct a list writer for an array of boolean items.
   *
   * @param items       The items.
   * @param typeMapping The type mapping, used to lookup the type for the components.
   * @param context     The context.
   */
  public ListWriter(boolean[] items, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    this((Object) items, typeMapping, context);
  }

  /**
   * Construct a list writer for an array of byte items.
   *
   * @param items       The items.
   * @param typeMapping The type mapping, used to lookup the type for the components.
   * @param context     The context.
   */
  public ListWriter(byte[] items, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    this((Object) items, typeMapping, context);
  }

  /**
   * Construct a list writer for an array of char items.
   *
   * @param items       The items.
   * @param typeMapping The type mapping, used to lookup the type for the components.
   * @param context     The context.
   */
  public ListWriter(char[] items, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    this((Object) items, typeMapping, context);
  }

  /**
   * Construct a list writer for an array of double items.
   *
   * @param items       The items.
   * @param typeMapping The type mapping, used to lookup the type for the components.
   * @param context     The context.
   */
  public ListWriter(double[] items, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    this((Object) items, typeMapping, context);
  }

  /**
   * Construct a list writer for an array of float items.
   *
   * @param items       The items.
   * @param typeMapping The type mapping, used to lookup the type for the components.
   * @param context     The context.
   */
  public ListWriter(float[] items, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    this((Object) items, typeMapping, context);
  }

  /**
   * Construct a list writer for an array of int items.
   *
   * @param items       The items.
   * @param typeMapping The type mapping, used to lookup the type for the components.
   * @param context     The context.
   */
  public ListWriter(int[] items, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    this((Object) items, typeMapping, context);
  }

  /**
   * Construct a list writer for an array of long items.
   *
   * @param items       The items.
   * @param typeMapping The type mapping, used to lookup the type for the components.
   * @param context     The context.
   */
  public ListWriter(long[] items, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    this((Object) items, typeMapping, context);
  }

  /**
   * Construct a list writer for an array of short items.
   *
   * @param items       The items.
   * @param typeMapping The type mapping, used to lookup the type for the components.
   * @param context     The context.
   */
  public ListWriter(short[] items, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    this((Object) items, typeMapping, context);
  }

  /**
   * Construct the list writer with the specified array.
   *
   * @param array       The array.
   * @param typeMapping The type mapping.
   * @param context     The context.
   * @throws IllegalArgumentException if the object isn't an array.
   */
  private ListWriter(Object array, TypeMapping typeMapping, MessageContext context) throws XFireFault {
    if (array != null) {
      for (int i = 0; i < Array.getLength(array); i++) {
        Object item = Array.get(array, i);
        Type componentType = typeMapping.getType(item.getClass());
        componentType.writeObject(item, this, context);
      }
    }
  }

  /**
   * Adds the string form of the specified value to the list.
   *
   * @param value The value to add to the list.
   * @throws IllegalArgumentException If the string form of the value contains a whitespace.
   */
  public void writeValue(Object value) {
    if (value == null) {
      return;
    }

    String stringForm = String.valueOf(value);
    if ((stringForm.indexOf(' ') >= 0)
      || (stringForm.indexOf('\n') >= 0)
      || (stringForm.indexOf('\t') >= 0)
      || (stringForm.indexOf('\r') >= 0)
      || (stringForm.indexOf('\f') >= 0)
      || (stringForm.indexOf(0x0B) >= 0)) {
      throw new IllegalArgumentException("A value in an xml list cannot contain a whitespace.  Offending value: " + stringForm);
    }

    items.add(stringForm);
  }

  /**
   * Get the current list value.
   *
   * @return the current list value, or null if no items have been added.
   */
  public String getValue() {
    if (items.size() == 0) {
      return null;
    }

    StringBuffer buffer = new StringBuffer();
    Iterator it = items.iterator();
    while (it.hasNext()) {
      buffer.append((String) it.next());
      if (it.hasNext()) {
        buffer.append(' ');
      }
    }

    return buffer.toString();
  }

  /**
   * @throws UnsupportedOperationException Because a list is a simple type.
   */
  public MessageWriter getAttributeWriter(String name) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException Because a list is a simple type.
   */
  public MessageWriter getAttributeWriter(String name, String namespace) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException Because a list is a simple type.
   */
  public MessageWriter getAttributeWriter(QName qname) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException Because a list is a simple type.
   */
  public MessageWriter getElementWriter(String name) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException Because a list is a simple type.
   */
  public MessageWriter getElementWriter(String name, String namespace) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException Because a list is a simple type.
   */
  public MessageWriter getElementWriter(QName qname) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String getPrefixForNamespace(String namespace) {
    throw new UnsupportedOperationException();
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String getPrefixForNamespace(String namespace, String hint) {
    throw new UnsupportedOperationException();
  }

  /**
   * No-op
   */
  public void close() {
  }

}
