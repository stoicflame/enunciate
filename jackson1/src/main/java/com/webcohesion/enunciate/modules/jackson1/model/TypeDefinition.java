/*
 * Copyright 2006-2008 Web Cohesion
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

package com.webcohesion.enunciate.modules.jackson1.model;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.element.PropertyElement;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jackson1.EnunciateJackson1Context;
import org.codehaus.jackson.annotate.*;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
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
  protected final EnunciateJackson1Context context;

  protected TypeDefinition(TypeElement delegate, EnunciateJackson1Context context) {
    super(delegate, context.getContext().getProcessingEnvironment());

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
      AccessorFilter filter = new AccessorFilter(getAnnotation(JsonAutoDetect.class), getAnnotation(JsonIgnoreProperties.class), context.isHonorJaxb(), getAnnotation(XmlAccessorType.class));
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
            if (overridesAnother(((PropertyElement) accessor).getGetter()) || overridesAnother(((PropertyElement) accessor).getSetter())) {
              continue;
            }
          }

          memberAccessors.add(new Member(accessor, this, context));
        }
      }
    }

    this.members = Collections.unmodifiableSortedSet(memberAccessors);
    this.value = value;
    this.wildcardMember = wildcardMember;
    this.facets.addAll(Facet.gatherFacets(delegate));
    this.facets.addAll(Facet.gatherFacets(this.env.getElementUtils().getPackageOf(delegate)));
    this.context = context;
  }

  protected TypeDefinition(TypeDefinition copy) {
    super(copy.delegate, copy.env);
    this.members = copy.members;
    this.value = copy.value;
    this.wildcardMember = copy.wildcardMember;
    this.facets.addAll(copy.facets);
    this.context = copy.context;
  }

  public EnunciateJackson1Context getContext() {
    return context;
  }

  /**
   * Load the potential accessors for this type definition.
   *
   * @param filter The filter.
   * @return the potential accessors for this type definition.
   */
  protected List<javax.lang.model.element.Element> loadPotentialAccessors(AccessorFilter filter) {
    List<VariableElement> potentialFields = new ArrayList<VariableElement>();
    List<PropertyElement> potentialProperties = new ArrayList<PropertyElement>();
    aggregatePotentialAccessors(potentialFields, potentialProperties, this, filter, false);

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
  protected void aggregatePotentialAccessors(List<VariableElement> fields, List<PropertyElement> properties, DecoratedTypeElement clazz, AccessorFilter filter, boolean childIsIgnored) {
    DecoratedTypeElement superDeclaration = clazz.getSuperclass() != null ? (DecoratedTypeElement) this.env.getTypeUtils().asElement(clazz.getSuperclass()) : null;
    if (superDeclaration != null && (isJsonIgnored(superDeclaration) || childIsIgnored)) {
      childIsIgnored = true;
      aggregatePotentialAccessors(fields, properties, superDeclaration, filter, childIsIgnored);
    }

    for (VariableElement fieldDeclaration : ElementFilter.fieldsIn(clazz.getEnclosedElements())) {
      JsonUnwrapped unwrapped = fieldDeclaration.getAnnotation(JsonUnwrapped.class);
      if (unwrapped != null && unwrapped.enabled()) {
        TypeMirror typeMirror = fieldDeclaration.asType();
        if (!(typeMirror instanceof DeclaredType)) {
          throw new EnunciateException(String.format("%s: %s cannot be JSON unwrapped.", fieldDeclaration, typeMirror));
        }
        aggregatePotentialAccessors(fields, properties, (DecoratedTypeElement) ((DeclaredType)typeMirror).asElement(), filter, childIsIgnored);
      }
      else if (!filter.accept((DecoratedElement) fieldDeclaration)) {
        remove(fieldDeclaration, fields);
      }
      else {
        addOrReplace(fieldDeclaration, fields);
      }
    }

    for (PropertyElement propertyDeclaration : clazz.getProperties()) {
      JsonUnwrapped unwrapped = propertyDeclaration.getAnnotation(JsonUnwrapped.class);
      if (unwrapped != null && unwrapped.enabled()) {
        TypeMirror typeMirror = propertyDeclaration.asType();
        if (!(typeMirror instanceof DeclaredType)) {
          throw new EnunciateException(String.format("%s: %s cannot be JSON unwrapped.", propertyDeclaration, typeMirror));
        }
        aggregatePotentialAccessors(fields, properties, (DecoratedTypeElement) ((DeclaredType)typeMirror).asElement(), filter, childIsIgnored);
      }
      else if (!filter.accept(propertyDeclaration)) {
        remove(propertyDeclaration, properties);
      }
      else {
        addOrReplace(propertyDeclaration, properties);
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
    while (superType != null && !Object.class.getName().equals(superType.getQualifiedName().toString())) {
      List<ExecutableElement> methods = ElementFilter.methodsIn(superType.getEnclosedElements());
      for (ExecutableElement candidate : methods) {
        if (this.env.getElementUtils().overrides(method, candidate, declaringType)) {
          return true;
        }
      }

      superType = (TypeElement) this.env.getTypeUtils().asElement(superType.getSuperclass());
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
   * Whether a declaration is json ignored.
   *
   * @param declaration The declaration on which to determine json ignorance.
   * @return Whether a declaration is json ignored.
   */
  protected boolean isJsonIgnored(javax.lang.model.element.Element declaration) {
    JsonIgnore ignore = declaration.getAnnotation(JsonIgnore.class);
    return (ignore != null && ignore.value());
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

}
