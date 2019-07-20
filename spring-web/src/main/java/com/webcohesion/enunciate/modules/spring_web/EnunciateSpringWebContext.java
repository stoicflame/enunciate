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
package com.webcohesion.enunciate.modules.spring_web;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.TypeElementComparator;
import com.webcohesion.enunciate.module.EnunciateModuleContext;
import com.webcohesion.enunciate.modules.spring_web.api.impl.AnnotationBasedResourceGroupImpl;
import com.webcohesion.enunciate.modules.spring_web.api.impl.PathBasedResourceGroupImpl;
import com.webcohesion.enunciate.modules.spring_web.api.impl.ResourceClassResourceGroupImpl;
import com.webcohesion.enunciate.modules.spring_web.api.impl.ResourceImpl;
import com.webcohesion.enunciate.modules.spring_web.model.RequestMapping;
import com.webcohesion.enunciate.modules.spring_web.model.SpringController;
import com.webcohesion.enunciate.modules.spring_web.model.SpringControllerAdvice;
import com.webcohesion.enunciate.util.PathSortStrategy;
import com.webcohesion.enunciate.util.ResourceComparator;
import com.webcohesion.enunciate.util.ResourceGroupComparator;
import com.webcohesion.enunciate.util.SortedList;

import java.util.*;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateSpringWebContext extends EnunciateModuleContext {

  public enum GroupingStrategy {
    path,
    annotation,
    resource_class
  }

  private final Set<SpringController> controllers;
  private final Set<SpringControllerAdvice> advice;
  private String relativeContextPath = "";
  private GroupingStrategy groupingStrategy = GroupingStrategy.resource_class;
  private InterfaceDescriptionFile wadlFile = null;
  private PathSortStrategy pathSortStrategy = PathSortStrategy.breadth_first;
  private final boolean disableExamples;

  public EnunciateSpringWebContext(EnunciateContext context, boolean disableExamples) {
    super(context);
    this.disableExamples = disableExamples;
    this.controllers = new TreeSet<SpringController>(new TypeElementComparator());
    this.advice = new TreeSet<SpringControllerAdvice>(new TypeElementComparator());
  }

  public EnunciateContext getContext() {
    return context;
  }

  public Set<SpringController> getControllers() {
    return controllers;
  }

  public Set<SpringControllerAdvice> getAdvice() {
    return advice;
  }

  public void add(SpringController controller) {
    this.controllers.add(controller);
    debug("Added %s as a Spring controller.", controller.getQualifiedName());
  }

  public void add(SpringControllerAdvice advice) {
    this.advice.add(advice);
    debug("Added %s as Spring controller advice.", advice.getQualifiedName());
  }

  public boolean isIncludeResourceGroupName() {
    return this.groupingStrategy != GroupingStrategy.path;
  }

  public boolean isDisableExamples() {
    return disableExamples;
  }

  public void setRelativeContextPath(String relativeContextPath) {
    this.relativeContextPath = relativeContextPath;
  }

  public void setGroupingStrategy(GroupingStrategy groupingStrategy) {
    this.groupingStrategy = groupingStrategy;
  }

  public PathSortStrategy getPathSortStrategy() {
    return pathSortStrategy;
  }

  public void setPathSortStrategy(PathSortStrategy pathSortStrategy) {
    this.pathSortStrategy = pathSortStrategy;
  }

  public InterfaceDescriptionFile getWadlFile() {
    return wadlFile;
  }

  public void setWadlFile(InterfaceDescriptionFile wadlFile) {
    this.wadlFile = wadlFile;
  }

  public List<ResourceGroup> getResourceGroups(ApiRegistrationContext registrationContext) {
    List<ResourceGroup> resourceGroups;
    if (this.groupingStrategy == GroupingStrategy.path) {
      //group resources by path.
      resourceGroups = getResourceGroupsByPath(registrationContext);
    }
    else if (this.groupingStrategy == GroupingStrategy.annotation) {
      resourceGroups = getResourceGroupsByAnnotation(registrationContext);
    }
    else {
      resourceGroups = getResourceGroupsByClass(registrationContext);
    }

    Collections.sort(resourceGroups, new Comparator<ResourceGroup>() {
      @Override
      public int compare(ResourceGroup o1, ResourceGroup o2) {
        return o1.getLabel().compareTo(o2.getLabel());
      }
    });
    return resourceGroups;
  }

  public List<ResourceGroup> getResourceGroupsByClass(ApiRegistrationContext registrationContext) {
    List<ResourceGroup> resourceGroups = new ArrayList<ResourceGroup>();
    Set<String> slugs = new TreeSet<String>();
    FacetFilter facetFilter = registrationContext.getFacetFilter();
    for (SpringController springController : controllers) {
      if (!facetFilter.accept(springController)) {
        continue;
      }

      String slug = springController.getSimpleName().toString();
      if (slugs.contains(slug)) {
        slug = "";
        String[] qualifiedNameTokens = springController.getQualifiedName().toString().split("\\.");
        for (int i = qualifiedNameTokens.length - 1; i >= 0; i--) {
          slug = slug.isEmpty() ? qualifiedNameTokens[i] : slug + "_" + qualifiedNameTokens[i];
          if (!slugs.contains(slug)) {
            break;
          }
        }
      }
      slugs.add(slug);

      ResourceGroup group = new ResourceClassResourceGroupImpl(springController, slug, relativeContextPath, registrationContext);

      if (!group.getResources().isEmpty()) {
        resourceGroups.add(group);
      }
    }

    Collections.sort(resourceGroups, new ResourceGroupComparator(this.pathSortStrategy));

    return resourceGroups;
  }

  public List<ResourceGroup> getResourceGroupsByPath(ApiRegistrationContext registrationContext) {
    Map<String, PathBasedResourceGroupImpl> resourcesByPath = new HashMap<String, PathBasedResourceGroupImpl>();

    FacetFilter facetFilter = registrationContext.getFacetFilter();
    for (SpringController springController : controllers) {
      if (!facetFilter.accept(springController)) {
        continue;
      }

      for (RequestMapping method : springController.getRequestMappings()) {
        if (facetFilter.accept(method)) {
          String path = method.getFullpath();
          PathBasedResourceGroupImpl resourceGroup = resourcesByPath.get(path);
          if (resourceGroup == null) {
            resourceGroup = new PathBasedResourceGroupImpl(relativeContextPath, path, new ArrayList<Resource>());
            resourcesByPath.put(path, resourceGroup);
          }

          resourceGroup.getResources().add(new ResourceImpl(method, resourceGroup, registrationContext));
        }
      }
    }

    ArrayList<ResourceGroup> resourceGroups = new ArrayList<ResourceGroup>(resourcesByPath.values());
    Collections.sort(resourceGroups, new ResourceGroupComparator(this.pathSortStrategy));
    return resourceGroups;
  }

  public List<ResourceGroup> getResourceGroupsByAnnotation(ApiRegistrationContext registrationContext) {
    Map<String, AnnotationBasedResourceGroupImpl> resourcesByAnnotation = new HashMap<String, AnnotationBasedResourceGroupImpl>();

    FacetFilter facetFilter = registrationContext.getFacetFilter();
    for (SpringController springController : controllers) {
      if (!facetFilter.accept(springController)) {
        continue;
      }

      com.webcohesion.enunciate.metadata.rs.ResourceGroup controllerAnnotation = null;
      boolean controllerAnnotationEvaluated = false;
      for (RequestMapping method : springController.getRequestMappings()) {
        if (facetFilter.accept(method)) {
          com.webcohesion.enunciate.metadata.rs.ResourceGroup annotation = method.getAnnotation(com.webcohesion.enunciate.metadata.rs.ResourceGroup.class);
          if (annotation == null) {
            if (!controllerAnnotationEvaluated) {
              controllerAnnotation = springController.getAnnotation(com.webcohesion.enunciate.metadata.rs.ResourceGroup.class);
              controllerAnnotationEvaluated = true;
            }
            annotation = controllerAnnotation;
          }
          String label = annotation == null ? "Other" : annotation.value();
          String description = annotation == null ? null : annotation.description();
          if ("##default".equals(description)) {
            description = null;
          }
          AnnotationBasedResourceGroupImpl resourceGroup = resourcesByAnnotation.get(label);
          if (resourceGroup == null) {
            resourceGroup = new AnnotationBasedResourceGroupImpl(relativeContextPath, label, new SortedList<Resource>(new ResourceComparator(this.pathSortStrategy)), this.pathSortStrategy);
            resourcesByAnnotation.put(label, resourceGroup);
          }
          resourceGroup.setDescriptionIfNull(description);

          resourceGroup.getResources().add(new ResourceImpl(method, resourceGroup, registrationContext));
        }
      }
    }

    ArrayList<ResourceGroup> resourceGroups = new ArrayList<ResourceGroup>(resourcesByAnnotation.values());
    Collections.sort(resourceGroups, new ResourceGroupComparator(this.pathSortStrategy));
    return resourceGroups;
  }
}
