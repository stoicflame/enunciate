package net.sf.enunciate.template.freemarker;

import net.sf.enunciate.template.strategies.jaxb.SchemaLoopStrategy;
import net.sf.jelly.apt.freemarker.FreemarkerTransform;

/**
 * @author Ryan Heaton
 */
public class ForEachSchemaTransform extends FreemarkerTransform<SchemaLoopStrategy> {

  /**
   * Construct a new transform under the specified namespace.  <code>null</code> or <code>""</code> means the root namespace.
   *
   * @param namespace The namespace.
   */
  public ForEachSchemaTransform(String namespace) {
    super(namespace);
  }

  public SchemaLoopStrategy newStrategy() {
    return new SchemaLoopStrategy();
  }
}
