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
package com.webcohesion.enunciate.modules.jaxrs.model;

import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.element.PropertyElement;
import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;
import com.webcohesion.enunciate.modules.jaxrs.model.util.JaxrsUtil;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.ws.rs.Consumes;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.*;

/**
 * A JAX-RS resource.
 *
 * @author Ryan Heaton
 */
public abstract class Resource extends DecoratedTypeElement implements HasFacets {

  private final EnunciateJaxrsContext context;
  private final String path;
  private final Set<String> consumesMime;
  private final Set<String> producesMime;
  private final List<ResourceParameter> resourceParameters;
  private final List<ResourceMethod> resourceMethods;
  private final List<SubResourceLocator> resourceLocators;
  private final Set<Facet> facets = new TreeSet<Facet>();

  protected Resource(TypeElement delegate, String path, EnunciateJaxrsContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());

    this.context = context;

    if (path == null) {
      throw new NullPointerException();
    }
    this.path = path;

    Set<String> consumes = new TreeSet<String>();
    Consumes consumesInfo = delegate.getAnnotation(Consumes.class);
    if (consumesInfo != null) {
      consumes.addAll(JaxrsUtil.value(consumesInfo));
    }
    else {
      consumes.add("*/*");
    }
    this.consumesMime = Collections.unmodifiableSet(consumes);

    Set<String> produces = new TreeSet<String>();
    Produces producesInfo = delegate.getAnnotation(Produces.class);
    if (producesInfo != null) {
      produces.addAll(JaxrsUtil.value(producesInfo));
    }
    else {
      produces.add("*/*");
    }
    this.producesMime = Collections.unmodifiableSet(produces);

