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
package com.webcohesion.enunciate.api;

import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.services.ServiceApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Ryan Heaton
 */
public class ApiRegistry {

  private final List<ServiceApi> serviceApis = new ArrayList<ServiceApi>();
  private final List<ResourceApi> resourceApis = new ArrayList<ResourceApi>();
  private final Set<Syntax> syntaxes = new TreeSet<Syntax>();
  private InterfaceDescriptionFile swaggerUI;

  public List<ServiceApi> getServiceApis() {
    return serviceApis;
  }

  public List<ResourceApi> getResourceApis() {
    return resourceApis;
  }

  public Set<Syntax> getSyntaxes() {
    return syntaxes;
  }

  public InterfaceDescriptionFile getSwaggerUI() {
    return swaggerUI;
  }

  public void setSwaggerUI(InterfaceDescriptionFile swaggerUI) {
    this.swaggerUI = swaggerUI;
  }
}
