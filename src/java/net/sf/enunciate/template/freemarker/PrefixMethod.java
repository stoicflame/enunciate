package net.sf.enunciate.template.freemarker;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.util.List;
import java.util.Map;

/**
 * A method used in templates to output the prefix for a given namespace.
 *
 * @author Ryan Heaton
 */
public class PrefixMethod implements TemplateMethodModel {

  /**
   * Returns the qname of the element that has the first parameter as the namespace, the second as the element.
   *
   * @param list The arguments.
   * @return The qname.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The prefix method must have a namespace as a parameter.");
    }

    String namespace = (String) list.get(0);
    String prefix = lookupPrefix(namespace);
    if (prefix == null) {
      throw new TemplateModelException("No prefix specified for {" + namespace + "}");
    }
    return prefix;
  }

  /**
   * Convenience method to lookup a namespace prefix given a namespace.
   *
   * @param namespace The namespace for which to lookup the prefix.
   * @return The namespace prefix.
   */
  protected static String lookupPrefix(String namespace) {
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

}
