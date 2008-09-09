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
import org.codehaus.enunciate.contract.common.rest.RESTResource;
import org.codehaus.enunciate.contract.jaxrs.ResourceMethod;
import org.codehaus.enunciate.contract.jaxrs.RootResource;
import org.codehaus.enunciate.contract.rest.RESTEndpoint;
import org.codehaus.enunciate.contract.rest.RESTMethod;
import org.codehaus.enunciate.template.strategies.EnunciateTemplateLoopStrategy;
import org.codehaus.enunciate.util.RESTResourcePathComparator;

import java.util.*;

/**
 * Strategy for looping through each REST resource.
 *
 * @author Ryan Heaton
 */
public class RESTResourcesByPathLoopStrategy extends EnunciateTemplateLoopStrategy<List<RESTResource>> {

  private String var = "resources";

  protected Iterator<List<RESTResource>> getLoop(TemplateModel model) throws TemplateException {
    TreeMap<String, List<RESTResource>> resourcesByPath = new TreeMap<String, List<RESTResource>>(new RESTResourcePathComparator());
    for (RESTEndpoint endpoint : getRESTEndpoints()) {
      for (RESTMethod resource : endpoint.getRESTMethods()) {
        String path = resource.getPath();
        List<RESTResource> resourceList = resourcesByPath.get(path);
        if (resourceList == null) {
          resourceList = new ArrayList<RESTResource>();
          resourcesByPath.put(path, resourceList);
        }

        resourceList.add(resource);
      }
    }

    for (RootResource rootResource : getModel().getRootResources()) {
      for (ResourceMethod resource : rootResource.getResourceMethods(true)) {
        String path = resource.getPath();
        List<RESTResource> resourceList = resourcesByPath.get(path);
        if (resourceList == null) {
          resourceList = new ArrayList<RESTResource>();
          resourcesByPath.put(path, resourceList);
        }

        resourceList.add(resource);
      }
    }

    return resourcesByPath.values().iterator();
  }

  @Override
  protected void setupModelForLoop(TemplateModel model, List<RESTResource> resources, int index) throws TemplateException {
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
}