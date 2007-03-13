package org.codehaus.enunciate.template.freemarker;

import org.codehaus.enunciate.template.strategies.jaxws.WebMessageLoopStrategy;
import net.sf.jelly.apt.freemarker.FreemarkerTransform;

/**
 * Transform that iterates over each web message of a web method.
 *
 * @author Ryan Heaton
 */
public class ForEachWebMessageTransform extends FreemarkerTransform<WebMessageLoopStrategy> {

  /**
   * Construct a new transform under the specified namespace.  <code>null</code> or <code>""</code> means the root namespace.
   *
   * @param namespace The namespace.
   */
  public ForEachWebMessageTransform(String namespace) {
    super(namespace);
  }

  public WebMessageLoopStrategy newStrategy() {
    return new WebMessageLoopStrategy();
  }
}
