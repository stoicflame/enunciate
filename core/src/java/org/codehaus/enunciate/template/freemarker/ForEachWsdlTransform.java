package org.codehaus.enunciate.template.freemarker;

import net.sf.jelly.apt.freemarker.FreemarkerTransform;
import org.codehaus.enunciate.template.strategies.jaxws.WsdlLoopStrategy;

/**
 * Transform that iterates over each wsdl.
 *
 * @author Ryan Heaton
 */
public class ForEachWsdlTransform extends FreemarkerTransform<WsdlLoopStrategy> {

  /**
   * Construct a new transform under the specified namespace.  <code>null</code> or <code>""</code> means the root namespace.
   *
   * @param namespace The namespace.
   */
  public ForEachWsdlTransform(String namespace) {
    super(namespace);
  }

  public WsdlLoopStrategy newStrategy() {
    return new WsdlLoopStrategy();
  }
  
}
