package net.sf.enunciate.util;

import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.util.Map;

/**
 * A qname that overrides the toString() to output "prefix:localPart".
 *
 * @author Ryan Heaton
 */
public class QName {

  private String namespaceURI;
  private String localPart;

  /**
   * A qname of the given namespace and localpart.  Prefix will be looked up.
   *
   * @param namespaceURI The namespace.
   * @param localPart    The local part.
   */
  public QName(String namespaceURI, String localPart) {
    this.namespaceURI = namespaceURI;
    this.localPart = localPart;
  }

  /**
   * The namespace URI, null if the default namespace.
   *
   * @return The namespace URI.
   */
  public String getNamespaceURI() {
    return this.namespaceURI;
  }

  /**
   * The local part.
   *
   * @return The local part.
   */
  public String getLocalPart() {
    return this.localPart;
  }

  /**
   * The prefix, or null if the default.
   *
   * @return The prefix, or null if the default.
   */
  public String getPrefix() {
    return lookupPrefix(getNamespaceURI());
  }

  /**
   * Convenience method to lookup a namespace prefix given a namespace.
   *
   * @param namespace The namespace for which to lookup the prefix.
   * @return The namespace prefix.
   */
  protected static String lookupPrefix(String namespace) {
    if (namespace == null) {
      return null;
    }

    return getNamespacesToPrefixes().get(namespace);
  }

  /**
   * The namespace to prefix map.
   *
   * @return The namespace to prefix map.
   */
  protected static Map<String, String> getNamespacesToPrefixes() {
    return getModel().getNamespacesToPrefixes();
  }

  /**
   * Get the current root model.
   *
   * @return The current root model.
   */
  protected static EnunciateFreemarkerModel getModel() {
    return ((EnunciateFreemarkerModel) FreemarkerModel.get());
  }

  @Override
  public String toString() {
    String string = getLocalPart();
    if (getPrefix() != null) {
      string = getPrefix() + ":" + string;
    }
    return string;
  }


}