    this.facets.addAll(Facet.gatherFacets(delegate));
    this.resourceParameters = Collections.unmodifiableList(getResourceParameters(delegate, context));
    this.resourceMethods = Collections.unmodifiableList(getResourceMethods(delegate, context));
    this.resourceLocators = Collections.unmodifiableList(getSubresourceLocators(delegate, context));
  }

  /**
   * Get the sub-resource locators for the specified type.
   *
   * @param delegate The type.
   * @param context The context
   * @return The sub-resource locators.
   */
  protected List<SubResourceLocator> getSubresourceLocators(TypeElement delegate, EnunciateJaxrsContext context) {
    if (delegate == null || delegate.getQualifiedName().toString().equals(Object.class.getName())) {
      return Collections.emptyList();
    }

    ArrayList<SubResourceLocator> resourceLocators = new ArrayList<SubResourceLocator>();
    METHOD_LOOP : for (ExecutableElement methodElement : ElementFilter.methodsIn(delegate.getEnclosedElements())) {
      if (methodElement.getAnnotation(Path.class) != null) { //sub-resource locators are annotated with @Path AND they have no resource method designator.
        for (AnnotationMirror annotation : methodElement.getAnnotationMirrors()) {
          Element annotationElement = annotation.getAnnotationType().asElement();
          if (annotationElement != null) {
            if (annotationElement.getAnnotation(HttpMethod.class) != null) {
              continue METHOD_LOOP;
            }
          }
        }

        resourceLocators.add(new SubResourceLocator(methodElement, this, this.context));
      }
    }

    //some methods may be specified by a superclass and/or implemented interface.  But the annotations on the current class take precedence.
    for (TypeMirror superType : delegate.getInterfaces()) {
      if (superType instanceof DeclaredType) {
        List<SubResourceLocator> interfaceMethods = getSubresourceLocators((TypeElement) ((DeclaredType)superType).asElement(), context);
        for (SubResourceLocator interfaceMethod : interfaceMethods) {
          if (!isOverridden(interfaceMethod, resourceLocators)) {
            resourceLocators.add(interfaceMethod);
          }
        }
      }
    }

    if (delegate.getKind() == ElementKind.CLASS) {
      TypeMirror superclass = delegate.getSuperclass();
      if (superclass instanceof DeclaredType && ((DeclaredType)superclass).asElement() != null) {
        List<SubResourceLocator> superMethods = getSubresourceLocators((TypeElement) ((DeclaredType) superclass).asElement(), context);
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
   * @param context The context
   * @return The resource methods.
   */
  protected List<ResourceMethod> getResourceMethods(final TypeElement delegate, EnunciateJaxrsContext context) {
    if (delegate == null || delegate.getQualifiedName().toString().equals(Object.class.getName())) {
      return Collections.emptyList();
    }

    ArrayList<ResourceMethod> resourceMethods = new ArrayList<ResourceMethod>();
    for (ExecutableElement method : ElementFilter.methodsIn(delegate.getEnclosedElements())) {
      if (method.getModifiers().contains(Modifier.PUBLIC)) {
        for (AnnotationMirror annotation : method.getAnnotationMirrors()) {
          Element annotationElement = annotation.getAnnotationType().asElement();
          if (annotationElement != null) {
            if (annotationElement.getAnnotation(HttpMethod.class) != null) {
              resourceMethods.add(new ResourceMethod(method, this, context));
              break;
            }
          }
        }
      }
    }

    //some methods may be specified by a superclass and/or implemented interface.  But the annotations on the current class take precedence.
    for (TypeMirror interfaceType : delegate.getInterfaces()) {
      if (interfaceType instanceof DeclaredType) {
        List<ResourceMethod> interfaceMethods = getResourceMethods((TypeElement) ((DeclaredType)interfaceType).asElement(), context);
        for (ResourceMethod interfaceMethod : interfaceMethods) {
          if (!isOverridden(interfaceMethod, resourceMethods)) {
            resourceMethods.add(interfaceMethod);
          }
        }
      }
    }

    if (delegate.getKind() == ElementKind.CLASS) {
      TypeMirror superclass = delegate.getSuperclass();
      if (superclass instanceof DeclaredType && ((DeclaredType)superclass).asElement() != null) {
        List<ResourceMethod> superMethods = getResourceMethods((TypeElement) ((DeclaredType) superclass).asElement(), context);
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
   * @param context The context
   * @return The resource parameters.
   */
  protected List<ResourceParameter> getResourceParameters(TypeElement delegate, EnunciateJaxrsContext context) {
    if (delegate == null || delegate.getQualifiedName().toString().equals(Object.class.getName())) {
      return Collections.emptyList();
    }

    List<ResourceParameter> resourceParameters = new ArrayList<ResourceParameter>();
    for (VariableElement field : ElementFilter.fieldsIn(delegate.getEnclosedElements())) {
      if (ResourceParameter.isResourceParameter(field, this.context)) {
        resourceParameters.add(new ResourceParameter(field, context));
      }
    }

    for (PropertyElement property : ((DecoratedTypeElement)delegate).getProperties()) {
      if (ResourceParameter.isResourceParameter(property, this.context)) {
        resourceParameters.add(new ResourceParameter(property, context));
      }
    }

    if (delegate.getKind() == ElementKind.CLASS) {
      List<ResourceParameter> superParams = getResourceParameters((TypeElement) ((DeclaredType) delegate.getSuperclass()).asElement(), context);
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
  protected boolean isOverridden(ExecutableElement method, ArrayList<? extends ExecutableElement> resourceMethods) {
    Elements decls = this.env.getElementUtils();


    for (ExecutableElement resourceMethod : resourceMethods) {
      if (decls.overrides(resourceMethod, method, (TypeElement) resourceMethod.getEnclosingElement())) {
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
    Elements decls = this.env.getElementUtils();

    for (ResourceParameter resourceParameter : resourceParameters) {
      if (decls.hides(resourceParameter, param)) {
        return true;
      }
    }

    return false;
  }

  public EnunciateJaxrsContext getContext() {
    return context;
  }

  /**
   * The path to this resource.
   *
   * @return The path to this resource.
   */
  public final String getPath() {
    return this.path;
  }

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
      LinkedList<Resource> resources = new LinkedList<Resource>();
      Set<String> visited = new TreeSet<String>();
      resources.add(this);
      while (!resources.isEmpty()) {
        Resource resource = resources.pop();
        visited.add(resource.getQualifiedName().toString());
        resourceMethods.addAll(resource.getResourceMethods());

        for (SubResourceLocator locator : resource.getResourceLocators()) {
          SubResource subresource = locator.getResource();
          if (!visited.contains(subresource.getQualifiedName().toString())) {
            resources.add(subresource);
          }
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

  /**
   * The facets here applicable.
   *
   * @return The facets here applicable.
   */
  public Set<Facet> getFacets() {
    return facets;
  }

}
