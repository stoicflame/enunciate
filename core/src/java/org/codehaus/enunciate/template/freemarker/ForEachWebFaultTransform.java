package org.codehaus.enunciate.template.freemarker;

import net.sf.jelly.apt.freemarker.FreemarkerTransform;
import org.codehaus.enunciate.template.strategies.jaxws.WebFaultLoopStrategy;

/**
 * Transform for iterating over each web fault of a WSDL.
 *
 * @author Ryan Heaton
 */
public class ForEachWebFaultTransform extends FreemarkerTransform<WebFaultLoopStrategy> {

  /**
   * Construct a new transform under the specified namespace.  <code>null</code> or <code>""</code> means the root namespace.
   *
   * @param namespace The namespace.
   */
  public ForEachWebFaultTransform(String namespace) {
    super(namespace);
  }

  public WebFaultLoopStrategy newStrategy() {
    return new WebFaultLoopStrategy();
  }
}
