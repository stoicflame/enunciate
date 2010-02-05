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

package org.codehaus.enunciate.contract.jaxrs;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.util.Declarations;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.DeclarationDecorator;
import net.sf.jelly.apt.decorations.declaration.DecoratedDeclaration;
import net.sf.jelly.apt.decorations.declaration.DecoratedTypeDeclaration;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;

import javax.ws.rs.*;
import java.util.*;

/**
 * A JAX-RS resource.
 *
 * @author Ryan Heaton
 */
public abstract class Resource extends DecoratedTypeDeclaration {
  
  private final Set<String> consumesMime;
  private final Set<String> producesMime;
  private final List<ResourceParameter> resourceParameters;
  private final List<ResourceMethod> resourceMethods;
  private final List<SubResourceLocator> resourceLocators;

  public Resource(TypeDeclaration delegate) {
    super(delegate);

    Set<String> consumes = new TreeSet<String>();
    Consumes consumesInfo = delegate.getAnnotation(Consumes.class);
    if (consumesInfo != null) {
      consumes.addAll(Arrays.asList(consumesInfo.value()));
    }
    else {
      consumes.add("*/*");
    }
    this.consumesMime = Collections.unmodifiableSet(consumes);

    Set<String> produces = new TreeSet<String>();
    Produces producesInfo = delegate.getAnnotation(Produces.class);
    if (producesInfo != null) {
      produces.addAll(Arrays.asList(producesInfo.value()));
    }
    else {
      produces.add("*/*");
    }
    this.producesMime = Collections.unmodifiableSet(produces);
    this.resourceParameters = Collections.unmodifiableList(getResourceParameters(delegate));
    this.resourceMethods = Collections.unmodifiableList(getResourceMethods(delegate));
    this.resourceLocators = Collections.unmodifiableList(getSubresourceLocators(delegate));
  }

  /**
   * Get the sub-resource locators for the specified type.
   *
   * @param delegate The type.
   * @return The sub-resource locators.
   */
  protected List<SubResourceLocator> getSubresourceLocators(TypeDeclaration delegate) {
    if (delegate == null || delegate.getQualifiedName().equals(Object.class.getName())) {
      return Collections.emptyList();
    }

    ArrayList<SubResourceLocator> resourceLocators = new ArrayList<SubResourceLocator>();
    METHOD_LOOP : for (MethodDeclaration methodDeclaration : delegate.getMethods()) {
      if (methodDeclaration.getAnnotation(Path.class) != null) { //sub-resource locators are annotated with @Path AND they have no resource method designator.
        for (AnnotationMirror annotation : methodDeclaration.getAnnotationMirrors()) {
          AnnotationTypeDeclaration annotationDeclaration = annotation.getAnnotationType().getDeclaration();
          if (annotationDeclaration != null) {
            if (annotationDeclaration.getAnnotation(HttpMethod.class) != null) {
              continue METHOD_LOOP;
            }
          }
        }

        resourceLocators.add(new SubResourceLocator(methodDeclaration, this));
      }
    }

    //some methods may be specified by a superclass and/or implemented interface.  But the annotations on the current class take precedence.
    for (InterfaceType interfaceType : delegate.getSuperinterfaces()) {
      List<SubResourceLocator> interfaceMethods = getSubresourceLocators(interfaceType.getDeclaration());
      for (SubResourceLocator interfaceMethod : interfaceMethods) {
        if (!isOverridden(interfaceMethod, resourceLocators)) {
          resourceLocators.add(interfaceMethod);
        }
      }
    }

    if (delegate instanceof ClassDeclaration) {
      ClassType superclass = ((ClassDeclaration) delegate).getSuperclass();
      if (superclass != null && superclass.getDeclaration() != null) {
        List<SubResourceLocator> superMethods = getSubresourceLocators(superclass.getDeclaration());
        for (SubResourceLocator superMethod : superMethods) {
          if (!isOverridden(superMethod, resourceLocators)) {
            resourceLocators.add(superMethod);
          }
        }
      }
    }

    return resourceLocators;
  }

  /**
   * Get all the resource methods for the specified type.
   *
   * @param delegate The type.
   * @return The resource methods.
   */
  protected List<ResourceMethod> getResourceMethods(final TypeDeclaration delegate) {
    if (delegate == null || delegate.getQualifiedName().equals(Object.class.getName())) {
      return Collections.emptyList();
    }

    ArrayList<ResourceMethod> resourceMethods = new ArrayList<ResourceMethod>();
    for (MethodDeclaration methodDeclaration : delegate.getMethods()) {
      if (methodDeclaration.getModifiers().contains(Modifier.PUBLIC)) {
        for (AnnotationMirror annotation : methodDeclaration.getAnnotationMirrors()) {
          AnnotationTypeDeclaration annotationDeclaration = annotation.getAnnotationType().getDeclaration();
          if (annotationDeclaration != null) {
            if (annotationDeclaration.getAnnotation(HttpMethod.class) != null) {
              resourceMethods.add(new ResourceMethod(methodDeclaration, this));
              break;
            }
          }
        }
      }
    }

    //some methods may be specified by a superclass and/or implemented interface.  But the annotations on the current class take precedence.
    for (InterfaceType interfaceType : delegate.getSuperinterfaces()) {
      List<ResourceMethod> interfaceMethods = getResourceMethods(interfaceType.getDeclaration());
      for (ResourceMethod interfaceMethod : interfaceMethods) {
        if (!isOverridden(interfaceMethod, resourceMethods)) {
          resourceMethods.add(interfaceMethod);
        }
      }
    }

    if (delegate instanceof ClassDeclaration) {
      ClassType superclass = ((ClassDeclaration) delegate).getSuperclass();
      if (superclass != null && superclass.getDeclaration() != null) {
        List<ResourceMethod> superMethods = getResourceMethods(superclass.getDeclaration());
        for (ResourceMethod superMethod : superMethods) {
          if (!isOverridden(superMethod, resourceMethods)) {
            resourceMethods.add(superMethod);
          }
        }
      }
    }

    return resourceMethods;
  }

