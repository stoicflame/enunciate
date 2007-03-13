package org.codehaus.enunciate.template.freemarker;

import org.codehaus.enunciate.template.strategies.jaxws.BindingTypeLoopStrategy;
import net.sf.jelly.apt.freemarker.FreemarkerTransform;

/**
 * Tranform for iterating over each binding type of an endpoint interface.
 *
 * @author Ryan Heaton
 */
public class ForEachBindingTypeTransform extends FreemarkerTransform<BindingTypeLoopStrategy> {

  /**
   * Construct a new transform under the specified namespace.  <code>null</code> or <code>""</code> means the root namespace.
   *
   * @param namespace The namespace.
   */
  public ForEachBindingTypeTransform(String namespace) {
    super(namespace);
  }

  public BindingTypeLoopStrategy newStrategy() {
    return new BindingTypeLoopStrategy();
  }
}
