package net.sf.enunciate.template.freemarker;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;
import net.sf.enunciate.util.QName;

import java.util.List;

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
      throw new TemplateModelException("The qname method must have a namespace as a parameter.");
    }

    String namespace = (String) list.get(0);
    return new SimpleScalar(new QName(namespace, "").getPrefix());
  }

}
