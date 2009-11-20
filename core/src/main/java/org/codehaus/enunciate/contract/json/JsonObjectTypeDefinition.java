package org.codehaus.enunciate.contract.json;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import org.codehaus.enunciate.json.JsonIgnore;
import org.codehaus.enunciate.json.JsonName;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;

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

    private final boolean isList;
    private final TypeMirror targetType;

    private JsonPropertyDeclaration(final PropertyDeclaration propertyDeclaration) {
      super(propertyDeclaration.getGetter(), propertyDeclaration.getSetter());

      // TODO Last remaining problem: I'm only getting the FQN from the TypeMirror. At doc time I need to be able to resolve the simple-or-given name

      DecoratedTypeMirror decoratedPropertyType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(getPropertyType());
      isList = decoratedPropertyType.isCollection() || decoratedPropertyType.isArray();
      if (isList && getPropertyType() instanceof DeclaredType) {
        DeclaredType declaredType = (DeclaredType) getPropertyType();
        Collection<TypeMirror> actualTypeArguments = declaredType.getActualTypeArguments();
        if(actualTypeArguments != null && actualTypeArguments.size() == 1) {
          targetType = TypeMirrorDecorator.decorate(actualTypeArguments.iterator().next());
        } else {
          targetType = getPropertyType();
        }
      } else {
        targetType = getPropertyType();
      }
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

    public boolean isList() {
      return isList;
    }

    public String getTypeName() {
      return targetType.toString();
    }

    public TypeMirror getTargetType() {
      return targetType;
    }
  }
}
