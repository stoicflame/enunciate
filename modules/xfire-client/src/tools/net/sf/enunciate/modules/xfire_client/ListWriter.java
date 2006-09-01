package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.aegis.AbstractMessageWriter;
import org.codehaus.xfire.aegis.MessageWriter;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A writer used to compile a list of simple xml types.
 * <p/>
 * Every time a value is written to this ListWriter, it's string form is appended
 * to a whitespace-separated list.  The value of this list can be retrieved from
 * the
 *
 * @author Ryan Heaton
 */
public class ListWriter extends AbstractMessageWriter {

  private final ArrayList items = new ArrayList();

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
   * @return the current list value.
   */
  public String getValue() {
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
    return null;
  }

  /**
   * @throws UnsupportedOperationException Because a list is a simple type.
   */
  public MessageWriter getAttributeWriter(String name, String namespace) {
    return null;
  }

  /**
   * @throws UnsupportedOperationException Because a list is a simple type.
   */
  public MessageWriter getAttributeWriter(QName qname) {
    return null;
  }

  /**
   * @throws UnsupportedOperationException Because a list is a simple type.
   */
  public MessageWriter getElementWriter(String name) {
    return null;
  }

  /**
   * @throws UnsupportedOperationException Because a list is a simple type.
   */
  public MessageWriter getElementWriter(String name, String namespace) {
    return null;
  }

  /**
   * @throws UnsupportedOperationException Because a list is a simple type.
   */
  public MessageWriter getElementWriter(QName qname) {
    return null;
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String getPrefixForNamespace(String namespace) {
    return null;
  }

  /**
   * @throws UnsupportedOperationException
   */
  public String getPrefixForNamespace(String namespace, String hint) {
    return null;
  }

  /**
   * No-op
   */
  public void close() {
  }


}
