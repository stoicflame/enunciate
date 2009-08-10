package org.codehaus.enunciate.contract.json;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;

import org.codehaus.enunciate.json.JsonProperty;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.TypeMirror;

/**
 * <p>
 * A json object type definition.
 * </p>
 *
 * @author Steven Cummings
 */
public final class JsonObjectTypeDefinition extends JsonTypeDefinition {
  private final Map<String, Property> propertiesByName;

  JsonObjectTypeDefinition(final ClassDeclaration delegate) {
    super(delegate);
    final Map<String, Property> propertiesByName = new HashMap<String, Property>();
    for (final PropertyDeclaration propertyDeclaration : getProperties()) {
      final JsonProperty jsonProperty = propertyDeclaration.getAnnotation(JsonProperty.class);
      if (jsonProperty != null) {
        final String name = jsonProperty.name() == null || jsonProperty.name().trim().length() == 0 ? propertyDeclaration.getSimpleName() : jsonProperty.name();
        final Property property = new Property(name, propertyDeclaration.getDocValue(), TypeMirrorDecorator.decorate(propertyDeclaration.getPropertyType()));

        propertiesByName.put(name, property);
      }
    }
    this.propertiesByName = Collections.unmodifiableMap(propertiesByName);
  }

  /**
   * @return The propertiesByName.
   */
  public Map<String, Property> getPropertiesByName() {
    return propertiesByName;
  }

  public static final class Property {
    private final String name;
    private final String description;
    private final TypeMirror type;

    private Property(final String name, final String description, final TypeMirror type) {
      assert name != null;
      assert type != null;

      this.name = name;
      this.description = description;
      this.type = type;
    }

    /**
     * @return The name.
     */
    public String getName() {
      return name;
    }

    /**
     * @return The description.
     */
    public String getDescription() {
      return description;
    }

    /**
     * @return The type.
     */
    public TypeMirror getType() {
      return type;
    }
  }
}
