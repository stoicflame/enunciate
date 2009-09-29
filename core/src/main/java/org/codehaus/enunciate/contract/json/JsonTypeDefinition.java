package org.codehaus.enunciate.contract.json;

import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;

import org.codehaus.enunciate.json.JsonName;
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
public abstract class JsonTypeDefinition extends DecoratedClassDeclaration {

  public static JsonTypeDefinition createTypeDefinition(final ClassDeclaration delegate) {
    if (delegate instanceof EnumDeclaration) {
      return new JsonEnumTypeDefinition((EnumDeclaration) delegate);
    }
    return new JsonObjectTypeDefinition(delegate);
  }

  protected JsonTypeDefinition(final ClassDeclaration delegate) {
    super(delegate);
  }

  public final ClassDeclaration classDeclaration() {
    return (ClassDeclaration) getDelegate();
  }

  public final String getTypeName() {
    JsonName jsonName = getDelegate().getAnnotation(JsonName.class);
    return jsonName == null ? classDeclaration().getQualifiedName() : jsonName.value();
  }
}
