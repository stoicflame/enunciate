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

import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;

import javax.lang.model.element.TypeElement;
import java.util.*;

/**
 * A JAX-RS sub-resource.
 *
 * @author Ryan Heaton
 */
public class SubResource extends Resource {

  static ThreadLocal<LinkedList<SubResource>> ANCESTOR_DECLARATIONS = new ThreadLocal<LinkedList<SubResource>>() {
    @Override
    protected LinkedList<SubResource> initialValue() {
      return new LinkedList<SubResource>();
    }
  };

  private final SubResourceLocator locator;

  public SubResource(TypeElement delegate, String path, SubResourceLocator locator, TypeVariableContext variableContext, EnunciateJaxrsContext context) {
    super(delegate, path, variableContext, context);
    this.locator = locator;
  }

  @Override
  public Set<ResourceParameter> getResourceParameters() {
    TreeSet<ResourceParameter> params = new TreeSet<ResourceParameter>(super.getResourceParameters());
    params.addAll(getLocator().getResourceParameters());
    return params;
  }

  @Override
  protected List<SubResourceLocator> getSubresourceLocators(TypeElement delegate, TypeVariableContext variableContext, EnunciateJaxrsContext context) {
    if (delegate.getQualifiedName().equals(getQualifiedName())) {
      ANCESTOR_DECLARATIONS.get().addFirst(this);
      try {
        return super.getSubresourceLocators(delegate, variableContext, context);
      }
      finally {
        ANCESTOR_DECLARATIONS.get().removeFirst();
      }
    }
    else {
      return super.getSubresourceLocators(delegate, variableContext, context);
    }
  }

  /**
   * The subresource locator.
   *
   * @return The subresource locator.
   */
  public SubResourceLocator getLocator() {
    return locator;
  }

  /**
   * The parent resource.
   *
   * @return The parent resource.
   */
  public Resource getParent() {
    return getLocator().getParent();
  }

}
