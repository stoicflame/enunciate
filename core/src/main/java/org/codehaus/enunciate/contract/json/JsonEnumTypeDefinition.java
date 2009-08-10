package org.codehaus.enunciate.contract.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;

/**
 * <p>
 * A json enum type definition.
 * </p>
 *
 * @author Steven Cummings
 */
public final class JsonEnumTypeDefinition extends JsonTypeDefinition {
  private final Collection<EnumValue> enumValues;

  JsonEnumTypeDefinition(final EnumDeclaration delegate) {
    super(delegate);
    final Collection<EnumValue> enumValues = new ArrayList<EnumValue>(delegate.getEnumConstants().size());
    for (final EnumConstantDeclaration constantDeclaration : delegate.getEnumConstants()) {
      enumValues.add(new EnumValue(constantDeclaration.getSimpleName(), constantDeclaration.getDocComment()));
    }
    this.enumValues = Collections.unmodifiableCollection(enumValues);
  }

  /**
   * @return The enumValues.
   */
  public Collection<EnumValue> getEnumValues() {
    return enumValues;
  }

  public static final class EnumValue {
    private final String name;
    private final String description;

    private EnumValue(final String name, final String description) {
      assert name != null;
      this.name = name;
      this.description = description;
    }

    /**
     * @return Enum value name.
     */
    public String getName() {
      return name;
    }

    /**
     * @return Enum value description.
     */
    public String getDescription() {
      return description;
    }
  }
}
