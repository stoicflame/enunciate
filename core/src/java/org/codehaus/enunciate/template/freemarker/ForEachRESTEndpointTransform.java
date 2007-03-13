package org.codehaus.enunciate.template.freemarker;

import org.codehaus.enunciate.template.strategies.rest.RESTEndpointLoopStrategy;
import net.sf.jelly.apt.freemarker.FreemarkerTransform;

/**
 * @author Ryan Heaton
 */
public class ForEachRESTEndpointTransform extends FreemarkerTransform<RESTEndpointLoopStrategy> {

  /**
   * Construct a new transform under the specified namespace.  <code>null</code> or <code>""</code> means the root namespace.
   *
   * @param namespace The namespace.
   */
  public ForEachRESTEndpointTransform(String namespace) {
    super(namespace);
  }

  public RESTEndpointLoopStrategy newStrategy() {
    return new RESTEndpointLoopStrategy();
  }
}
