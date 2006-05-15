package net.sf.enunciate.template.freemarker;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;
import net.sf.enunciate.decorations.QName;

import java.util.List;

/**
 * A method for use in the templates that will output the qualified name of an element given the namespace.
 *
 * @author Ryan Heaton
 */
public class QNameMethod implements TemplateMethodModel {

  /**
   * Returns the qname of the element that has the first parameter as the namespace, the second as the element.
   * @param list The arguments.
   * @return The qname.
   */
  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The qname method must have as parameters the namespace and the local name, in that order; local name optional.");
    }

    String namespace = (String) list.get(0);
    String element = (String) list.get(1);
    return new QName(namespace, element);
  }

}
