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

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.element.PropertyElement;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.metadata.qname.XmlQNameEnumRef;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.util.AccessorBag;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import jakarta.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.beans.Introspector;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * A xml type definition.
 *
 * @author Ryan Heaton
 */
public abstract class TypeDefinition extends DecoratedTypeElement implements HasFacets {

  private final jakarta.xml.bind.annotation.XmlType xmlType;
  private final Schema schema;
  private final SortedSet<Element> elements;
  private final Collection<Attribute> attributes;
  private final Value xmlValue;
  private final Accessor xmlID;
  private final boolean hasAnyAttribute;
  private final TypeMirror anyAttributeQNameEnumRef;
  private final AnyElement anyElement;
  private final LinkedList<javax.lang.model.element.Element> referencedFrom = new LinkedList<javax.lang.model.element.Element>();
  private final Set<Facet> facets = new TreeSet<Facet>();
  protected final EnunciateJaxbContext context;

  protected TypeDefinition(TypeElement delegate, EnunciateJaxbContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());

    this.xmlType = getAnnotation(jakarta.xml.bind.annotation.XmlType.class);
    this.schema = new Schema(context.getContext().getProcessingEnvironment().getElementUtils().getPackageOf(delegate), env);

    ElementComparator comparator = new ElementComparator(getPropertyOrder(), getAccessorOrder(), env);
    SortedSet<Element> elementAccessors = new TreeSet<Element>(comparator);
    AccessorFilter filter = new AccessorFilter(getAccessType());
    Collection<Attribute> attributeAccessors = new ArrayList<Attribute>();
    Value value = null;

    Accessor xmlID = null;
    AnyElement anyElement = null;
    boolean hasAnyAttribute = false;
    TypeMirror anyAttributeQNameEnumRef = null;
    for (javax.lang.model.element.Element accessor : loadPotentialAccessors(filter)) {
      Accessor added;
      if (isAttribute(accessor)) {
        Attribute attribute = new Attribute(accessor, this, context);
        attributeAccessors.add(attribute);
        added = attribute;
      }
      else if (isValue(accessor)) {
        if (value != null) {
          throw new EnunciateException("Accessor " + accessor.getSimpleName() + " of " + getQualifiedName() + ": a type definition cannot have more than one xml value.");
        }

        value = new Value(accessor, this, context);
        added = value;
      }
      else if (isElementRef(accessor)) {
        ElementRef elementRef = new ElementRef(accessor, this, context);
        if (!elementAccessors.add(elementRef)) {
          //see http://jira.codehaus.org/browse/ENUNCIATE-381; the case for this is when an annotated field has an associated public property
          //we'll just silently continue
          continue;
        }
        added = elementRef;
      }
      else if (isAnyAttribute(accessor)) {
        hasAnyAttribute = true;

        final XmlQNameEnumRef enumRef = accessor.getAnnotation(XmlQNameEnumRef.class);
        if (enumRef != null) {
          anyAttributeQNameEnumRef = Annotations.mirrorOf(enumRef::value, this.env);
        }

        continue;
      }
      else if (isAnyElement(accessor)) {
        anyElement = new AnyElement(accessor, this, context);
        continue;
      }
      else if (isUnsupported(accessor)) {
        throw new EnunciateException("Accessor " + accessor.getSimpleName() + " of " + getQualifiedName() + ": sorry, we currently don't support mixed or wildard elements. Maybe someday...");
      }
      else {
        //its an element accessor.

        if (accessor instanceof PropertyElement) {
          //if the accessor is a property and either the getter or setter overrides ANY method of ANY superclass, exclude it.
          if (overridesAnother(((PropertyElement) accessor).getGetter()) || overridesAnother(((PropertyElement) accessor).getSetter())) {
            continue;
          }
        }

        Element element = new Element(accessor, this, context);
        if (!elementAccessors.add(element)) {
          //see http://jira.codehaus.org/browse/ENUNCIATE-381; the case for this is when an annotated field has an associated public property
          //we'll just silently continue
          continue;
        }
        added = element;
      }

      if (added.getAnnotation(XmlID.class) != null) {
        if (xmlID != null) {
          throw new EnunciateException("Accessor " + added.getSimpleName() + " of " + getQualifiedName() + ": more than one XML id specified.");
        }

        xmlID = added;
      }
    }

