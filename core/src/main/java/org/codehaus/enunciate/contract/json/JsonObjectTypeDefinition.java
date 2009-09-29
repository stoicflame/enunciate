package org.codehaus.enunciate.contract.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;

import org.codehaus.enunciate.json.JsonName;
import org.codehaus.enunciate.json.JsonProperty;

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
    @SuppressWarnings("hiding")
    final Map<String, JsonPropertyDeclaration> propertiesByName = new HashMap<String, JsonPropertyDeclaration>();
    for (final PropertyDeclaration propertyDeclaration : getProperties()) {
      if (propertyDeclaration.getAnnotation(JsonProperty.class) != null) {
        JsonPropertyDeclaration property = new JsonPropertyDeclaration(propertyDeclaration);
        propertiesByName.put(property.getPropertyName(), property);
      }
    }
    this.propertiesByName = Collections.unmodifiableMap(propertiesByName);
  }

  /**
   * @return The propertiesByName.
   */
  public Map<String, JsonPropertyDeclaration> getPropertiesByName() {
    return propertiesByName;
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
      return jsonName == null ? getSimpleName() : jsonName.value();
    }

    /**
     * @return The description.
     */
    public String getPropertyDescription() {
      return getDocValue();
    }
  }
}
