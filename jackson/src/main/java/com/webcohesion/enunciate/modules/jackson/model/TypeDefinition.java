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

import com.fasterxml.jackson.annotation.*;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.element.*;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.*;

/**
 * A json type definition.
 *
 * @author Ryan Heaton
 */
public abstract class TypeDefinition extends DecoratedTypeElement implements HasFacets {

  private final SortedSet<Member> members;
  private final Value value;
  private final WildcardMember wildcardMember;
  private final LinkedList<javax.lang.model.element.Element> referencedFrom = new LinkedList<javax.lang.model.element.Element>();
  private final Set<Facet> facets = new TreeSet<Facet>();
  protected final EnunciateJacksonContext context;
  private final String[] propOrder;

  protected TypeDefinition(TypeElement delegate, EnunciateJacksonContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.context = context;

    String[] propOrder = null;
    boolean alphabetical = false;

    if (context.isHonorJaxb()) {
      XmlType typeInfo = getAnnotation(XmlType.class);
      if (typeInfo != null) {
        propOrder = typeInfo.propOrder();
        if (propOrder.length == 1 && "".equals(propOrder[0])) {
          propOrder = null;
        }
      }
    }

    JsonPropertyOrder propOrderInfo = getAnnotation(JsonPropertyOrder.class);
    if (propOrderInfo != null) {
      propOrder = propOrderInfo.value();
      alphabetical = propOrderInfo.alphabetic();
    }

    MemberComparator comparator = new MemberComparator(propOrder, alphabetical, env);
    SortedSet<Member> memberAccessors = new TreeSet<Member>(comparator);
    Value value = null;
    WildcardMember wildcardMember = null;
    JsonIgnoreType ignoreType = getAnnotation(JsonIgnoreType.class);
    if (ignoreType == null || !ignoreType.value()) {
      AccessorFilter filter = new AccessorFilter(context, getAnnotation(JsonAutoDetect.class), getAnnotation(JsonIgnoreProperties.class), getAnnotation(XmlAccessorType.class));
      value = null;

      wildcardMember = null;
      for (javax.lang.model.element.Element accessor : loadPotentialAccessors(filter)) {
        if (isValue(accessor)) {
          if (value != null) {
            throw new EnunciateException("Accessor " + accessor.getSimpleName() + " of " + getQualifiedName() + ": a type definition cannot have more than one json value.");
          }

          value = new Value(accessor, this, context);
        }
        else if (isWildcardProperty(accessor)) {
          wildcardMember = new WildcardMember(accessor, this, context);
        }
        else {
          //its an property accessor.

          if (accessor instanceof PropertyElement) {
            //if the accessor is a property and either the getter or setter overrides ANY method of ANY superclass, exclude it.
            if (overridesAnother(((PropertyElement) accessor).getGetter(), filter) || overridesAnother(((PropertyElement) accessor).getSetter(), filter)) {
              continue;
            }
          }

          memberAccessors.add(new Member(accessor, this, context));
        }
      }
    }

    this.propOrder = propOrder;
    this.members = Collections.unmodifiableSortedSet(memberAccessors);
    this.value = value;
    this.wildcardMember = wildcardMember;
    this.facets.addAll(Facet.gatherFacets(delegate, context.getContext()));
    this.facets.addAll(Facet.gatherFacets(this.env.getElementUtils().getPackageOf(delegate), context.getContext()));
  }

  protected TypeDefinition(TypeDefinition copy) {
    super(copy.delegate, copy.env);
    this.members = copy.members;
    this.value = copy.value;
    this.wildcardMember = copy.wildcardMember;
    this.facets.addAll(copy.facets);
    this.context = copy.context;
    this.propOrder = copy.propOrder;
  }

  public EnunciateJacksonContext getContext() {
    return context;
  }

