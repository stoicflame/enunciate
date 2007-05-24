/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.type.*;
import com.sun.mirror.util.Types;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.DeclarationDecorator;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.declaration.DecoratedClassDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedMemberDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.jaxb.adapters.Adaptable;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterType;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterUtil;
import org.codehaus.enunciate.contract.jaxb.types.KnownXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.validation.ValidationException;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * An accessor for a field or method value into a type.
 *
 * @author Ryan Heaton
 */
public abstract class Accessor extends DecoratedMemberDeclaration implements Adaptable {

  private final TypeDefinition typeDefinition;
  private final AdapterType adapterType;

  public Accessor(MemberDeclaration delegate, TypeDefinition typeDef) {
    super(delegate);

    if ((!(delegate instanceof FieldDeclaration)) && (!(delegate instanceof PropertyDeclaration))) {
      throw new IllegalArgumentException("Only a field or a property can be a JAXB accessor.");
    }

    this.typeDefinition = typeDef;
    this.adapterType = AdapterUtil.findAdapterType(this);
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
    TypeMirror accessorType;
    Declaration delegate = getDelegate();
    if (delegate instanceof FieldDeclaration) {
      accessorType = ((FieldDeclaration) delegate).getType();
    }
    else {
      accessorType = ((PropertyDeclaration) delegate).getPropertyType();
    }
    return accessorType;
  }

  /**
   * The bare (i.e. unwrapped) type of the accessor.
   *
   * @return The bare type of the accessor.
   */
  public TypeMirror getBareAccessorType() {
    return isCollectionType() ? getCollectionItemType() : getAccessorType();
  }

  /**
   * The base xml type of the accessor. The base type is either:
   * <p/>
   * <ol>
   * <li>The xml type of the accessor type.</li>
   * <li>The xml type of the component type of the accessor type if the accessor
   * type is a collection type.</li>
   * </ol>
   *
   * @return The base type.
   */
  public XmlType getBaseType() {
    //first check to see if the base type is dictated by a specific annotation.
    if (isXmlID()) {
      return KnownXmlType.ID;
    }

    if (isXmlIDREF()) {
      return KnownXmlType.IDREF;
    }

    if (isSwaRef()) {
      return KnownXmlType.SWAREF;
    }

    try {
      XmlType xmlType = XmlTypeFactory.findSpecifiedType(this);
      return (xmlType != null) ? xmlType : XmlTypeFactory.getXmlType(getAccessorType());
    }
    catch (XmlTypeException e) {
      throw new ValidationException(getPosition(), e.getMessage());
    }
  }

  /**
   * The qname for the referenced accessor, if this accessor is a reference to a global element, or null if
   * this element is not a reference element.
   *
   * @return The qname for the referenced element, if exists.
   */
  public QName getRef() {
    return null;
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
   * Whether this accessor is an XML ID.
   *
   * @return Whether this accessor is an XMLID.
   */
  public boolean isXmlID() {
    return getAnnotation(XmlID.class) != null;
  }

  /**
   * Whether this accessor is an XML IDREF.
   *
   * @return Whether this accessor is an XML IDREF.
   */
  public boolean isXmlIDREF() {
    return getAnnotation(XmlIDREF.class) != null;
  }

  /**
   * Whether this accessor consists of binary data.
   *
   * @return Whether this accessor consists of binary data.
   */
  public boolean isBinaryData() {
    return isSwaRef() || KnownXmlType.BASE64_BINARY.getQname().equals(getBaseType().getQname());
  }

  /**
   * Whether this accessor is a swa ref.
   *
   * @return Whether this accessor is a swa ref.
   */
  public boolean isSwaRef() {
    return (getAnnotation(XmlAttachmentRef.class) != null)
      && (getAccessorType() instanceof DeclaredType)
      && ("javax.activation.DataHandler".equals(((DeclaredType) getAccessorType()).getDeclaration().getQualifiedName()));
  }

  /**
   * Whether this accessor is an MTOM attachment.
   *
   * @return Whether this accessor is an MTOM attachment.
   */
  public boolean isMTOMAttachment() {
    return (getAnnotation(XmlInlineBinaryData.class) == null) && (KnownXmlType.BASE64_BINARY.getQname().equals(getBaseType().getQname()));
  }

  /**
   * The suggested mime type of the binary data, or null if none.
   *
   * @return The suggested mime type of the binary data, or null if none.
   */
  public String getMimeType() {
    XmlMimeType mimeType = getAnnotation(XmlMimeType.class);
    if (mimeType != null) {
      return mimeType.value();
    }

    return null;
  }

  /**
   * Whether the accessor type is a collection type.
   *
   * @return Whether the accessor type is a collection type.
   */
  public boolean isCollectionType() {
    if (isXmlList()) {
      return false;
    }

    DecoratedTypeMirror accessorType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(getAccessorType());
    if (accessorType.isArray()) {
      TypeMirror componentType = ((ArrayType) accessorType).getComponentType();
      //special case for byte[]
      return !(componentType instanceof PrimitiveType) || !(((PrimitiveType) componentType).getKind() == PrimitiveType.Kind.BYTE);
    }

    return accessorType.isCollection();
  }

  /**
   * If this is a collection type, return the type parameter of the collection, or null if this isn't a
   * parameterized collection type.
   *
   * @return the type parameter of the collection.
   */
  public TypeMirror getCollectionItemType() {
    DecoratedTypeMirror accessorType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(getAccessorType());
    if (accessorType.isArray()) {
      return ((ArrayType) accessorType).getComponentType();
    }
    else if (accessorType.isCollection()) {
      Iterator<TypeMirror> itemTypes = ((DeclaredType) accessorType).getActualTypeArguments().iterator();
      if (!itemTypes.hasNext()) {
        AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
        Types typeUtils = env.getTypeUtils();
        return TypeMirrorDecorator.decorate(typeUtils.getDeclaredType(env.getTypeDeclaration(java.lang.Object.class.getName())));
      }
      else {
        return itemTypes.next();
      }
    }

    return null;
  }

  /**
   * Returns the accessor for the XML id, or null if none was found or if this isn't an Xml IDREF accessor.
   *
   * @return The accessor, or null.
   */
  public MemberDeclaration getAccessorForXmlID() {
    if (isXmlIDREF()) {
      TypeMirror accessorType = getBareAccessorType();
      if (accessorType instanceof ClassType) {
        return getXmlIDAccessor((ClassType) accessorType);
      }
    }

    return null;
  }

  /**
   * Gets the xml id accessor for the specified class type (recursively through superclasses).
   *
   * @param classType The class type.
   * @return The xml id accessor.
   */
  private MemberDeclaration getXmlIDAccessor(ClassType classType) {
    ClassDeclaration declaration = classType.getDeclaration();
    if ((declaration == null) || (Object.class.getName().equals(declaration.getQualifiedName()))) {
      return null;
    }

    DecoratedClassDeclaration decoratedDeclaration = (DecoratedClassDeclaration) DeclarationDecorator.decorate(declaration);

    for (FieldDeclaration field : decoratedDeclaration.getFields()) {
      if (field.getAnnotation(XmlID.class) != null) {
        return field;
      }
    }

    for (PropertyDeclaration property : decoratedDeclaration.getProperties()) {
      if (property.getAnnotation(XmlID.class) != null) {
        return property;
      }
    }

    return getXmlIDAccessor(classType.getSuperclass());
  }

  // Inherited.
  public boolean isAdapted() {
    return this.adapterType != null;
  }

  // Inherited.
  public AdapterType getAdapterType() {
    return this.adapterType;
  }
}
