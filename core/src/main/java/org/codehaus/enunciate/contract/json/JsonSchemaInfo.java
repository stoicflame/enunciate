package org.codehaus.enunciate.contract.json;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.enunciate.json.JsonSchema;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;

/**
 * <p>
 * Information about JSON type structures present.
 * </p>
 *
 * @author Steven Cummings
 */
public final class JsonSchemaInfo {
  /**
   * Get the schema ID associated with the given class.
   *
   * @param delegate Class to get the associated schema ID for. Must not be null.
   * @return Non-null Schema ID associated with the given class.
   * @throws java.lang.AssertionError if the parameter conditions are not met and assertions are enabled.
   */
  public static String schemaIdForType(final ClassDeclaration delegate) {
    assert delegate != null;

    final PackageDeclaration schemaPackage = delegate.getPackage();
    // TODO is there any reason a ClassDeclaration.getPackage() would ever return null?
    String schemaId = schemaPackage.getQualifiedName();
    final JsonSchema jsonSchema = schemaPackage.getAnnotation(JsonSchema.class);
    if (jsonSchema != null) {
      if (jsonSchema.schemaId() != null) {
        schemaId = jsonSchema.schemaId();
      }
    }
    return schemaId;
  }

  private String schemaId;
  private final Map<String, JsonRootElementDeclaration> topLevelTypes = new HashMap<String, JsonRootElementDeclaration>();
  private final Map<String, JsonTypeDefinition> types = new HashMap<String, JsonTypeDefinition>();

  /**
   * @return The schema id.
   */
  public String getSchemaId() {
    return schemaId;
  }

  /**
   * @param schemaId The schema id.
   */
  public void setSchemaId(final String schemaId) {
    this.schemaId = schemaId;
  }

  /**
   * @return Non-null Map of top-level JSON types by type name, keyed by type-name.
   */
  public Map<String, JsonRootElementDeclaration> getTopLevelTypes() {
    return topLevelTypes;
  }

  /**
   * @return Non-null Map of available JSON types in this schema, keyed by type-name.
   */
  public Map<String, JsonTypeDefinition> getTypes() {
    return types;
  }
}
