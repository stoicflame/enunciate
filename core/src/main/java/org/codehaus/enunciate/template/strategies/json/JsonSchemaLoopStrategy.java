package org.codehaus.enunciate.template.strategies.json;

import java.util.Iterator;

import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;

import org.codehaus.enunciate.contract.json.JsonSchemaInfo;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;

/**
 * <p>
 * Strategy for looping over JSON schemas.
 * </p>
 * @author Steven Cummings
 */
public class JsonSchemaLoopStrategy extends EnunciateTemplateLoopStrategy<JsonSchemaInfo> {

  private static final String LOOP_VARIABLE_NAME = "schema";
  private JsonSchemaInfo currentSchema;

  /**
   * {@inheritDoc}
   */
  @Override
  protected Iterator<JsonSchemaInfo> getLoop(@SuppressWarnings("unused") final TemplateModel model) {
    return getIdsToJsonSchemas().values().iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void setupModelForLoop(TemplateModel model, JsonSchemaInfo schema, int index) throws TemplateException {
    super.setupModelForLoop(model, schema, index);

    if (schema != null) {
      getModel().setVariable(LOOP_VARIABLE_NAME, schema);
    }

    currentSchema = schema;
  }

  /**
   * The current schema in the loop.
   * @return The current schema in the loop.
   */
  public JsonSchemaInfo getCurrentSchema() {
    return currentSchema;
  }
}
