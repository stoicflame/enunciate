package org.codehaus.enunciate.modules.docs;

import java.util.List;
import java.util.Map;

import org.codehaus.enunciate.contract.common.rest.RESTResourcePayload;
import org.codehaus.enunciate.contract.json.JsonSchemaInfo;
import org.codehaus.enunciate.contract.json.JsonType;
import org.codehaus.enunciate.contract.json.JsonTypeDefinition;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * <p>
 * Finds the schema for a given JSON type.
 * </p>
 * @author Steven Cummings
 */
public class JsonSchemaForType implements TemplateMethodModelEx {

  /**
   * Method name.
   */
  public static final String NAME = "jsonSchemaForType";

  private final Map<String, JsonSchemaInfo> idsToJsonSchemas;

  public JsonSchemaForType(final Map<String, JsonSchemaInfo> idsToJsonSchemas)
  {
    assert idsToJsonSchemas != null : "idsToJsonSchemas:null";

    this.idsToJsonSchemas = idsToJsonSchemas;
  }

  /**
   * {@inheritDoc}
   */
  public JsonSchemaInfo exec(List arguments) throws TemplateModelException {
    if (arguments.size() != 1) {
      throw new TemplateModelException("The " + NAME + " method expects exactly one argument");
    }

    Object object = BeansWrapper.getDefaultInstance().unwrap((TemplateModel) arguments.get(0));

    if (object instanceof JsonTypeDefinition) {
      final JsonTypeDefinition jsonTypeDefinition = (JsonTypeDefinition) object;
      return idsToJsonSchemas.get(JsonSchemaInfo.schemaIdForType(jsonTypeDefinition.classDeclaration()));
    }

    if (object instanceof RESTResourcePayload) {
      final RESTResourcePayload restResourcePayload = (RESTResourcePayload) object;
      final JsonType jsonType = restResourcePayload.getJsonType();
      if (jsonType instanceof JsonTypeDefinition) {
        JsonTypeDefinition jsonTypeDefinition = (JsonTypeDefinition) jsonType;
        final String schemaId = JsonSchemaInfo.schemaIdForType(jsonTypeDefinition.classDeclaration());
        return idsToJsonSchemas.get(schemaId);
      }
    }
    return null;
  }
}
