/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jaxb.model;

import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.decorations.element.PropertyElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.metadata.qname.XmlQNameEnumRef;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.Adaptable;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jaxb.model.types.KnownXmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlTypeFactory;
import com.webcohesion.enunciate.modules.jaxb.model.util.JAXBUtil;
import com.webcohesion.enunciate.modules.jaxb.model.util.MapType;
import com.webcohesion.enunciate.util.HasClientConvertibleType;
import com.webcohesion.enunciate.util.OptionalUtils;

import jakarta.activation.DataHandler;
import javax.lang.model.element.Element;
import javax.lang.model.type.*;
import jakarta.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * An accessor for a field or method value into a type.
 *
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public abstract class Accessor extends DecoratedElement<javax.lang.model.element.Element> implements Adaptable, HasFacets, HasClientConvertibleType {

  final TypeDefinition typeDefinition;
  final AdapterType adapterType;
  final Set<Facet> facets = new TreeSet<Facet>();
  final EnunciateJaxbContext context;

  public Accessor(javax.lang.model.element.Element delegate, TypeDefinition typeDef, EnunciateJaxbContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.typeDefinition = typeDef;
    this.facets.addAll(Facet.gatherFacets(delegate, context.getContext()));
    this.facets.addAll(typeDef.getFacets());
    this.context = context;
    this.adapterType = JAXBUtil.findAdapterType(this, context);
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
   * The JAXB context.
   *
   * @return The Enunciate JAXB context.
   */
  public EnunciateJaxbContext getContext() {
    return context;
  }

  /**
   * The simple name for client-side code generation.
   *
   * @return The simple name for client-side code generation.
   */
  public String getClientSimpleName() {
    String clientSimpleName = this.delegate.getSimpleName().toString();
    ClientName clientName = this.delegate.getAnnotation(ClientName.class);
    if (clientName != null) {
      clientSimpleName = clientName.value();
    }
    return clientSimpleName;
  }

  /**
   * The type of the accessor.
   *
   * @return The type of the accessor.
   */
  public DecoratedTypeMirror getAccessorType() {
    DecoratedTypeMirror accessorType = (DecoratedTypeMirror) asType();

    accessorType = OptionalUtils.stripOptional(accessorType, this.context.getContext().getProcessingEnvironment());

    DecoratedDeclaredType normalizedCollection = JAXBUtil.getNormalizedCollection(accessorType, this.context.getContext().getProcessingEnvironment());
    if (normalizedCollection != null) {
      accessorType = normalizedCollection;
    }
    else {
      MapType mapType = MapType.findMapType(accessorType, this.context);
      if (mapType != null) {
        accessorType = mapType;
      }
    }

    return accessorType;
  }

  @Override
  public TypeMirror getClientConvertibleType() {
    return getAccessorType();
  }

  /**
   * The bare (i.e. unwrapped) type of the accessor.
   *
   * @return The bare type of the accessor.
   */
  public DecoratedTypeMirror getBareAccessorType() {
    if (isCollectionType()) {
      return getCollectionItemType();
    }

    if (isXmlList()) {
      DecoratedTypeMirror componentType = getCollectionItemType();
      return componentType != null ? componentType : getAccessorType();
    }

    return getAccessorType();
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

    XmlType xmlType = XmlTypeFactory.findSpecifiedType(this, this.context);
    return (xmlType != null) ? xmlType : XmlTypeFactory.getXmlType(getAccessorType(), this.context);
  }

  /**
   * The XML type of the property. This can be different from the "base" type, which doesn't apply to element refs.
   *
   * @return The XML type of the property.
   */
  public XmlType getXmlType() {
    return getBaseType();
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
    return this.delegate.getAnnotation(XmlList.class) != null;
  }

  /**
   * Whether this accessor is an XML ID.
   *
   * @return Whether this accessor is an XMLID.
   */
  public boolean isXmlID() {
    return this.delegate.getAnnotation(XmlID.class) != null;
  }

  /**
   * Whether this accessor is an XML IDREF.
   *
   * @return Whether this accessor is an XML IDREF.
   */
  public boolean isXmlIDREF() {
    return this.delegate.getAnnotation(XmlIDREF.class) != null;
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
   * Whether this access is a QName type.
   *
   * @return Whether this access is a QName type.
   */
  public boolean isQNameType() {
    return getBaseType() == KnownXmlType.QNAME;
  }

  /**
   * Get the resolved accessor type for this accessor.
   *
   * @return the resolved accessor type for this accessor.
   */
  public DecoratedTypeMirror getResolvedAccessorType() {
    DecoratedTypeMirror accessorType = getAccessorType();

    if (isAdapted()) {
      accessorType = (DecoratedTypeMirror) getAdapterType().getAdaptingType(accessorType, this.context.getContext());
    }

    return accessorType;
  }

  /**
   * Whether this accessor is a swa ref.
   *
   * @return Whether this accessor is a swa ref.
   */
  public boolean isSwaRef() {
    DecoratedTypeMirror<?> accessorType = (DecoratedTypeMirror) getAccessorType();
    return (getAnnotation(XmlAttachmentRef.class) != null) && (accessorType.isInstanceOf(DataHandler.class));
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

    DecoratedTypeMirror accessorType = getAccessorType();
    if (isAdapted()) {
      accessorType = (DecoratedTypeMirror) getAdapterType().getAdaptingType(accessorType, this.context.getContext());
    }

    if (accessorType.isArray()) {
      //special case for byte[]
      return ((ArrayType) accessorType).getComponentType().getKind() != TypeKind.BYTE;
    }

    return accessorType.isCollection();
  }

  /**
   * If this is a collection type, return the type parameter of the collection, or null if this isn't a
   * parameterized collection type.
   *
   * @return the type parameter of the collection.
   */
  public DecoratedTypeMirror getCollectionItemType() {
    if (isAdapted()) {
      DecoratedTypeMirror adaptingType = (DecoratedTypeMirror) getAdapterType().getAdaptingType();
      if (adaptingType.isCollection()) {
        return TypeMirrorUtils.getComponentType(adaptingType, this.context.getContext().getProcessingEnvironment());
      }
      else {
        return adaptingType;
      }
    }
    else {
      return TypeMirrorUtils.getComponentType(getAccessorType(), this.context.getContext().getProcessingEnvironment());
    }
  }

  /**
   * Returns the accessor for the XML id, or null if none was found or if this isn't an Xml IDREF accessor.
   *
   * @return The accessor, or null.
   */
  public DecoratedElement getAccessorForXmlID() {
    if (isXmlIDREF()) {
      DecoratedTypeMirror accessorType = getBareAccessorType();
      if (accessorType.isDeclared()) {
        return getXmlIDAccessor((DecoratedDeclaredType) accessorType);
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
  private DecoratedElement getXmlIDAccessor(DecoratedDeclaredType classType) {
    if (classType == null) {
      return null;
    }

    DecoratedTypeElement declaration = (DecoratedTypeElement) classType.asElement();
    if ((declaration == null) || (Object.class.getName().equals(declaration.getQualifiedName().toString()))) {
      return null;
    }

    for (Element field : ElementUtils.fieldsOrRecordComponentsIn(declaration)) {
      if (field.getAnnotation(XmlID.class) != null) {
        return (DecoratedElement) field;
      }
    }

    for (PropertyElement property : declaration.getProperties()) {
      if (property.getAnnotation(XmlID.class) != null) {
        return property;
      }
    }

    TypeMirror superclass = declaration.getSuperclass();
    if (superclass == null || superclass.getKind() == TypeKind.NONE) {
      return null;
    }

    return getXmlIDAccessor((DecoratedDeclaredType) superclass);
  }

  /**
   * @return The list of class names that this type definition wants you to "see also".
   */
  public Collection<DecoratedTypeMirror> getSeeAlsos() {
    Collection<DecoratedTypeMirror> seeAlsos = null;
    XmlSeeAlso seeAlsoInfo = getAnnotation(XmlSeeAlso.class);
    if (seeAlsoInfo != null) {
      seeAlsos = new ArrayList<DecoratedTypeMirror>();
      try {
        for (Class clazz : seeAlsoInfo.value()) {
          seeAlsos.add(TypeMirrorUtils.mirrorOf(clazz, this.env));
        }
      }
      catch (MirroredTypesException e) {
        seeAlsos.addAll((Collection<? extends DecoratedTypeMirror>) TypeMirrorDecorator.decorate(e.getTypeMirrors(), this.env));
      }
    }
    return seeAlsos;
  }

  // Inherited.
  public boolean isAdapted() {
    return this.adapterType != null;
  }

  // Inherited.
  public AdapterType getAdapterType() {
    return this.adapterType;
  }

  /**
   * Whether this accessor is an attribute.
   *
   * @return Whether this accessor is an attribute.
   */
  public boolean isAttribute() {
    return false;
  }

  /**
   * Whether this accessor is a value.
   *
   * @return Whether this accessor is a value.
   */
  public boolean isValue() {
    return false;
  }

  /**
   * Whether this accessor is an element ref.
   *
   * @return Whether this accessor is an element ref.
   */
  public boolean isElementRef() {
    return false;
  }

  /**
   * Whether this QName accessor references a QName enum type.
   *
   * @return Whether this QName accessor references a QName enum type.
   */
  public boolean isReferencesQNameEnum() {
    return getAnnotation(XmlQNameEnumRef.class) != null;
  }

  /**
   * The enum type containing the known qnames for this qname enum accessor, or null is this accessor doesn't reference a known qname type.
   *
   * @return The enum type containing the known qnames for this qname enum accessor.
   */
  public DecoratedTypeMirror getQNameEnumRef() {
    final XmlQNameEnumRef enumRef = getAnnotation(XmlQNameEnumRef.class);
    DecoratedTypeMirror qnameEnumType = null;
    if (enumRef != null) {
      qnameEnumType = Annotations.mirrorOf(enumRef::value, this.env);
    }
    return qnameEnumType;
  }

  /**
   * Set of (human-readable) locations that this type definition is referenced from.
   *
   * @return The referenced-from list.
   */
  public LinkedList<Element> getReferencedFrom() {
    LinkedList<Element> stack = new LinkedList<Element>(this.typeDefinition.getReferencedFrom());
    stack.add(this);
    return stack;
  }

  /**
   * The facets here applicable.
   *
   * @return The facets here applicable.
   */
  public Set<Facet> getFacets() {
    return facets;
  }

  public boolean overrides(Accessor accessor) {
    return accessor != null && accessor != this && accessor.getAnnotation(XmlTransient.class) == null && getSimpleName().toString().equals(accessor.getSimpleName().toString());
  }
}
