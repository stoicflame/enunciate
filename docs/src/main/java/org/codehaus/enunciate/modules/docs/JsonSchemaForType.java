package org.codehaus.enunciate.modules.docs;

import java.util.List;

import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.common.rest.RESTResourcePayload;
import org.codehaus.enunciate.contract.json.JsonSchemaInfo;
import org.codehaus.enunciate.contract.json.JsonType;
import org.codehaus.enunciate.contract.json.JsonTypeDefinition;

import com.sun.mirror.type.TypeMirror;

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

  private final EnunciateFreemarkerModel model;

  public JsonSchemaForType(final EnunciateFreemarkerModel model)
  {
    assert model != null : "model must not be null";
    
    this.model = model;
  }

  /**
   * {@inheritDoc}
   */
  public JsonSchemaInfo exec(List arguments) throws TemplateModelException {
    if (arguments.size() != 1) {
      throw new TemplateModelException("The " + NAME + " method expects exactly one argument");
    }

    final Object object = BeansWrapper.getDefaultInstance().unwrap((TemplateModel) arguments.get(0));

    if (object instanceof String) {
      final String typeName = (String) object;
      final JsonType jsonType = model.findJsonTypeDefinition(typeName);
      return jsonSchemaForType(jsonType);
    }

    if (object instanceof TypeMirror) {
      TypeMirror typeMirror = (TypeMirror) object;
      final JsonType jsonType = model.findJsonTypeDefinition(typeMirror.toString());
      return jsonSchemaForType(jsonType);
    }

    if (object instanceof JsonType) {
      final JsonType jsonType = (JsonType) object;
      return jsonSchemaForType(jsonType);
    }

    if (object instanceof RESTResourcePayload) {
      final RESTResourcePayload restResourcePayload = (RESTResourcePayload) object;
      final JsonType jsonType = restResourcePayload.getJsonType();
      return jsonSchemaForType(jsonType);
    }
    return null;
  }

  private JsonSchemaInfo jsonSchemaForType(final JsonType jsonType) {
    if (jsonType instanceof JsonTypeDefinition) {
      final JsonTypeDefinition jsonTypeDefinition = (JsonTypeDefinition) jsonType;
      final String schemaId = JsonSchemaInfo.schemaIdForType(jsonTypeDefinition.classDeclaration());
      return model.getIdsToJsonSchemas().get(schemaId);
    }
    return null;
  }
}
