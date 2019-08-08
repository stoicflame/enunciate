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
package com.webcohesion.enunciate.modules.jackson.model;

import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.metadata.qname.XmlQNameEnumRef;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;
import com.webcohesion.enunciate.modules.jackson.model.adapters.Adaptable;
import com.webcohesion.enunciate.modules.jackson.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonTypeFactory;
import com.webcohesion.enunciate.modules.jackson.model.types.KnownJsonType;
import com.webcohesion.enunciate.modules.jackson.model.util.JacksonUtil;
import com.webcohesion.enunciate.modules.jackson.model.util.MapType;
import com.webcohesion.enunciate.util.AnnotationUtils;
import com.webcohesion.enunciate.util.HasClientConvertibleType;
import com.webcohesion.enunciate.util.OptionalUtils;

import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

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
  final EnunciateJacksonContext context;

  public Accessor(javax.lang.model.element.Element delegate, TypeDefinition typeDef, EnunciateJacksonContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.typeDefinition = typeDef;
    this.facets.addAll(Facet.gatherFacets(delegate, context.getContext()));
    this.facets.addAll(typeDef.getFacets());
    this.context = context;
    this.adapterType = JacksonUtil.findAdapterType(this, context);
  }

  /**
   * The name of the accessor.
   *
   * @return The name of the accessor.
   */
  public abstract String getName();

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

    accessorType = this.context.resolveSyntheticType(accessorType);

    DecoratedDeclaredType normalizedCollection = JacksonUtil.getNormalizedCollection(accessorType, this.context.getContext().getProcessingEnvironment());
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
    return isCollectionType() ? getCollectionItemType() : getAccessorType();
  }

  /**
   * The base json type of the accessor. The base type is either:
   * <p/>
   * <ol>
   * <li>The json type of the accessor type.</li>
   * <li>The json type of the component type of the accessor type if the accessor type is a collection type.</li>
   * </ol>
   *
   * @return The base type.
   */
  public JsonType getJsonType() {
    JsonType jsonType = JsonTypeFactory.findSpecifiedType(this, this.context);
    if (jsonType != null) {
      return jsonType;
    }

    if (AnnotationUtils.isPassword(this)) {
      return KnownJsonType.PASSWORD;
    }

    return JsonTypeFactory.getJsonType(getAccessorType(), this.context);
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
   * Whether the accessor type is a collection type.
   *
   * @return Whether the accessor type is a collection type.
   */
  public boolean isCollectionType() {
    DecoratedTypeMirror accessorType = getAccessorType();
    if (isAdapted()) {
      accessorType = (DecoratedTypeMirror) getAdapterType().getAdaptingType(accessorType, this.context.getContext());
    }
    return accessorType.isArray() || accessorType.isCollection();
  }

  /**
   * If this is a collection type, return the type parameter of the collection, or null if this isn't a
   * parameterized collection type.
   *
   * @return the type parameter of the collection.
   */
  public DecoratedTypeMirror getCollectionItemType() {
    return TypeMirrorUtils.getComponentType(getAccessorType(), this.context.getContext().getProcessingEnvironment());
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
   * Whether this accessor is a value.
   *
   * @return Whether this accessor is a value.
   */
  public boolean isValue() {
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
    XmlQNameEnumRef enumRef = getAnnotation(XmlQNameEnumRef.class);
    DecoratedTypeMirror qnameEnumType = null;
    if (enumRef != null) {
      try {
        qnameEnumType = TypeMirrorUtils.mirrorOf(enumRef.value(), this.env);
      }
      catch (MirroredTypeException e) {
        qnameEnumType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(e.getTypeMirror(), this.env);
      }
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

  public EnunciateJacksonContext getContext() {
    return context;
  }

  /**
   * The facets here applicable.
   *
   * @return The facets here applicable.
   */
  public Set<Facet> getFacets() {
    return facets;
  }

}
