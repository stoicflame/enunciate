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
package com.webcohesion.enunciate.modules.spring_web.model;

import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.modules.spring_web.EnunciateSpringWebContext;

import javax.annotation.security.RolesAllowed;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import java.util.*;

/**
 * A JAX-RS resource.
 *
 * @author Ryan Heaton
 */
public class SpringController extends DecoratedTypeElement implements HasFacets {

  private final EnunciateSpringWebContext context;
  private final Set<String> paths;
  private final Set<String> consumesMime;
  private final Set<String> producesMime;
  private final List<RequestMapping> requestMappings;
  private final Set<Facet> facets = new TreeSet<Facet>();

  public SpringController(TypeElement delegate, EnunciateSpringWebContext context) {
    this(delegate, loadPaths(delegate), context);

  }

  private static Set<String> loadPaths(TypeElement delegate) {
    TreeSet<String> paths = new TreeSet<String>();
    org.springframework.web.bind.annotation.RequestMapping mappingInfo = delegate.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
    paths.addAll(Arrays.asList(mappingInfo.path()));
    paths.addAll(Arrays.asList(mappingInfo.value()));
    if (paths.isEmpty()) {
      paths.add("");
    }
    return paths;
  }

  public SpringController(TypeElement delegate, Set<String> paths, EnunciateSpringWebContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());

    this.context = context;
    this.paths = paths;

    org.springframework.web.bind.annotation.RequestMapping mappingInfo = delegate.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);

    Set<String> consumes = new TreeSet<String>();
    if (mappingInfo != null && mappingInfo.consumes().length > 0) {
      for (String mt : mappingInfo.consumes()) {
        if (!mt.startsWith("!")) {
          consumes.add(mt);
        }
      }
    }
    else {
      consumes.add("*/*");
    }
    this.consumesMime = Collections.unmodifiableSet(consumes);

    Set<String> produces = new TreeSet<String>();
    if (mappingInfo != null && mappingInfo.produces().length > 0) {
      for (String mt : mappingInfo.produces()) {
        if (!mt.startsWith("!")) {
          produces.add(mt);
        }
      }
    }
    else {
      produces.add("*/*");
    }
    this.producesMime = Collections.unmodifiableSet(produces);

    this.facets.addAll(Facet.gatherFacets(delegate));
    this.requestMappings = Collections.unmodifiableList(getRequestMappings(delegate, new TypeVariableContext(), context));
  }

  /**
   * Get all the resource methods for the specified type.
   *
   * @param delegate The type.
   * @param context The context
   * @return The resource methods.
   */
  protected List<RequestMapping> getRequestMappings(final TypeElement delegate, TypeVariableContext variableContext, EnunciateSpringWebContext context) {
    if (delegate == null || delegate.getQualifiedName().toString().equals(Object.class.getName())) {
      return Collections.emptyList();
    }

    ArrayList<RequestMapping> requestMappings = new ArrayList<RequestMapping>();
    for (ExecutableElement method : ElementFilter.methodsIn(delegate.getEnclosedElements())) {
      org.springframework.web.bind.annotation.RequestMapping mappingInfo = method.getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class);
      if (mappingInfo != null) {
        requestMappings.add(new RequestMapping(method, this, variableContext, context));
      }
    }

    //some methods may be specified by a superclass and/or implemented interface.  But the annotations on the current class take precedence.
    for (TypeMirror interfaceType : delegate.getInterfaces()) {
      if (interfaceType instanceof DeclaredType) {
        DeclaredType declared = (DeclaredType) interfaceType;
        TypeElement element = (TypeElement) declared.asElement();
        List<RequestMapping> interfaceMethods = getRequestMappings(element, variableContext.push(element.getTypeParameters(), declared.getTypeArguments()), context);
        for (RequestMapping interfaceMethod : interfaceMethods) {
          if (!isOverridden(interfaceMethod, requestMappings)) {
            requestMappings.add(interfaceMethod);
          }
        }
      }
    }

    if (delegate.getKind() == ElementKind.CLASS) {
      TypeMirror superclass = delegate.getSuperclass();
      if (superclass instanceof DeclaredType && ((DeclaredType)superclass).asElement() != null) {
        DeclaredType declared = (DeclaredType) superclass;
        TypeElement element = (TypeElement) declared.asElement();
        List<RequestMapping> superMethods = getRequestMappings(element, variableContext.push(element.getTypeParameters(), declared.getTypeArguments()), context);
        for (RequestMapping superMethod : superMethods) {
          if (!isOverridden(superMethod, requestMappings)) {
            requestMappings.add(superMethod);
          }
        }
      }
    }

    return requestMappings;
  }

  /**
   * Whether the specified method is overridden by any of the methods in the specified list.
   *
   * @param method The method.
   * @param resourceMethods The method list.
   * @return If the methdo is overridden by any of the methods in the list.
   */
  protected boolean isOverridden(ExecutableElement method, ArrayList<? extends ExecutableElement> resourceMethods) {
    Elements decls = this.env.getElementUtils();


    for (ExecutableElement resourceMethod : resourceMethods) {
      if (decls.overrides(resourceMethod, method, (TypeElement) resourceMethod.getEnclosingElement())) {
        return true;
      }
    }

    return false;
  }

  public EnunciateSpringWebContext getContext() {
    return context;
  }

  /**
   * The path to this resource.
   *
   * @return The path to this resource.
   */
  public final Set<String> getPaths() {
    return this.paths;
  }

  /**
   * The MIME types that the methods on this resource consumes (possibly overridden).
   *
   * @return The MIME types that the methods on this resource consumes.
   */
  public Set<String> getConsumesMime() {
    return consumesMime;
  }

  /**
   * The MIME types that the methods on this resource consumes (possibly overridden).
   *
   * @return The MIME types that the methods on this resource consumes.
   */
  public Set<String> getProducesMime() {
    return producesMime;
  }

  /**
   * The resource methods.
   *
   * @return The resource methods.
   */
  public List<RequestMapping> getRequestMappings() {
    return requestMappings;
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
   * The security roles for this resource.
   *
   * @return The security roles for this resource.
   */
  public Set<String> getSecurityRoles() {
    TreeSet<String> roles = new TreeSet<String>();
    RolesAllowed rolesAllowed = getAnnotation(RolesAllowed.class);
    if (rolesAllowed != null) {
      Collections.addAll(roles, rolesAllowed.value());
    }
    return roles;
  }
}
