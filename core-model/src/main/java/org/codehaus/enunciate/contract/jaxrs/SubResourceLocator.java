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

import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.Context;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedMethodDeclaration;

import javax.ws.rs.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

/**
 * A sub-resource locator.  Invoked on a JAX-RS resource in order to locate a subresource.
 *
 * @author Ryan Heaton
 */
public class SubResourceLocator extends DecoratedMethodDeclaration {

  private final Path path;
  private final SubResource resource;
  private final Resource parent;
  private final List<ResourceParameter> resourceParameters;
  private final ParameterDeclaration entityParameter;

  public SubResourceLocator(MethodDeclaration delegate, Resource parent) {
    super(delegate);
    this.parent = parent;

    this.path = delegate.getAnnotation(Path.class);
    if (this.path == null) {
      throw new IllegalArgumentException("A subresource locator must specify a path with the @javax.ws.rs.Path annotation.");
    }

    SubResource resource;
    TypeMirror returnType = delegate.getReturnType();
    if ((returnType instanceof DeclaredType) && ((DeclaredType) returnType).getDeclaration() != null) {
      TypeDeclaration declaration = ((DeclaredType) returnType).getDeclaration();
      resource = findRecursiveSubResource(declaration, getPath());
      resource = resource == null ? new SubResource(declaration, getPath(), this) : resource;
    }
    else {
      resource = new SubResource(Context.getCurrentEnvironment().getTypeDeclaration(Object.class.getName()), getPath(), this);
    }
    this.resource = resource;

    ParameterDeclaration entityParameter = null;
    List<ResourceParameter> resourceParameters = new ArrayList<ResourceParameter>();
    for (ParameterDeclaration parameterDeclaration : delegate.getParameters()) {
      if (ResourceParameter.isResourceParameter(parameterDeclaration)) {
        resourceParameters.add(new ResourceParameter(parameterDeclaration));
      }
      else {
        entityParameter = parameterDeclaration;
      }
    }

    this.entityParameter = entityParameter;
    this.resourceParameters = resourceParameters;
  }

  //fix for ENUNCIATE-574
  private SubResource findRecursiveSubResource(TypeDeclaration declaration, String path) {
    LinkedList<SubResource> ancestorResources = SubResource.ANCESTOR_DECLARATIONS.get();
    for (SubResource ancestorResource : ancestorResources) {
      if (ancestorResource.getQualifiedName().equals(declaration.getQualifiedName()) && ancestorResource.getPath().equals(path)) {
        return ancestorResource;
      }
    }
    return null;
  }

  /**
   * The path of the subresource.
   *
   * @return The path of the subresource.
   */
  public String getPath() {
    return this.path.value();
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
  public List<ResourceParameter> getResourceParameters() {
    ArrayList<ResourceParameter> resourceParams = new ArrayList<ResourceParameter>(this.resourceParameters);
    resourceParams.addAll(getParent().getResourceParameters());
    return resourceParams;
  }

  /**
   * The entity parameter.
   *
   * @return The entity parameter, or null if none.
   */
  public ParameterDeclaration getEntityParameter() {
    return entityParameter;
  }

}