  /**
   * Load the potential accessors for this type definition.
   *
   * @param filter The filter.
   * @return the potential accessors for this type definition.
   */
  protected List<javax.lang.model.element.Element> loadPotentialAccessors(AccessorFilter filter) {
    if (getKind() == ElementKind.ENUM) {
      return Collections.emptyList(); // ignore properties if enum
    }
    List<VariableElement> potentialFields = new ArrayList<VariableElement>();
    List<PropertyElement> potentialProperties = new ArrayList<PropertyElement>();
    aggregatePotentialAccessors(potentialFields, potentialProperties, this, filter, this.context.isCollapseTypeHierarchy());

    List<javax.lang.model.element.Element> accessors = new ArrayList<javax.lang.model.element.Element>();
    accessors.addAll(potentialFields);
    accessors.addAll(potentialProperties);
    return accessors;
  }

  /**
   * Aggregate the potential accessor into their separate buckets for the given class declaration, recursively including transient superclasses.
   *
   * @param fields     The fields.
   * @param properties The properties.
   * @param clazz      The class.
   * @param filter     The filter.
   */
  protected void aggregatePotentialAccessors(List<VariableElement> fields, List<PropertyElement> properties, DecoratedTypeElement clazz, AccessorFilter filter, boolean inlineAccessorsOfSuperclasses) {
    String fqn = clazz.getQualifiedName().toString();
    if (Object.class.getName().equals(fqn) || Enum.class.getName().equals(fqn)) {
      return;
    }

    DecoratedTypeElement superDeclaration = clazz.getSuperclass() != null ? (DecoratedTypeElement) this.env.getTypeUtils().asElement(clazz.getSuperclass()) : null;
    if (superDeclaration != null && (this.context.isIgnored(superDeclaration) || inlineAccessorsOfSuperclasses)) {
      inlineAccessorsOfSuperclasses = true;
      aggregatePotentialAccessors(fields, properties, superDeclaration, filter, true);
    }

    TypeElement mixin = this.context.lookupMixin(clazz);

    List<VariableElement> fieldElements = new ArrayList<VariableElement>(ElementFilter.fieldsIn(clazz.getEnclosedElements()));
    if (mixin != null) {
      //replace all mixin fields.
      for (VariableElement mixinField : ElementFilter.fieldsIn(mixin.getEnclosedElements())) {
        int index = indexOf(fieldElements, mixinField.getSimpleName().toString());
        if (index >= 0) {
          fieldElements.set(index, mixinField);
        }
        else {
          fieldElements.add(mixinField);
        }
      }
    }

    Set<String> propsIgnore = new HashSet<String>();
    for (VariableElement fieldDeclaration : fieldElements) {
      JsonUnwrapped unwrapped = fieldDeclaration.getAnnotation(JsonUnwrapped.class);
      if (unwrapped != null && unwrapped.enabled()) {
        DecoratedTypeElement element;
        TypeMirror typeMirror = fieldDeclaration.asType();
        switch (typeMirror.getKind()) {
          case DECLARED:
            element = (DecoratedTypeElement) ((DeclaredType)typeMirror).asElement();
            break;
          case TYPEVAR:
            typeMirror = ((TypeVariable) typeMirror).getUpperBound();
            element = (DecoratedTypeElement) ((DeclaredType)typeMirror).asElement();
            break;
          case WILDCARD:
            TypeMirror bound = ((WildcardType) typeMirror).getExtendsBound();
            if (bound == null) {
              bound = ((WildcardType) typeMirror).getSuperBound();
            }
            if (!(bound instanceof DeclaredType)) {
              bound = TypeMirrorUtils.objectType(this.env);
            }
            element = (DecoratedTypeElement) ((DeclaredType)bound).asElement();
            break;
          default:
            throw new EnunciateException(String.format("%s: %s cannot be JSON unwrapped.", fieldDeclaration, typeMirror));
        }

        aggregatePotentialAccessors(fields, properties, element, filter, inlineAccessorsOfSuperclasses);
        //Fix issue #806
        propsIgnore.add(fieldDeclaration.getSimpleName().toString());
      }
      else if (!filter.accept((DecoratedElement) fieldDeclaration)) {
        remove(fieldDeclaration, fields);
      }
      else {
        addOrReplace(fieldDeclaration, fields);
      }
    }

    JacksonPropertySpec propertySpec = new JacksonPropertySpec(this.env);
    List<PropertyElement> propertyElements = new ArrayList<PropertyElement>(clazz.getProperties(propertySpec));
    if (mixin != null) {
      //replace all mixin properties.
      for (PropertyElement mixinProperty : ((DecoratedTypeElement)mixin).getProperties(propertySpec)) {
        int index = indexOf(propertyElements, mixinProperty.getSimpleName().toString());
        if (index >= 0) {
          propertyElements.set(index, mixinProperty);
        }
        else {
          propertyElements.add(mixinProperty);
        }
      }
    }

    for (PropertyElement propertyDeclaration : propertyElements) {
      JsonUnwrapped unwrapped = propertyDeclaration.getAnnotation(JsonUnwrapped.class);
      if (unwrapped != null && unwrapped.enabled()) {
        DecoratedTypeElement element;
        TypeMirror typeMirror = propertyDeclaration.asType();
        switch (typeMirror.getKind()) {
          case DECLARED:
            element = (DecoratedTypeElement) ((DeclaredType)typeMirror).asElement();
            break;
          case TYPEVAR:
            typeMirror = ((TypeVariable) typeMirror).getUpperBound();
            element = (DecoratedTypeElement) ((DeclaredType)typeMirror).asElement();
            break;
          case WILDCARD:
            TypeMirror bound = ((WildcardType) typeMirror).getExtendsBound();
            if (bound == null) {
              bound = ((WildcardType) typeMirror).getSuperBound();
            }
            if (!(bound instanceof DeclaredType)) {
              bound = TypeMirrorUtils.objectType(this.env);
            }
            element = (DecoratedTypeElement) ((DeclaredType)bound).asElement();
            break;
          default:
            throw new EnunciateException(String.format("%s: %s cannot be JSON unwrapped.", propertyDeclaration, typeMirror));
        }

        aggregatePotentialAccessors(fields, properties, element, filter, inlineAccessorsOfSuperclasses);
      }
      else if (!filter.accept(propertyDeclaration) || indexOf(fields, propertyDeclaration.getSimpleName().toString()) >= 0 ||
              propsIgnore.contains(propertyDeclaration.getSimpleName().toString())) {
        remove(propertyDeclaration, properties);
      }
      else {
        addOrReplace(propertyDeclaration, properties);
      }
    }
  }

