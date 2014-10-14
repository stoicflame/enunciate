package org.codehaus.enunciate.contract.json;

import java.util.Collection;
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
    return schemaIdForPackage(schemaPackage);
  }

  /**
   * @param schemaPackage Package to get the associated schema ID for. Must not be null.
   * @return Non-null Schema ID associated with the given package.
   * @throws java.lang.AssertionError if the parameter conditions are not met and assertions are enabled.
   */
  public static String schemaIdForPackage(final PackageDeclaration schemaPackage) {
    assert schemaPackage != null : "schemaPackage:null";

    final JsonSchema jsonSchema = schemaPackage.getAnnotation(JsonSchema.class);
    if (jsonSchema != null && jsonSchema.schemaId() != null) {
      return jsonSchema.schemaId();
    }
    return schemaPackage.getQualifiedName();
  }

  private final String schemaId;
  private final String documentation;
  private final Map<String, JsonRootElementDeclaration> topLevelTypes = new HashMap<String, JsonRootElementDeclaration>();
  private final Map<String, JsonTypeDefinition> types = new HashMap<String, JsonTypeDefinition>();

  /**
   * Create a new JsonSchemaInfo.
   * @param schemaPackage Declaration to create a JSON schema from.
   */
  public JsonSchemaInfo(final PackageDeclaration schemaPackage) {
    assert schemaPackage != null : "schemaPackage:null";

    schemaId = schemaIdForPackage(schemaPackage);
    documentation = schemaPackage.getDocComment();
  }

  /**
   * @return The schema id.
   */
  public String getSchemaId() {
    return schemaId;
  }

  /**
   * @return Schema documentation.
   */
  public String getDocumentation() {
    return documentation;
  }

  /**
   * @return Non-null Map of top-level JSON types by type name, keyed by type-name.
   */
  public Map<String, JsonRootElementDeclaration> getTopLevelTypesByName() {
    return topLevelTypes;
  }

  /**
   * @return Non-null Map of available JSON types in this schema, keyed by type-name.
   */
  public Map<String, JsonTypeDefinition> getTypesByName() {
    return types;
  }

  /**
   * @return Non-null Collection of available JSON types in this schema.
   */
  public Collection<JsonTypeDefinition> getTypes() {
    return getTypesByName().values();
  }
}
