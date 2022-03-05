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
package com.webcohesion.enunciate.modules.jaxrs.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;

import static com.webcohesion.enunciate.modules.jaxrs.model.Resource.extractPathComponents;

/**
 * A sub-resource locator.  Invoked on a JAX-RS resource in order to locate a subresource.
 *
 * @author Ryan Heaton
 */
public class SubResourceLocator extends DecoratedExecutableElement implements PathContext {

  private String path;
  private final List<PathSegment> pathComponents;
  private final SubResource resource;
  private final Resource parent;
  private final List<ResourceParameter> resourceParameters;
  private final VariableElement entityParameter;
  private final EnunciateJaxrsContext context;

  public SubResourceLocator(ExecutableElement delegate, Resource parent, TypeVariableContext variableContext, EnunciateJaxrsContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.context = context;
    this.parent = parent;

    javax.ws.rs.Path p = delegate.getAnnotation(javax.ws.rs.Path.class);
    jakarta.ws.rs.Path p2 = delegate.getAnnotation(jakarta.ws.rs.Path.class);
    if(p != null || p2 != null) {
        this.path = p != null ? p.value() : p2.value();
    }
    if (this.path == null) {
      throw new IllegalArgumentException("A subresource locator must specify a path with the " 
              + (p != null ? "@javax.ws.rs.Path" : "@jakarta.ws.rs.Path") + " annotation.");
    }
    this.pathComponents = extractPathComponents(this.path);

    SubResource resource;
    TypeMirror returnType = delegate.getReturnType();
    if ((returnType instanceof DeclaredType) && ((DeclaredType) returnType).asElement() != null) {
      TypeElement declaration = (TypeElement) ((DeclaredType) returnType).asElement();
      if (Class.class.getName().equals(declaration.getQualifiedName().toString())) {
        //subresource locators may return the class instead of the instance, so unwrap.
        List<? extends TypeMirror> classTypes = ((DeclaredType) returnType).getTypeArguments();
        if (classTypes != null && classTypes.size() > 0) {
          returnType = classTypes.get(0);
          if ((returnType instanceof DeclaredType) && ((DeclaredType) returnType).asElement() != null) {
            declaration = (TypeElement) ((DeclaredType) returnType).asElement();
            resource = findRecursiveSubResource(declaration, getPath());
            resource = resource == null ? new SubResource(declaration, getPath(), this, variableContext, context) : resource;
          }
          else {
            resource = new SubResource((TypeElement) TypeMirrorUtils.objectType(context.getContext().getProcessingEnvironment()).asElement(), getPath(), this, variableContext, context);
          }
        }
        else {
          resource = new SubResource((TypeElement) TypeMirrorUtils.objectType(context.getContext().getProcessingEnvironment()).asElement(), getPath(), this, variableContext, context);
        }
      }
      else {
        resource = findRecursiveSubResource(declaration, getPath());
        resource = resource == null ? new SubResource(declaration, getPath(), this, variableContext, context) : resource;
      }
    }
    else {
      resource = new SubResource((TypeElement) TypeMirrorUtils.objectType(context.getContext().getProcessingEnvironment()).asElement(), getPath(), this, variableContext, context);
    }
    this.resource = resource;

    VariableElement entityParameter = null;
    List<ResourceParameter> resourceParameters = new ArrayList<ResourceParameter>();
    for (VariableElement parameterDeclaration : delegate.getParameters()) {
      if (ResourceParameter.isResourceParameter(parameterDeclaration, context)) {
        resourceParameters.add(new ResourceParameter(parameterDeclaration, this));
      }
      else {
        entityParameter = parameterDeclaration;
      }
    }

    this.entityParameter = entityParameter;
    this.resourceParameters = resourceParameters;
  }

  //fix for ENUNCIATE-574
  private SubResource findRecursiveSubResource(TypeElement declaration, String path) {
    LinkedList<SubResource> ancestorResources = SubResource.ANCESTOR_DECLARATIONS.get();
    for (SubResource ancestorResource : ancestorResources) {
      if (ancestorResource.getQualifiedName().equals(declaration.getQualifiedName()) && ancestorResource.getPath().equals(path)) {
        return ancestorResource;
      }
    }
    return null;
  }

  @Override
  public List<PathSegment> getPathComponents() {
    List<PathSegment> components = new ArrayList<PathSegment>();
    Resource parent = getParent();
    if (parent != null) {
      components.addAll(parent.getPathComponents());
    }
    components.addAll(this.pathComponents);
    return components;
  }

  @Override
  public EnunciateJaxrsContext getContext() {
    return this.context;
  }

  /**
   * The path of the subresource.
   *
   * @return The path of the subresource.
   */
  public String getPath() {
    return this.path;
  }

  /**
   * The resource that this locates.
   *
   * @return The resource that this locates.
   */
  public SubResource getResource() {
    return resource;
  }

  /**
   * The resource that hosts this locator.
   *
   * @return The resource that hosts this locator.
   */
  public Resource getParent() {
    return parent;
  }

  /**
   * The list of resource parameters that this method requires to be invoked.
   *
   * @return The list of resource parameters that this method requires to be invoked.
   */
  public Set<ResourceParameter> getResourceParameters() {
    TreeSet<ResourceParameter> resourceParams = new TreeSet<ResourceParameter>(this.resourceParameters);
    resourceParams.addAll(getParent().getResourceParameters());
    return resourceParams;
  }

  /**
   * The entity parameter.
   *
   * @return The entity parameter, or null if none.
   */
  public VariableElement getEntityParameter() {
    return entityParameter;
  }

}
