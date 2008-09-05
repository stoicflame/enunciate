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

import java.util.List;
import java.util.ArrayList;

/**
 * A JAX-RS sub-resource.
 *
 * @author Ryan Heaton
 */
public class SubResource extends Resource {

  private final String path;
  private final SubResourceLocator locator;

  public SubResource(TypeDeclaration delegate, String path, SubResourceLocator locator) {
    super(delegate);
    this.path = path;
    this.locator = locator;
  }

  @Override
  public List<ResourceParameter> getResourceParameters() {
    ArrayList<ResourceParameter> params = new ArrayList<ResourceParameter>(super.getResourceParameters());
    params.addAll(getLocator().getResourceParameters());
    return params;
  }


  /**
   * The path to this subresource.
   *
   * @return The path to this subresource.
   */
  public String getPath() {
    return path;
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
