package net.sf.enunciate.modules.xml;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.StringModel;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class QNameModel extends StringModel {

  private final QName qname;

  public QNameModel(QName qname, BeansWrapper wrapper) {
    super(qname, wrapper);
    this.qname = qname;
  }

  @Override
  public String getAsString() {
    String string = qname.getLocalPart();
    String prefix = lookupPrefix(qname.getNamespaceURI());
    if (prefix != null) {
      string = prefix + ":" + string;
    }
    return string;
  }

  /**
   * Convenience method to lookup a namespace prefix given a namespace.
   *
   * @param namespace The namespace for which to lookup the prefix.
   * @return The namespace prefix.
   */
  protected static String lookupPrefix(String namespace) {
    if ((namespace == null) || ("".equals(namespace))) {
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
    return getAsString();
  }


}
