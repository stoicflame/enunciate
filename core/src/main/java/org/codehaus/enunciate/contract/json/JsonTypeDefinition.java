package org.codehaus.enunciate.contract.json;

import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

import org.codehaus.enunciate.json.JsonName;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

/**
 * <p>
 * A json type definition.
 * </p>
 *
 * @author Steven Cummings
 */
public abstract class JsonTypeDefinition extends DecoratedClassDeclaration implements JsonType {
  /**
   * Create a new JsonTypeDefinition. If delegate is an {@link EnumDeclaration} then a {@link JsonEnumTypeDefinition} is created. Otherwise a {@link JsonObjectTypeDefinition} is created.
   * @param delegate Declaration to create a JSON type from (must not be null).
   * @return Non-null JsonTypeDefinition.
   */
  public static JsonTypeDefinition createTypeDefinition(final ClassDeclaration delegate) {
    if (delegate instanceof EnumDeclaration) {
      return new JsonEnumTypeDefinition((EnumDeclaration) delegate);
    }
    return new JsonObjectTypeDefinition(delegate);
  }

  /**
   * Create a new JsonTypeDefinition.
   * @param delegate Declaration to create a JSON type from (must not be null).
   */
  protected JsonTypeDefinition(final ClassDeclaration delegate) {
    super(delegate);
  }

  /**
   * @return Non-null ClassDeclaration that is the source for this JSON type.
   */
  public final ClassDeclaration classDeclaration() {
    return (ClassDeclaration) getDelegate();
  }

  /**
   * {@inheritDoc}
   */
  public final String getTypeName() {
    return getTypeName(classDeclaration());
  }

  /**
   * @param delegate Type declaration to get a JSON type name for (must not be null).
   * @return JSON type name for the given declaration.
   */
  public static String getTypeName(final TypeDeclaration delegate) {
    JsonName jsonName = delegate.getAnnotation(JsonName.class);
    return jsonName == null ? delegate.getSimpleName() : jsonName.value();
  }
}
