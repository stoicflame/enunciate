package org.codehaus.enunciate.contract.json;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;

import org.codehaus.enunciate.json.JsonIgnore;
import org.codehaus.enunciate.json.JsonName;

import com.sun.mirror.declaration.ClassDeclaration;

/**
 * <p>
 * A json object type definition.
 * </p>
 *
 * @author Steven Cummings
 */
public final class JsonObjectTypeDefinition extends JsonTypeDefinition {
  private final Map<String, JsonPropertyDeclaration> propertiesByName;

  JsonObjectTypeDefinition(final ClassDeclaration delegate) {
    super(delegate);
    final Map<String, JsonPropertyDeclaration> propertiesByName = new HashMap<String, JsonPropertyDeclaration>();
    for (final PropertyDeclaration propertyDeclaration : getProperties()) {
      if(propertyDeclaration.getAnnotation(JsonIgnore.class) != null) {
        continue;
      }

      final JsonPropertyDeclaration property = new JsonPropertyDeclaration(propertyDeclaration);
      propertiesByName.put(property.getPropertyName(), property);
    }
    this.propertiesByName = Collections.unmodifiableMap(propertiesByName);
  }

  /**
   * @return Non null Map of JsonPropertyDeclarations, keyed by name.
   */
  public Map<String, JsonPropertyDeclaration> getJsonPropertiesByName() {
    return propertiesByName;
  }

  /**
   * @return Non null Collection of JsonPropertyDeclarations.
   */
  public Collection<JsonPropertyDeclaration> getJsonProperties() {
    return getJsonPropertiesByName().values();
  }

  public static final class JsonPropertyDeclaration extends PropertyDeclaration {
    private JsonPropertyDeclaration(final PropertyDeclaration propertyDeclaration) {
      super(propertyDeclaration.getGetter(), propertyDeclaration.getSetter());
    }

    /**
     * @return The name.
     */
    public String getPropertyName() {
      JsonName jsonName = getAnnotation(JsonName.class);
      return jsonName == null ? super.getPropertyName() : jsonName.value();
    }

    /**
     * @return The description.
     */
    public String getPropertyDescription() {
      return getDocValue();
    }

    public String getPropertyTypeName()
    {
      return getPropertyType().toString();
    }
  }
}
