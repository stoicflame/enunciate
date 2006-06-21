package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.type.TypeMirror;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.enunciate.contract.jaxb.types.SpecifiedXmlType;
import net.sf.enunciate.contract.jaxb.types.XmlTypeException;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.declaration.DecoratedMemberDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import javax.xml.bind.annotation.*;

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
  public TypeMirror getAccessorType() {
    Declaration delegate = getDelegate();
    if (delegate instanceof FieldDeclaration) {
      return ((FieldDeclaration) delegate).getType();
    }
    else {
      return ((PropertyDeclaration) delegate).getPropertyType();
    }
  }

  /**
   * The base xml type of the accessor. The base type is either:
   * <p/>
   * <ol>
   * <li>The accessor type.</li>
   * <li>The component type of the accessor type if the accessor type is a collection type.</li>
   * </ol>
   *
   * @return The base type.
   */
  public XmlTypeMirror getBaseType() {
    //first check to see if the base type is dictated by a specific annotation.
    XmlSchemaType schemaType = getAnnotation(XmlSchemaType.class);
    if (schemaType != null) {
      return new SpecifiedXmlType(schemaType);
    }

    XmlID xmlID = getAnnotation(XmlID.class);
    if (xmlID != null) {
      return KnownXmlType.ID;
    }

    XmlIDREF xmlIDREF = getAnnotation(XmlIDREF.class);
    if (xmlIDREF != null) {
      return KnownXmlType.IDREF;
    }

    XmlAttachmentRef attachmentRef = getAnnotation(XmlAttachmentRef.class);
    if (attachmentRef != null) {
      return KnownXmlType.SWAREF;
    }

    TypeMirror baseType = getAccessorType();
    try {
      return ((EnunciateFreemarkerModel) FreemarkerModel.get()).getXmlType(baseType);
    }
    catch (XmlTypeException e) {
      throw new ValidationException(getPosition(), e.getMessage());
    }
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


  /**
   * Whether the accessor type is a collection type.
   *
   * @return Whether the accessor type is a collection type.
   */
  public boolean isCollectionType() {
    DecoratedTypeMirror accessorType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(getAccessorType());
    return accessorType.isArray() || accessorType.isCollection();
  }
}
