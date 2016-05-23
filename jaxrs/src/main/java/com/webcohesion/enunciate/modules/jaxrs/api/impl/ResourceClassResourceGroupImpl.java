package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.PathSummary;
import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.Label;
import com.webcohesion.enunciate.metadata.rs.ResourceLabel;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;
import com.webcohesion.enunciate.util.PathSummaryComparator;

import javax.lang.model.element.AnnotationMirror;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class ResourceClassResourceGroupImpl implements ResourceGroup {

  private final com.webcohesion.enunciate.modules.jaxrs.model.Resource resourceClass;
  private final List<Resource> resources = new ArrayList<Resource>();
  private final String contextPath;
  private final String slug;

  public ResourceClassResourceGroupImpl(com.webcohesion.enunciate.modules.jaxrs.model.Resource resourceClass, String slug, String contextPath) {
    this.resourceClass = resourceClass;
    this.contextPath = contextPath;
    this.slug = slug;
    FacetFilter facetFilter = resourceClass.getContext().getContext().getConfiguration().getFacetFilter();
    for (ResourceMethod resourceMethod : resourceClass.getResourceMethods(true)) {
      if (!facetFilter.accept(resourceMethod)) {
        continue;
      }

      this.resources.add(new ResourceImpl(resourceMethod, this));
    }
  }

  @Override
  public String getSlug() {
    return "resource_" + this.slug;
  }

  @Override
  public String getLabel() {
    String label = this.resourceClass.getSimpleName().toString();
    ResourceLabel resourceLabel = resourceClass.getAnnotation(ResourceLabel.class);
    if (resourceLabel != null && !"##default".equals(resourceLabel.value())) {
      label = resourceLabel.value();
    }

    Label generic = this.resourceClass.getAnnotation(Label.class);
    if (generic != null) {
      label = generic.value();
    }

    JavaDoc.JavaDocTagList tags = this.resourceClass.getJavaDoc().get("label");
    if (tags != null && tags.size() > 0) {
      String tag = tags.get(0).trim();
      label = tag.isEmpty() ? label : tag;
    }

    return label;
  }

  @Override
  public String getSortKey() {
    String sortKey = getLabel();
    ResourceLabel resourceLabel = resourceClass.getAnnotation(ResourceLabel.class);
    if (resourceLabel != null && !"##default".equals(resourceLabel.sortKey())) {
      sortKey = resourceLabel.sortKey();
    }
    return sortKey;
  }

  @Override
  public String getRelativeContextPath() {
    return this.contextPath;
  }

  @Override
  public String getDescription() {
    return resourceClass.getJavaDoc().toString();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.resourceClass);
  }

  @Override
  public List<PathSummary> getPaths() {
    HashMap<String, PathSummary> summaries = new HashMap<String, PathSummary>();
    for (Resource resource : this.resources) {
      Set<String> methods = new TreeSet<String>();
      for (Method method : resource.getMethods()) {
        methods.add(method.getHttpMethod());
      }

      PathSummary summary = summaries.get(resource.getPath());
      if (summary == null) {
        summary = new PathSummaryImpl(resource.getPath(), methods, resource.getStyles());
        summaries.put(resource.getPath(), summary);
      }
      else {
        summary.getMethods().addAll(methods);
      }
    }
    ArrayList<PathSummary> pathSummaries = new ArrayList<PathSummary>(summaries.values());
    Collections.sort(pathSummaries, new PathSummaryComparator(resourceClass.getContext().getPathSortStrategy()));
    return pathSummaries;
  }

  @Override
  public List<Resource> getResources() {
    return this.resources;
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.resourceClass.getAnnotations();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.resourceClass.getJavaDoc();
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.resourceClass, this.resourceClass.getContext().getContext().getConfiguration().getAnnotationStyles());
  }
}
