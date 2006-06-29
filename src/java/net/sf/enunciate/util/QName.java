package net.sf.enunciate.util;

import freemarker.ext.beans.InvalidPropertyException;
import freemarker.template.*;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.util.Map;

/**
 * A qname that overrides the toString() to output "prefix:localPart".
 *
 * @author Ryan Heaton
 */
public class QName extends javax.xml.namespace.QName implements TemplateHashModel, TemplateScalarModel {

  public static final String EMPTY_PREFIX = "<empty-prefix>";

  /**
   * A qname of the given namespace and localpart.  Prefix will be looked up.
   *
   * @param namespaceURI The namespace.
   * @param localPart    The local part.
   */
  public QName(String namespaceURI, String localPart) {
    super(namespaceURI, localPart, lookupPrefix(namespaceURI));

  }

  /**
   * A qname of the given namespace, localpart, and prefix.
   *
   * @param namespaceURI The namespace.
   * @param localPart    The local part.
   * @param prefix       The prefix.
   */
  public QName(String namespaceURI, String localPart, String prefix) {
    super(namespaceURI, localPart, prefix);
  }

  //Inherited.
  public TemplateModel get(String property) throws TemplateModelException {
    if ("prefix".equalsIgnoreCase(property)) {
      return new SimpleScalar(getPrefix());
    }
    else if ("namespaceURI".equalsIgnoreCase(property)) {
      return new SimpleScalar(getNamespaceURI());
    }
    else if ("localPart".equalsIgnoreCase(property)) {
      return new SimpleScalar(getLocalPart());
    }
    else {
      throw new InvalidPropertyException(property);
    }
  }

  //Inherited.
  public boolean isEmpty() throws TemplateModelException {
    return false;
  }

  //Inherited.
  public String getAsString() throws TemplateModelException {
    return toString();
  }

  /**
   * Convenience method to lookup a namespace prefix given a namespace.
   *
   * @param namespace The namespace for which to lookup the prefix.
   * @return The namespace prefix.
   */
  protected static String lookupPrefix(String namespace) {
    if (namespace == null) {
      return EMPTY_PREFIX;
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
    if (getPrefix() != EMPTY_PREFIX) {
      string = getPrefix() + ":" + string;
    }
    return string;
  }


}
