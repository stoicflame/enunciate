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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.*;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;
import com.webcohesion.enunciate.modules.jackson.javac.ToStringValueProperty;
import com.webcohesion.enunciate.util.AccessorBag;
import com.webcohesion.enunciate.util.AnnotationUtils;
import com.webcohesion.enunciate.util.SortedList;

import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;

/**
 * A json type definition.
 *
 * @author Ryan Heaton
 */
public abstract class TypeDefinition extends DecoratedTypeElement implements HasFacets {

  private final List<Member> members;
  private final Value value;
  private final WildcardMember wildcardMember;
  private final LinkedList<javax.lang.model.element.Element> referencedFrom = new LinkedList<>();
  private final Set<Facet> facets = new TreeSet<>();
  protected final EnunciateJacksonContext context;
  private final String[] propOrder;
  private String typeIdProperty;

  protected TypeDefinition(TypeElement delegate, EnunciateJacksonContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.context = context;

    String[] propOrder = null;
    boolean alphabetical = context.isPropertiesAlphabetical();

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
    SortedList<Member> memberAccessors = new SortedList<>(comparator);
    Value value = loadToStringValue(context);
    WildcardMember wildcardMember = null;
    JsonIgnoreType ignoreType = getAnnotation(JsonIgnoreType.class);
    boolean ignoreThisType = ignoreType == null || !ignoreType.value();
    boolean hasToStringValue = value != null;
    if (ignoreThisType && !hasToStringValue) {
      AccessorFilter filter = new AccessorFilter(context, getAnnotation(JsonAutoDetect.class), getAnnotation(JsonIgnoreProperties.class), getAnnotation(XmlAccessorType.class));
      value = null;

      wildcardMember = null;
      final AccessorBag accessorBag = loadPotentialAccessors(filter);
      this.typeIdProperty = accessorBag.typeIdProperty;
      for (javax.lang.model.element.Element accessor : accessorBag.getAccessors()) {
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
    
    memberAccessors.removeDuplicates(Comparator.comparing(new Function<Member, String>() {
      @Override
      public String apply(Member member) {
        return member.getName();
      }
    }));
    this.propOrder = propOrder;
    this.members = Collections.unmodifiableList(memberAccessors);
    this.value = value;
    this.wildcardMember = wildcardMember;
    if (delegate instanceof HasFacets) {
      this.facets.addAll(((HasFacets) delegate).getFacets());
    }
    else {
      this.facets.addAll(Facet.gatherFacets(delegate, context.getContext()));
      this.facets.addAll(Facet.gatherFacets(this.env.getElementUtils().getPackageOf(delegate), context.getContext()));
    }
  }

  private Value loadToStringValue(EnunciateJacksonContext context) {
    for (ExecutableElement method : getMethods()) {
      if (method.getSimpleName().contentEquals("toString") && method.getParameters().isEmpty() && method.getModifiers().contains(Modifier.PUBLIC) && method.getAnnotation(JsonValue.class) != null) {
        return new Value(new ToStringValueProperty(method, context.getContext().getProcessingEnvironment()), this, context);
      }
    }
    return null;
  }

  protected TypeDefinition(TypeDefinition copy) {
    super(copy.delegate, copy.env);
    this.members = copy.members;
    this.value = copy.value;
    this.wildcardMember = copy.wildcardMember;
    this.facets.addAll(copy.facets);
    this.context = copy.context;
    this.propOrder = copy.propOrder;
    this.typeIdProperty = copy.typeIdProperty;
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
  protected AccessorBag loadPotentialAccessors(AccessorFilter filter) {
    if (getKind() == ElementKind.ENUM) {
      return new AccessorBag(); // ignore properties if enum
    }
    AccessorBag bag = new AccessorBag();
    aggregatePotentialAccessors(bag, this, filter, this.context.isCollapseTypeHierarchy());
    return bag;
  }

  /**
   * Aggregate the potential accessor into their separate buckets for the given class declaration, recursively including transient superclasses.
   *
   * @param bag        The collected fields and properties.
   * @param clazz      The class.
   * @param filter     The filter.
   */
  protected void aggregatePotentialAccessors(AccessorBag bag, DecoratedTypeElement clazz, AccessorFilter filter, boolean inlineAccessorsOfSuperclasses) {
    String fqn = clazz.getQualifiedName().toString();
    if (Object.class.getName().equals(fqn) || Enum.class.getName().equals(fqn) || Record.class.getName().equals(fqn)) {
      return;
    }

    if (bag.typeIdProperty == null) {
      final JsonTypeInfo info = clazz.getAnnotation(JsonTypeInfo.class);
      if (info != null && !info.property().isEmpty()) {
        bag.typeIdProperty = info.property();
      }
    }

    DecoratedTypeElement superDeclaration = clazz.getSuperclass() != null ? (DecoratedTypeElement) this.env.getTypeUtils().asElement(clazz.getSuperclass()) : null;
    if (superDeclaration != null && (this.context.isIgnored(superDeclaration) || inlineAccessorsOfSuperclasses)) {
      inlineAccessorsOfSuperclasses = true;
      aggregatePotentialAccessors(bag, superDeclaration, filter, true);
    }

    TypeElement mixin = this.context.lookupMixin(clazz);
    if (mixin == null) {
      DeclaredType as = refineType(env, new DecoratedTypeElement(clazz, env), JsonSerialize.class, JsonSerialize::as);
      if (as == null) {
        as = refineType(env, new DecoratedTypeElement(clazz, env), JsonDeserialize.class, JsonDeserialize::as);
      }
      if (as != null) {
        mixin = (TypeElement) as.asElement();
      }
    }

    List<Element> fieldElements = ElementUtils.fieldsOrRecordComponentsIn(clazz);
    if (mixin != null) {
      //replace all mixin fields.
      for (Element mixinField : ElementUtils.fieldsOrRecordComponentsIn(mixin)) {
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
    for (Element fieldDeclaration : fieldElements) {
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

        aggregatePotentialAccessors(bag, element, filter, inlineAccessorsOfSuperclasses);
        //Fix issue #806
        propsIgnore.add(fieldDeclaration.getSimpleName().toString());
      }
      else if (!filter.accept((DecoratedElement) fieldDeclaration)) {
        bag.fields.removeByName(fieldDeclaration);
      }
      else {
        bag.fields.addOrReplace(fieldDeclaration);
      }
    }

    JacksonPropertySpec propertySpec = new JacksonPropertySpec(this.env);
    List<PropertyElement> propertyElements = new ArrayList<>(clazz.getProperties(propertySpec));
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

        aggregatePotentialAccessors(bag, element, filter, inlineAccessorsOfSuperclasses);
      }
      else if (!filter.accept(propertyDeclaration) || indexOf(bag.fields, propertyDeclaration.getSimpleName().toString()) >= 0 ||
              propsIgnore.contains(propertyDeclaration.getSimpleName().toString())) {
        bag.properties.removeByName(propertyDeclaration);
      }
      else {
        bag.properties.addOrReplace(propertyDeclaration);
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
  public List<Member> getMembers() {
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
    return typeIdProperty;
  }

  public String getTypeIdValue() {
    List<JsonSubTypes> subTypes = AnnotationUtils.getAnnotations(JsonSubTypes.class, this, false);
    if (!subTypes.isEmpty()) {
      final Types typeUtils = env.getTypeUtils();
      for (JsonSubTypes.Type type : subTypes.get(0).value()) {
        DecoratedTypeMirror decoratedType = Annotations.mirrorOf(type::value, env);
        if (typeUtils.isSameType(asType(), decoratedType.getDelegate())) {
          if (!type.name().isEmpty()) {
            return type.name();
          }
        }
      }
    }
    JsonTypeName typeName = getAnnotation(JsonTypeName.class);
    if (typeName != null && !typeName.value().isEmpty()) {
      return typeName.value();
    }
    return isAbstract() ? "..." : getSimpleName().toString();
  }

  public String[] getPropertyOrder() {
    return propOrder;
  }

  public boolean isHasSubTypes() {
    JsonSubTypes subtypes = getAnnotation(JsonSubTypes.class);
    return subtypes != null && subtypes.value().length > 0;
  }

  public Map<String, DecoratedTypeMirror> getSubTypes() {
    Map<String, DecoratedTypeMirror> subtypes = null;
    JsonSubTypes info = getAnnotation(JsonSubTypes.class);
    if (info != null) {
      subtypes = new TreeMap<>();
      for (JsonSubTypes.Type type : info.value()) {
        DecoratedTypeMirror t;
        TypeElement te;
        try {
          te = this.env.getElementUtils().getTypeElement(type.value().getName());
          t = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(te.asType(), this.env);
        }
        catch (MirroredTypeException e) {
          t = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(e.getTypeMirror(), this.env);
          Element el = t instanceof DeclaredType ? ((DeclaredType) t).asElement() : null;
          te = el instanceof TypeElement ? (TypeElement) el : null;
        }

        String name = type.name();
        if (name.isEmpty()) {
          if (te != null) {
            JsonTypeName typeName = te.getAnnotation(JsonTypeName.class);
            if (typeName != null) {
              name = typeName.value();
            }
          }
        }

        if (name.isEmpty() && te != null) {
          name = te.getSimpleName().toString();
        }

        if (name.isEmpty()) {
          continue;
        }

        subtypes.put(name, t);
      }
    }

    return subtypes;
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
  
  public String getTypeFormat() {
    return this.context.getConfiguredTypeFormat(this);
  }

  static <A extends Annotation> DeclaredType refineType(DecoratedProcessingEnvironment env, DecoratedElement<?> element, Class<A> annotation, Function<A, Class<?>> refiner) {
      Element elt = element;
      while (elt != null && elt.getKind() != ElementKind.CLASS && elt.getKind() != ElementKind.INTERFACE && elt.getKind() != ElementKind.RECORD) {
        elt = elt.getEnclosingElement();
      }
      if (elt == null) {
        return null;
      }
      final A js = elt.getAnnotation(annotation);
      if (js == null) {
        return null;
      }
      return (DeclaredType) Annotations.mirrorOf(() -> refiner.apply(js), env, Void.class);
  }

  public static class JacksonPropertySpec extends ElementUtils.DefaultPropertySpec {

    public JacksonPropertySpec(DecoratedProcessingEnvironment env) {
      super(env);
    }

    private <A extends Annotation> DecoratedExecutableElement refine(DecoratedExecutableElement executable, Class<A> annotation, Function<A, Class<?>> refiner) {
      DeclaredType as = refineType(env, executable, annotation, refiner);
      if (as == null) {
        return executable;
      }
      for (Element elem : as.asElement().getEnclosedElements()) {
        if (elem.getSimpleName().equals(executable.getSimpleName()) && elem instanceof ExecutableElement) {
          return new DecoratedExecutableElement((ExecutableElement) elem, env);
        }
      }
      return executable;
    }

    @Override
    public boolean isGetter(DecoratedExecutableElement executable) {
      executable = refine(executable, JsonSerialize.class, JsonSerialize::as);
      return executable.isGetter() || (executable.getParameters().isEmpty() && (executable.getAnnotation(JsonProperty.class) != null || executable.getAnnotation(JsonValue.class) != null));
    }

    @Override
    public boolean isSetter(DecoratedExecutableElement executable) {
      executable = refine(executable, JsonDeserialize.class, JsonDeserialize::as);
      return executable.isSetter() || (executable.getParameters().size() == 1 && (executable.getAnnotation(JsonProperty.class) != null || executable.getAnnotation(JsonValue.class) != null));
    }

    @Override
    public String getPropertyName(DecoratedExecutableElement method) {
      JsonProperty jsonProperty = refine(method, JsonSerialize.class, JsonSerialize::as).getAnnotation(JsonProperty.class);
      if (jsonProperty != null) {
        String propertyName = jsonProperty.value();
        if (!propertyName.isEmpty()) {
          return propertyName;
        }
      }

      return getSimpleName(method);
    }

    @Override
    public String getSimpleName(DecoratedExecutableElement method) {
      if (method.isGetter() || method.isSetter()) {
        return method.getPropertyName();
      }

      return method.getSimpleName().toString();
    }
  }

}
