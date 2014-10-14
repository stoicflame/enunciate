package org.codehaus.enunciate.contract.json;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.JavaDoc.JavaDocTagList;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import org.codehaus.enunciate.json.JsonIgnore;
import org.codehaus.enunciate.json.JsonName;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.ArrayType;
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

      DecoratedTypeMirror decoratedPropertyType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(getPropertyType());
      isList = decoratedPropertyType.isCollection() || decoratedPropertyType.isArray();

      if (decoratedPropertyType.isCollection() && getPropertyType() instanceof DeclaredType) {
        final DeclaredType declaredType = (DeclaredType) getPropertyType();
        final Collection<TypeMirror> actualTypeArguments = declaredType.getActualTypeArguments();
        if(actualTypeArguments != null && actualTypeArguments.size() == 1) {
          targetType = TypeMirrorDecorator.decorate(actualTypeArguments.iterator().next());
        } else {
          targetType = getPropertyType();
        }
      } else if (decoratedPropertyType.isArray() && getPropertyType() instanceof ArrayType) {
        final ArrayType arrayType = (ArrayType) getPropertyType();
        targetType = TypeMirrorDecorator.decorate(arrayType.getComponentType());
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
      final String docValue = getDocValue();
      if(docValue != null && docValue.trim().length() > 0) {
        return docValue;
      }
      JavaDocTagList javaDocTagList = getJavaDoc().get("return");
      if(javaDocTagList == null) {
        return null;
      }
      StringBuilder builder = new StringBuilder();
      boolean firstValue = true;
      for(String value : javaDocTagList)
      {
        if(firstValue) {
          firstValue = false;
        } else {
          builder.append('\n');
        }
        builder.append(value);
      }
      return builder.toString();
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