  protected int indexOf(List<? extends Element> accessors, String name) {
    for (int i = 0; i < accessors.size(); i++) {
      Element accessor = accessors.get(i);
      if (accessor.getSimpleName().toString().equals(name)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Whether the given method declaration overrides any method.
   *
   * @param method The method declaration.
   * @param filter The filter to use when looking for candidates.
   * @return Whether the given method declaration overrides any method.
   */
  private boolean overridesAnother(DecoratedExecutableElement method, AccessorFilter filter) {
    if (method == null) {
      return false;
    }

    if (this.context.isCollapseTypeHierarchy()) {
      return false; //if we're collapsing type hierarchy, ignore supertypes.
    }

    final TypeElement declaringType = (TypeElement) method.getEnclosingElement();
    TypeElement superType = (TypeElement) this.env.getTypeUtils().asElement(declaringType.getSuperclass());
    if (superType != null && !this.context.isIgnored(superType)) {
      while (superType != null && !Object.class.getName().equals(superType.getQualifiedName().toString())) {
        List<ExecutableElement> methods = ElementFilter.methodsIn(superType.getEnclosedElements());
        for (ExecutableElement candidate : methods) {
          if (this.env.getElementUtils().overrides(method, candidate, declaringType)) {
            return filter.accept((DecoratedElement) candidate);
          }
        }

        superType = (TypeElement) this.env.getTypeUtils().asElement(superType.getSuperclass());
      }
    }

    return false;
  }

  /**
   * Add the specified member declaration, or if it is already in the list (by name), replace it.
   *
   * @param memberDeclaration  The member to add/replace.
   * @param memberDeclarations The other members.
   */
  protected <M extends javax.lang.model.element.Element> void addOrReplace(M memberDeclaration, List<M> memberDeclarations) {
    remove(memberDeclaration, memberDeclarations);
    memberDeclarations.add(memberDeclaration);
  }

  /**
   * Remove specified member declaration from the specified list, if it exists..
   *
   * @param memberDeclaration  The member to remove.
   * @param memberDeclarations The other members.
   */
  protected <M extends javax.lang.model.element.Element> void remove(M memberDeclaration, List<M> memberDeclarations) {
    Iterator<M> it = memberDeclarations.iterator();
    while (it.hasNext()) {
      javax.lang.model.element.Element candidate = it.next();
      if (candidate.getSimpleName().equals(memberDeclaration.getSimpleName())) {
        it.remove();
      }
    }
  }

  /**
   * Whether a declaration is a json value.
   *
   * @param declaration The declaration to check.
   * @return Whether a declaration is an value.
   */
  protected boolean isValue(javax.lang.model.element.Element declaration) {
    return (declaration.getAnnotation(JsonValue.class) != null);
  }

  /**
   * Whether the member declaration is a wildcard member.
   *
   * @param declaration The declaration.
   * @return Whether the member declaration is a wildcard member.
   */
  protected boolean isWildcardProperty(javax.lang.model.element.Element declaration) {
    return declaration.getAnnotation(JsonAnyGetter.class) != null;
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
   * The "wildcard" member.
   *
   * @return The "wildcard" member.
   */
  public WildcardMember getWildcardMember() {
    return wildcardMember;
  }

  /**
   * The members of this type definition.
   *
   * @return The members of this type definition.
   */
  public SortedSet<Member> getMembers() {
    return members;
  }

  /**
   * The value of this type definition.
   *
   * @return The value of this type definition.
   */
  public Value getValue() {
    return value;
  }

  /**
   * Whether this is a complex type.
   *
   * @return Whether this is a complex type.
   */
  public boolean isObject() {
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

  public boolean isHasTypeInfo() {
    return getAnnotation(JsonTypeInfo.class) != null;
  }

  public JsonTypeInfo.Id getTypeIdType() {
    JsonTypeInfo.Id typeIdType = JsonTypeInfo.Id.CLASS;

    JsonTypeInfo typeInfo = getAnnotation(JsonTypeInfo.class);
    if (typeInfo != null) {
      typeIdType = typeInfo.use();
    }

    return typeIdType;
  }

  public JsonTypeInfo.As getTypeIdInclusion() {
    JsonTypeInfo.As inclusion = JsonTypeInfo.As.PROPERTY;

    JsonTypeInfo typeInfo = getAnnotation(JsonTypeInfo.class);
    if (typeInfo != null) {
      inclusion = typeInfo.include();
    }

    return inclusion;
  }

  public String getTypeIdProperty() {
    String property = null;

    JsonTypeInfo typeInfo = getAnnotation(JsonTypeInfo.class);
    if (typeInfo != null) {
      property = typeInfo.property();
      if ("".equals(property)) {
        property = null;
      }
    }

    return property;
  }

  public String[] getPropertyOrder() {
    return propOrder;
  }

  public List<Accessor> getAllAccessors() {
    ArrayList<Accessor> accessors = new ArrayList<Accessor>();
    Value value = getValue();
    if (value != null) {
      accessors.add(value);
    }
    accessors.addAll(getMembers());
    return accessors;
  }

  public static class JacksonPropertySpec extends ElementUtils.DefaultPropertySpec {

    public JacksonPropertySpec(DecoratedProcessingEnvironment env) {
      super(env);
    }

    @Override
    public boolean isGetter(DecoratedExecutableElement executable) {
      return executable.isGetter() || (executable.getParameters().isEmpty() && (executable.getAnnotation(JsonProperty.class) != null || executable.getAnnotation(JsonValue.class) != null));
    }

    @Override
    public boolean isSetter(DecoratedExecutableElement executable) {
      return executable.isSetter() || (executable.getParameters().size() == 1 && (executable.getAnnotation(JsonProperty.class) != null || executable.getAnnotation(JsonValue.class) != null));
    }

    @Override
    public String getPropertyName(DecoratedExecutableElement method) {
      JsonProperty jsonProperty = method.getAnnotation(JsonProperty.class);
      if (jsonProperty != null) {
        String propertyName = jsonProperty.value();
        if (!propertyName.isEmpty()) {
          return propertyName;
        }
      }

      if (method.isGetter() || method.isSetter()) {
        return method.getPropertyName();
      }

      return method.getSimpleName().toString();
    }
  }

}
