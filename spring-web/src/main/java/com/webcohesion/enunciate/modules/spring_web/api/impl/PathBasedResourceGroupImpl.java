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
package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.PathSummary;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.javac.TypeElementComparator;
import com.webcohesion.enunciate.javac.javadoc.DefaultJavaDocTagHandler;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.spring_web.model.SpringController;

import javax.lang.model.element.AnnotationMirror;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class PathBasedResourceGroupImpl implements ResourceGroup {

  private final String contextPath;
  private final String path;
  private final List<Resource> resources;

  public PathBasedResourceGroupImpl(String contextPath, String path, List<Resource> resources) {
    this.contextPath = contextPath;
    this.path = path;
    this.resources = resources;
  }

  @Override
  public String getSlug() {
    return "resource_" + scrubPathForSlug(path);
  }

  @Override
  public String getRelativeContextPath() {
    return this.contextPath;
  }

  @Override
  public String getLabel() {
    return this.path;
  }

  @Override
  public String getSortKey() {
    return this.path;
  }

  @Override
  public String getDescription() {
    //we'll return a description if all descriptions of all methods are the same, or if there's only one defining controller.
    String description = null;
    Set<SpringController> definingResourceClasses = new TreeSet<SpringController>(new TypeElementComparator());
    for (Resource resource : this.resources) {
      for (Method method : resource.getMethods()) {
        if (description != null && method.getDescription() != null && !description.equals(method.getDescription())){
          return null;
        }

        description = method.getDescription();
        if (description != null && description.trim().isEmpty()) {
          description = null;
        }
      }

      if (resource instanceof ResourceImpl) {
        definingResourceClasses.add(((ResourceImpl) resource).requestMapping.getParent());
      }
    }

    if (description == null && definingResourceClasses.size() == 1) {
      description = definingResourceClasses.iterator().next().getDocValue();
    }

    return description;
  }

  @Override
  public String getDeprecated() {
    String deprecated = null;
    for (Resource resource : this.resources) {
      deprecated = resource.getDeprecated();
      if (deprecated == null) {
        //if _any_ resources are not deprecated, this resource group isn't deprecated either.
        return null;
      }
    }
    return deprecated;
  }

  @Override
  public List<PathSummary> getPaths() {
    Set<String> methods = new TreeSet<String>();
    Set<String> styles = new TreeSet<String>();
    for (Resource resource : this.resources) {
      for (Method method : resource.getMethods()) {
        methods.add(method.getHttpMethod());
        styles.addAll(method.getStyles());
      }
    }
    return Arrays.asList((PathSummary) new PathSummaryImpl(this.path, methods, styles));
  }

  @Override
  public List<Resource> getResources() {
    return this.resources;
  }

  private static String scrubPathForSlug(String facetValue) {
    return facetValue.replace('/', '_').replace(':', '_').replace('{', '_').replace('}', '_');
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return Collections.emptyMap();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return new JavaDoc(null, new DefaultJavaDocTagHandler());
  }

  @Override
  public Set<String> getStyles() {
    TreeSet<String> styles = new TreeSet<String>();
    for (Resource resource : this.resources) {
      styles.addAll(resource.getStyles());
    }
    return styles;
  }
}
