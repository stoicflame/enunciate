package net.sf.enunciate.template.freemarker;

import net.sf.jelly.apt.freemarker.FreemarkerTransform;
import net.sf.enunciate.template.strategies.jaxws.ThrownWebFaultLoopStrategy;

/**
 * Transform for iterating over each thrown web fault of a specified web method.
 *
 * @author Ryan Heaton
 */
public class ForEachThrownWebFaultTransform extends FreemarkerTransform<ThrownWebFaultLoopStrategy> {

  /**
   * Construct a new transform under the specified namespace.  <code>null</code> or <code>""</code> means the root namespace.
   *
   * @param namespace The namespace.
   */
  public ForEachThrownWebFaultTransform(String namespace) {
    super(namespace);
  }

  public ThrownWebFaultLoopStrategy newStrategy() {
    return new ThrownWebFaultLoopStrategy();
  }
}
