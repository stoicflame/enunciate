package org.codehaus.enunciate.contract.json;

import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

import org.codehaus.enunciate.json.JsonType;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;

/**
 * <p>
 * A json type definition.
 * </p>
 *
 * @author Steven Cummings
 */
public class JsonTypeDefinition extends DecoratedClassDeclaration {

  public static JsonTypeDefinition createTypeDefinition(final ClassDeclaration delegate) {
    if (delegate instanceof EnumDeclaration) {
      return new JsonEnumTypeDefinition((EnumDeclaration) delegate);
    }
    else {
      return new JsonObjectTypeDefinition(delegate);
    }
  }

  protected JsonTypeDefinition(final ClassDeclaration delegate) {
    super(delegate);
  }

  public final ClassDeclaration classDeclaration() {
    return (ClassDeclaration) getDelegate();
  }

  protected final JsonType jsonType() {
    return getDelegate().getAnnotation(JsonType.class);
  }

  public final String getTypeName() {
    return jsonType() == null || jsonType().name() == null || jsonType().name().trim().length() == 0 ? classDeclaration().getQualifiedName() : jsonType().name();
  }
}
