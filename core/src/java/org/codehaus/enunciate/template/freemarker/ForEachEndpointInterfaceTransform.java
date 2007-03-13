package org.codehaus.enunciate.template.freemarker;

import net.sf.jelly.apt.freemarker.FreemarkerTransform;
import org.codehaus.enunciate.template.strategies.jaxws.EndpointInterfaceLoopStrategy;

/**
 * Iterates through each endpoint interface of a WSDL.
 *
 * @author Ryan Heaton
 */
public class ForEachEndpointInterfaceTransform extends FreemarkerTransform<EndpointInterfaceLoopStrategy> {

  /**
   * Construct a new transform under the specified namespace.  <code>null</code> or <code>""</code> means the root namespace.
   *
   * @param namespace The namespace.
   */
  public ForEachEndpointInterfaceTransform(String namespace) {
    super(namespace);
  }

  public EndpointInterfaceLoopStrategy newStrategy() {
    return new EndpointInterfaceLoopStrategy();
  }
}