    this.elements = Collections.unmodifiableSortedSet(elementAccessors);
    this.attributes = Collections.unmodifiableCollection(attributeAccessors);
    this.xmlValue = value;
    this.xmlID = xmlID;
    this.hasAnyAttribute = hasAnyAttribute;
    this.anyAttributeQNameEnumRef = anyAttributeQNameEnumRef;
    this.anyElement = anyElement;
    this.facets.addAll(Facet.gatherFacets(delegate, context.getContext()));
    this.facets.addAll(this.schema.getFacets());
    this.context = context;
  }

  protected TypeDefinition(TypeDefinition copy) {
    super(copy.delegate, copy.env);
    this.xmlType = copy.xmlType;
    this.schema = copy.schema;
    this.elements = copy.elements;
    this.attributes = copy.attributes;
    this.xmlValue = copy.xmlValue;
    this.xmlID = copy.xmlID;
    this.hasAnyAttribute = copy.hasAnyAttribute;
    this.anyAttributeQNameEnumRef = copy.anyAttributeQNameEnumRef;
    this.anyElement = copy.anyElement;
    this.facets.addAll(copy.facets);
    this.context = copy.context;
  }

  /**
   * Load the potential accessors for this type definition.
   *
   * @param filter The filter.
   * @return the potential accessors for this type definition.
   */
  protected List<javax.lang.model.element.Element> loadPotentialAccessors(AccessorFilter filter) {
    AccessorBag bag = new AccessorBag();
    aggregatePotentialAccessors(bag, this, filter, false);
    return bag.getAccessors();
  }

  /**
   * Aggregate the potential accessor into their separate buckets for the given class declaration, recursively including transient superclasses.
   *
   * @param bag        The collected fields and properties.
   * @param clazz      The class.
   * @param filter     The filter.
   */
  protected void aggregatePotentialAccessors(AccessorBag bag, DecoratedTypeElement clazz, AccessorFilter filter, boolean inlineAccessorsOfSuperclasses) {
    DecoratedTypeElement superDeclaration = clazz.getSuperclass() != null ? (DecoratedTypeElement) this.env.getTypeUtils().asElement(clazz.getSuperclass()) : null;
    if (superDeclaration != null && (isXmlTransient(superDeclaration) || inlineAccessorsOfSuperclasses)) {
      aggregatePotentialAccessors(bag, superDeclaration, filter, true);
    }

    for (VariableElement fieldDeclaration : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
      if (!filter.accept((DecoratedElement) fieldDeclaration)) {
        bag.fields.removeByName(fieldDeclaration);
      }
      else {
        bag.fields.addOrReplace(fieldDeclaration);
      }
    }

    for (PropertyElement propertyDeclaration : clazz.getProperties()) {
      if (!filter.accept(propertyDeclaration)) {
        bag.properties.remove(propertyDeclaration);
      }
      else {
        bag.properties.addOrReplace(propertyDeclaration);
      }
    }
  }

  /**
   * Whether the given method declaration overrides any method.
   *
   * @param method The method declaration.
   * @return Whether the given method declaration overrides any method.
   */
  protected boolean overridesAnother(DecoratedExecutableElement method) {
    if (method == null) {
      return false;
    }

    final TypeElement declaringType = (TypeElement) method.getEnclosingElement();
    TypeElement superType = (TypeElement) this.env.getTypeUtils().asElement(declaringType.getSuperclass());
    if (superType != null && superType.getAnnotation(XmlTransient.class) == null) { //ignore transient supertypes.
      while (superType != null && !Object.class.getName().equals(superType.getQualifiedName().toString())) {
        List<ExecutableElement> methods = ElementFilter.methodsIn(superType.getEnclosedElements());
        for (ExecutableElement candidate : methods) {
          if (this.env.getElementUtils().overrides(method, candidate, declaringType)) {
            return true;
          }
        }

        superType = (TypeElement) this.env.getTypeUtils().asElement(superType.getSuperclass());
      }
    }

    return false;
  }

  /**
   * Whether a declaration is an xml attribute.
   *
   * @param declaration The declaration to check.
   * @return Whether a declaration is an attribute.
   */
  protected boolean isAttribute(javax.lang.model.element.Element declaration) {
    //todo: the attribute wildcard?
    return (declaration.getAnnotation(XmlAttribute.class) != null);
  }

  /**
   * Whether a declaration is an xml value.
   *
   * @param declaration The declaration to check.
   * @return Whether a declaration is an value.
   */
  protected boolean isValue(javax.lang.model.element.Element declaration) {
    return (declaration.getAnnotation(XmlValue.class) != null);
  }

  /**
   * Whether a declaration is an xml element ref.
   *
   * @param declaration The declaration to check.
   * @return Whether a declaration is an xml element ref.
   */
  protected boolean isElementRef(javax.lang.model.element.Element declaration) {
    return ((declaration.getAnnotation(XmlElementRef.class) != null) || (declaration.getAnnotation(XmlElementRefs.class) != null));
  }

  /**
   * Whether the member declaration is XmlAnyAttribute.
   *
   * @param declaration The declaration.
   * @return Whether the member declaration is XmlAnyAttribute.
   */
  protected boolean isAnyAttribute(javax.lang.model.element.Element declaration) {
    return declaration.getAnnotation(XmlAnyAttribute.class) != null;
  }

  /**
   * Whether the member declaration is XmlAnyElement.
   *
   * @param declaration The declaration.
   * @return Whether the member declaration is XmlAnyElement.
   */
  protected boolean isAnyElement(javax.lang.model.element.Element declaration) {
    return declaration.getAnnotation(XmlAnyElement.class) != null;
  }

  /**
   * Whether a declaration is an xml-mixed property.
   *
   * @param declaration The declaration to check.
   * @return Whether a declaration is an mixed.
   */
  protected boolean isUnsupported(javax.lang.model.element.Element declaration) {
    //todo: support xml-mixed?
    return (declaration.getAnnotation(XmlMixed.class) != null);
  }

  /**
   * The name of the xml type element.
   *
   * @return The name of the xml type element.
   */
  public String getName() {
    String name = Introspector.decapitalize(getSimpleName().toString());

    if ((xmlType != null) && (!"##default".equals(xmlType.name()))) {
      name = xmlType.name();

      if ("".equals(name)) {
        name = null;
      }
    }

    return name;
  }

  /**
   * The namespace of the xml type element.
   *
   * @return The namespace of the xml type element.
   */
  public String getNamespace() {
    String namespace = getPackage().getNamespace();

    if ((xmlType != null) && (!"##default".equals(xmlType.namespace()))) {
      namespace = xmlType.namespace();
    }

    return namespace;
  }

  public EnunciateJaxbContext getContext() {
    return context;
  }

  /**
   * The simple name for client-side code generation.
   *
   * @return The simple name for client-side code generation.
   */
  public String getClientSimpleName() {
    String clientSimpleName = getSimpleName().toString();
    ClientName clientName = getAnnotation(ClientName.class);
    if (clientName != null) {
      clientSimpleName = clientName.value();
    }
    return clientSimpleName;
  }

  /**
   * The qname of this type definition.
   *
   * @return The qname of this type definition.
   */
  public QName getQname() {
    String localPart = getName();
    if (localPart == null) {
      localPart = "";
    }
    return new QName(getNamespace(), localPart);
  }

  /**
   * The default access type for the beans in this class.
   *
   * @return The default access type for the beans in this class.
   */
  public XmlAccessType getAccessType() {
    XmlAccessType accessType = getPackage().getAccessType();

    XmlAccessorType xmlAccessorType = getAnnotation(XmlAccessorType.class);
    if (xmlAccessorType != null) {
      accessType = xmlAccessorType.value();
    }
    else {
      XmlAccessType inheritedAccessType = getInheritedAccessType(this);
      if (inheritedAccessType != null) {
        accessType = inheritedAccessType;
      }
    }

    return accessType;
  }

  /**
   * Get the inherited accessor type of the given class, or null if none is found.
   *
   * @param declaration The inherited accessor type.
   * @return The inherited accessor type of the given class, or null if none is found.
   */
  protected XmlAccessType getInheritedAccessType(TypeElement declaration) {
    TypeMirror superclass = declaration.getSuperclass();
    if (superclass != null && superclass.getKind() != TypeKind.NONE) {
      TypeElement superDeclaration = (TypeElement) this.env.getTypeUtils().asElement(superclass);
      if ((superDeclaration != null) && (!Object.class.getName().equals(superDeclaration.getQualifiedName().toString()))) {
        XmlAccessorType xmlAccessorType = superDeclaration.getAnnotation(XmlAccessorType.class);
        if (xmlAccessorType != null) {
          return xmlAccessorType.value();
        }
        else {
          return getInheritedAccessType(superDeclaration);
        }
      }
    }

    return null;
  }

  /**
   * The property order of this xml type.
   *
   * @return The property order of this xml type.
   */
  public String[] getPropertyOrder() {
    String[] propertyOrder = null;

    if (xmlType != null) {
      String[] propOrder = xmlType.propOrder();
      if ((propOrder != null) && (propOrder.length > 0) && ((propOrder.length > 1) || !("".equals(propOrder[0])))) {
        propertyOrder = propOrder;
      }
    }

    return propertyOrder;
  }

  /**
   * The default accessor order of the beans in this package.
   *
   * @return The default accessor order of the beans in this package.
   */
  public XmlAccessOrder getAccessorOrder() {
    XmlAccessOrder order = getPackage().getAccessorOrder();

    XmlAccessorOrder xmlAccessorOrder = getAnnotation(XmlAccessorOrder.class);
    if (xmlAccessorOrder != null) {
      order = xmlAccessorOrder.value();
    }

    return order;
  }

  /**
   * @return The list of class names that this type definition wants you to "see also".
   */
  public Collection<TypeMirror> getSeeAlsos() {
    Collection<TypeMirror> seeAlsos = null;
    XmlSeeAlso seeAlsoInfo = getAnnotation(XmlSeeAlso.class);
    if (seeAlsoInfo != null) {
      seeAlsos = new ArrayList<TypeMirror>();
      try {
        for (Class clazz : seeAlsoInfo.value()) {
          TypeElement typeDeclaration = this.env.getElementUtils().getTypeElement(clazz.getName());
          seeAlsos.add(typeDeclaration.asType());
        }
      }
      catch (MirroredTypesException e) {
        seeAlsos.addAll(TypeMirrorDecorator.decorate(e.getTypeMirrors(), this.env));
      }
    }
    return seeAlsos;
  }

  /**
   * Whether this type definition has an "anyAttribute" definition.
   *
   * @return Whether this type definition has an "anyAttribute" definition.
   */
  public boolean isHasAnyAttribute() {
    return hasAnyAttribute;
  }

  /**
   * The enum type containing the known qnames for attributes of the 'any' attribute definition. <code>null</code> if none.
   *
   * @return The enum type containing the known qnames for attributes of the 'any' attribute definition. <code>null</code> if none.
   */
  public TypeMirror getAnyAttributeQNameEnumRef() {
    return anyAttributeQNameEnumRef;
  }

  /**
   * The "anyElement" element.
   *
   * @return The "anyElement" element.
   */
  public AnyElement getAnyElement() {
    return anyElement;
  }

  /**
   * The elements of this type definition.
   *
   * @return The elements of this type definition.
   */
  public SortedSet<Element> getElements() {
    return elements;
  }

  /**
   * The attributes of this type definition.
   *
   * @return The attributes of this type definition.
   */
  public Collection<Attribute> getAttributes() {
    return attributes;
  }

  /**
   * The value of this type definition.
   *
   * @return The value of this type definition.
   */
  public Value getValue() {
    return xmlValue;
  }

  public List<Accessor> getAllAccessors() {
    ArrayList<Accessor> accessors = new ArrayList<Accessor>();
    accessors.addAll(getAllAttributes());
    accessors.addAll(getAllValues());
    accessors.addAll(getAllElements());
    return accessors;
  }

  public List<Attribute> getAllAttributes() {
    ArrayList<Attribute> attributes = new ArrayList<Attribute>();

    com.webcohesion.enunciate.modules.jaxb.model.types.XmlType baseType = getBaseType();
    if (baseType instanceof XmlClassType) {
      attributes.addAll(((XmlClassType) baseType).getTypeDefinition().getAllAttributes());
    }

    MY_ATTRIBUTES : for (Attribute attribute : getAttributes()) {
      for (Attribute other : attributes) {
        if (attribute.overrides(other)) {
          continue MY_ATTRIBUTES;
        }
      }

      attributes.add(attribute);
    }

    return attributes;
  }

  public List<Value> getAllValues() {
    ArrayList<Value> values = new ArrayList<Value>();
    com.webcohesion.enunciate.modules.jaxb.model.types.XmlType baseType = getBaseType();
    if (baseType instanceof XmlClassType) {
      values.addAll(((XmlClassType) baseType).getTypeDefinition().getAllValues());
    }

    Value value = getValue();
    if (value != null && values.isEmpty()) {
      values.add(value);
    }

    return values;
  }

  public List<Element> getAllElements() {
    ArrayList<Element> elements = new ArrayList<Element>();
    com.webcohesion.enunciate.modules.jaxb.model.types.XmlType baseType = getBaseType();
    if (baseType instanceof XmlClassType) {
      elements.addAll(((XmlClassType) baseType).getTypeDefinition().getAllElements());
    }

    MY_ELEMENTS : for (Element element : getElements()) {
      for (Element other : elements) {
        if (element.overrides(other)) {
          continue MY_ELEMENTS;
        }
      }

      elements.add(element);
    }

    return elements;
  }

  /**
   * The accessor that is the xml id of this type definition, or null if none.
   *
   * @return The accessor that is the xml id of this type definition, or null if none.
   */
  public Accessor getXmlID() {
    return xmlID;
  }

  /**
   * Whether a declaration is xml transient.
   *
   * @param declaration The declaration on which to determine xml transience.
   * @return Whether a declaration is xml transient.
   */
  protected boolean isXmlTransient(javax.lang.model.element.Element declaration) {
    return (declaration.getAnnotation(XmlTransient.class) != null);
  }

  /**
   * Whether this xml type is anonymous.
   *
   * @return Whether this xml type is anonymous.
   */
  public boolean isAnonymous() {
    return getName() == null;
  }

  /**
   * The schema for this complex type.
   *
   * @return The schema for this complex type.
   */
  public Schema getSchema() {
    return schema;
  }

  // Inherited.
  @Override
  public Schema getPackage() {
    return getSchema();
  }

  /**
   * Whether this is a complex type.
   *
   * @return Whether this is a complex type.
   */
  public boolean isComplex() {
    return false;
  }

  /**
   * Whether this is a enum type.
   *
   * @return Whether this is a enum type.
   */
  public boolean isEnum() {
    return false;
  }

  /**
   * Whether this is a simple type.
   *
   * @return Whether this is a simple type.
   */
  public boolean isSimple() {
    return false;
  }

  /**
   * Whether this type definition is a base object (i.e. a root of the object hierarchy).
   *
   * @return Whether this type definition is a base object
   */
  public boolean isBaseObject() {
    return true;
  }

  /**
   * Set of (human-readable) locations that this type definition is referenced from.
   *
   * @return The referenced-from list.
   */
  public LinkedList<javax.lang.model.element.Element> getReferencedFrom() {
    return referencedFrom;
  }

  /**
   * The facets here applicable.
   *
   * @return The facets here applicable.
   */
  public Set<Facet> getFacets() {
    return facets;
  }

  /**
   * The base type of this type definition.
   *
   * @return The base type of this type definition.
   */
  public abstract com.webcohesion.enunciate.modules.jaxb.model.types.XmlType getBaseType();

}
