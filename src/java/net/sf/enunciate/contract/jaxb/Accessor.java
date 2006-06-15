package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.Types;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.declaration.DecoratedMemberDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import javax.xml.bind.annotation.XmlList;
import java.util.Collection;

/**
 * An accessor for a field or method value into a type.
 *
 * @author Ryan Heaton
 */
public abstract class Accessor extends DecoratedMemberDeclaration {

  private final TypeDefinition typeDefinition;

  public Accessor(MemberDeclaration delegate, TypeDefinition typeDef) {
    super(delegate);

    this.typeDefinition = typeDef;
  }

  /**
   * The name of the accessor.
   *
   * @return The name of the accessor.
   */
  public abstract String getName();

  /**
   * The namespace of the accessor.
   *
   * @return The namespace of the accessor.
   */
  public abstract String getNamespace();

  /**
   * The type of the accessor.
   *
   * @return The type of the accessor.
   */
  public DecoratedTypeMirror getAccessorType() {
    TypeMirror propertyType;
    Declaration delegate = getDelegate();
    if (delegate instanceof FieldDeclaration) {
      propertyType = ((FieldDeclaration) delegate).getType();
    }
    else {
      propertyType = ((PropertyDeclaration) delegate).getPropertyType();
    }

    return (DecoratedTypeMirror) TypeMirrorDecorator.decorate(propertyType);
  }

  /**
   * The base type of the accessor. The base type is either:
   * <p/>
   * <ol>
   * <li>The accessor type.</li>
   * <li>The component type of the accessor type if the accessor type is a collection type.</li>
   * </ol>
   *
   * @return The base type.
   */
  public DecoratedTypeMirror getBaseType() {
    DecoratedTypeMirror baseType = getAccessorType();

    if (baseType.isArray()) {
      baseType = (DecoratedTypeMirror) ((ArrayType) baseType).getComponentType();
    }
    else if (baseType.isCollection()) {
      Collection<TypeMirror> actualTypeArguments = ((DeclaredType) baseType).getActualTypeArguments();
      if (actualTypeArguments.isEmpty()) {
        AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
        Types types = env.getTypeUtils();
        TypeDeclaration declaration = env.getTypeDeclaration(Object.class.getName());
        return (DecoratedTypeMirror) TypeMirrorDecorator.decorate(types.getDeclaredType(declaration));
      }

      return (DecoratedTypeMirror) actualTypeArguments.iterator().next();
    }

    return baseType;
  }

  /**
   * The type definition for this accessor.
   *
   * @return The type definition for this accessor.
   */
  public TypeDefinition getTypeDefinition() {
    return typeDefinition;
  }

  /**
   * Whether this accessor is specified as an xml list.
   *
   * @return Whether this accessor is specified as an xml list.
   */
  public boolean isXmlList() {
    return getAnnotation(XmlList.class) != null;
  }


}