  /**
   * Get the resource parameters for the specified delegate.
   *
   * @param delegate The delegate.
   * @return The resource parameters.
   */
  protected List<ResourceParameter> getResourceParameters(TypeDeclaration delegate) {
    if (delegate == null || delegate.getQualifiedName().equals(Object.class.getName())) {
      return Collections.emptyList();
    }

    List<ResourceParameter> resourceParameters = new ArrayList<ResourceParameter>();
    DecoratedTypeDeclaration decorated = (DecoratedTypeDeclaration) DeclarationDecorator.decorate(delegate);
    for (FieldDeclaration field : decorated.getFields()) {
      if (ResourceParameter.isResourceParameter(field)) {
        resourceParameters.add(new ResourceParameter(field));
      }
    }

    for (PropertyDeclaration property : decorated.getProperties()) {
      if (ResourceParameter.isResourceParameter(property)) {
        resourceParameters.add(new ResourceParameter(property));
      }
    }

    if (delegate instanceof ClassDeclaration) {
      List<ResourceParameter> superParams = getResourceParameters(((ClassDeclaration) delegate).getSuperclass().getDeclaration());
      for (ResourceParameter superParam : superParams) {
        if (!isHidden(superParam, resourceParameters)) {
          resourceParameters.add(superParam);
        }
      }
    }

    return resourceParameters;
  }

  /**
   * Whether the specified method is overridden by any of the methods in the specified list.
   *
   * @param method The method.
   * @param resourceMethods The method list.
   * @return If the methdo is overridden by any of the methods in the list.
   */
  protected boolean isOverridden(DecoratedDeclaration method, ArrayList<? extends DecoratedDeclaration> resourceMethods) {
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    Declarations decls = env.getDeclarationUtils();

    Declaration unwrappedMethod = method.getDelegate();
    while (unwrappedMethod instanceof DecoratedDeclaration) {
      unwrappedMethod = ((DecoratedDeclaration) unwrappedMethod).getDelegate();
    }

    for (DecoratedDeclaration resourceMethod : resourceMethods) {
      Declaration candidate = resourceMethod.getDelegate();
      while (candidate instanceof DecoratedDeclaration) {
        //unwrap the candidate.
        candidate = ((DecoratedDeclaration) candidate).getDelegate();
      }

      if (decls.overrides((MethodDeclaration) candidate, (MethodDeclaration) unwrappedMethod)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Whether the specified resource parameter is hidden by any of the parameters in the specified list.
   *
   * @param param The param to test.
   * @param resourceParameters The other parameters.
   * @return If the parameter is hidden by any of the parameters in the specified list.
   */
  private boolean isHidden(ResourceParameter param, List<ResourceParameter> resourceParameters) {
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    Declarations decls = env.getDeclarationUtils();

    Declaration unwrappedParam = param.getDelegate();
    while (unwrappedParam instanceof DecoratedDeclaration) {
      unwrappedParam = ((DecoratedDeclaration) unwrappedParam).getDelegate();
    }

    for (ResourceParameter resourceParameter : resourceParameters) {
      Declaration candidate = resourceParameter.getDelegate();
      while (candidate instanceof DecoratedDeclaration) {
        //unwrap the candidate.
        candidate = ((DecoratedDeclaration) candidate).getDelegate();
      }

      if (decls.hides((MemberDeclaration) candidate, (MemberDeclaration) unwrappedParam)) {
        return true;
      }
    }

    return false;
  }

  /**
   * The path to this resource.
   *
   * @return The path to this resource.
   */
  public abstract String getPath();

  /**
   * The parent resource.
   *
   * @return The parent resource, or null if this is a root resource.
   */
  public abstract Resource getParent();

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
   * The resource parameters.
   *
   * @return The resource parameters.
   */
  public List<ResourceParameter> getResourceParameters() {
    return resourceParameters;
  }

  /**
   * The resource methods.
   *
   * @return The resource methods.
   */
  public List<ResourceMethod> getResourceMethods() {
    return resourceMethods;
  }

  /**
   * The resource methods.
   *
   * @param loadDescendants Whether to include the resource methods of all sub-resources.
   * @return The resource methods.
   */
  public List<ResourceMethod> getResourceMethods(boolean loadDescendants) {
    if (!loadDescendants) {
      return resourceMethods;
    }
    else {
      List<ResourceMethod> resourceMethods = new ArrayList<ResourceMethod>();
      Stack<Resource> resources = new Stack<Resource>();
      resources.add(this);
      while (!resources.isEmpty()) {
        Resource resource = resources.pop();
        resourceMethods.addAll(resource.getResourceMethods());

        for (SubResourceLocator locator : resource.getResourceLocators()) {
          resources.add(locator.getResource());
        }
      }

      return resourceMethods;
    }
  }

  /**
   * The resource locators.
   *
   * @return The resource locators.
   */
  public List<SubResourceLocator> getResourceLocators() {
    return resourceLocators;
  }
}
