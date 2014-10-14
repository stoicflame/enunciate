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

import com.sun.mirror.declaration.TypeDeclaration;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

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

  public SubResource(TypeDeclaration delegate, String path, SubResourceLocator locator) {
    super(delegate, path);
    this.locator = locator;
  }

  @Override
  public List<ResourceParameter> getResourceParameters() {
    ArrayList<ResourceParameter> params = new ArrayList<ResourceParameter>(super.getResourceParameters());
    params.addAll(getLocator().getResourceParameters());
    return params;
  }

  @Override
  protected List<SubResourceLocator> getSubresourceLocators(TypeDeclaration delegate) {
    if (delegate.getQualifiedName().equals(getQualifiedName())) {
      ANCESTOR_DECLARATIONS.get().addFirst(this);
      try {
        return super.getSubresourceLocators(delegate);
      }
      finally {
        ANCESTOR_DECLARATIONS.get().removeFirst();
      }
    }
    else {
      return super.getSubresourceLocators(delegate);
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
