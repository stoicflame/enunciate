package net.sf.enunciate.template.freemarker;

import net.sf.jelly.apt.freemarker.FreemarkerTransform;
import net.sf.enunciate.template.strategies.jaxws.WebMethodLoopStrategy;

/**
 * Transform that iterates over each web method of a given endpoint interface.
 *
 * @author Ryan Heaton
 */
public class ForEachWebMethodTransform extends FreemarkerTransform<WebMethodLoopStrategy> {

  /**
   * Construct a new transform under the specified namespace.  <code>null</code> or <code>""</code> means the root namespace.
   *
   * @param namespace The namespace.
   */
  public ForEachWebMethodTransform(String namespace) {
    super(namespace);
  }

  public WebMethodLoopStrategy newStrategy() {
    return new WebMethodLoopStrategy();
  }
}
