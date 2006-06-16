package net.sf.enunciate.contract.jaxb.types;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.ClassType;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.TypeDefinition;
import net.sf.jelly.apt.decorations.type.DecoratedClassType;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

/**
 * Decorator for an xml class type.
 *
 * @author Ryan Heaton
 */
public class XmlClassType extends DecoratedClassType implements XmlTypeMirror {

  private final TypeDefinition typeDef;

  public XmlClassType(ClassType delegate) throws XmlTypeException {
    super(delegate);
    ClassDeclaration classDeclaration = delegate.getDeclaration();
    if (classDeclaration == null) {
      throw new XmlTypeException("Unknown type definition: " + delegate);
    }
    this.typeDef = ((EnunciateFreemarkerModel) FreemarkerModel.get()).findOrCreateTypeDefinition(classDeclaration);
  }

  /**
   * The name of a class type depends on its type definition.
   *
   * @return The name of a class type depends on its type definition.
   */
  public String getName() {
    if (this.typeDef != null) {
      return this.typeDef.getName();
    }

    return null;
  }

  /**
   * The namespace of a class type depends on its type definition.
   *
   * @return The namespace of a class type depends on its type definition.
   */
  public String getNamespace() {
    if (this.typeDef != null) {
      return this.typeDef.getTargetNamespace();
    }

    return null;
  }

  /**
   * Whether a class type is anonymous depends on its type definition.
   *
   * @return Whether this class type is anonymous.
   */
  public boolean isAnonymous() {
    if (this.typeDef != null) {
      return this.typeDef.isAnonymous();
    }

    return true;
  }

}
