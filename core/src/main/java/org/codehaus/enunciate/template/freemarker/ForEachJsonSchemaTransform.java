package org.codehaus.enunciate.template.freemarker;

import org.codehaus.enunciate.template.strategies.json.JsonSchemaLoopStrategy;

import net.sf.jelly.apt.freemarker.FreemarkerTransform;

/**
 * <p>
 * Transform for looping over JSON schemas.
 * </p>
 * @author Steven Cummings
 */
public class ForEachJsonSchemaTransform extends FreemarkerTransform<JsonSchemaLoopStrategy> {
  /**
   * Create a new ForEachJsonSchemaTransform with the given namespace.
   * @param namespace The namespace.
   */
  public ForEachJsonSchemaTransform(String namespace) {
    super(namespace);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JsonSchemaLoopStrategy newStrategy() {
    return new JsonSchemaLoopStrategy();
  }
}
