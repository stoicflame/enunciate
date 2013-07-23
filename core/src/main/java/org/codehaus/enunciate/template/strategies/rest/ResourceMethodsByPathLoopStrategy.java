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

package org.codehaus.enunciate.template.strategies.rest;

import net.sf.jelly.apt.TemplateException;
import net.sf.jelly.apt.TemplateModel;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import org.codehaus.enunciate.util.FacetFilter;
import org.codehaus.enunciate.util.ResourceMethodPathComparator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * Strategy for looping through all sets of resource methods, grouped by path.
 *
 * @author Ryan Heaton
 */
public class ResourceMethodsByPathLoopStrategy extends EnunciateTemplateLoopStrategy<List<ResourceMethod>> {

  private boolean considerFacets = false;
  private String var = "resources";

  protected Iterator<List<ResourceMethod>> getLoop(TemplateModel model) throws TemplateException {
    TreeMap<String, List<ResourceMethod>> resourcesByPath = new TreeMap<String, List<ResourceMethod>>(new ResourceMethodPathComparator());

    for (RootResource rootResource : getModel().getRootResources()) {
      for (ResourceMethod resource : rootResource.getResourceMethods(true)) {
        if (considerFacets && !FacetFilter.accept(resource)) {
          continue;
        }

        String path = resource.getFullpath();
        List<ResourceMethod> resourceList = resourcesByPath.get(path);
        if (resourceList == null) {
          resourceList = new ArrayList<ResourceMethod>();
          resourcesByPath.put(path, resourceList);
        }

        resourceList.add(resource);
      }
    }

    return resourcesByPath.values().iterator();
  }

  @Override
  protected void setupModelForLoop(TemplateModel model, List<ResourceMethod> resources, int index) throws TemplateException {
    super.setupModelForLoop(model, resources, index);

    if (this.var != null) {
      getModel().setVariable(var, resources);
    }
  }

  /**
   * The variable into which to store the current REST endpoint.
   *
   * @return The variable into which to store the current REST endpoint.
   */
  public String getVar() {
    return var;
  }

  /**
   * The variable into which to store the current REST endpoint.
   *
   * @param var The variable into which to store the current REST endpoint.
   */
  public void setVar(String var) {
    this.var = var;
  }

  public boolean isConsiderFacets() {
    return considerFacets;
  }

  public void setConsiderFacets(boolean considerFacets) {
    this.considerFacets = considerFacets;
  }
}