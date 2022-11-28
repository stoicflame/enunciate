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
package com.webcohesion.enunciate.modules.jaxrs;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.TypeElementComparator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.module.EnunciateModuleContext;
import com.webcohesion.enunciate.modules.jaxrs.api.impl.AnnotationBasedResourceGroupImpl;
import com.webcohesion.enunciate.modules.jaxrs.api.impl.PathBasedResourceGroupImpl;
import com.webcohesion.enunciate.modules.jaxrs.api.impl.ResourceClassResourceGroupImpl;
import com.webcohesion.enunciate.modules.jaxrs.api.impl.ResourceImpl;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;
import com.webcohesion.enunciate.modules.jaxrs.model.RootResource;
import com.webcohesion.enunciate.modules.jaxrs.model.util.JaxrsUtil;
import com.webcohesion.enunciate.util.*;
import org.apache.commons.configuration.HierarchicalConfiguration;

import javax.lang.model.element.TypeElement;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJaxrsContext extends EnunciateModuleContext {

  public enum GroupingStrategy {
    path,
    annotation,
    resource_class
  }

  private final Map<String, String> mediaTypeIds;
  private final Set<RootResource> rootResources;
  private final Set<TypeElement> providers;
  private final Set<String> customResourceParameterAnnotations;
  private final Set<String> systemResourceParameterAnnotations;
  private String relativeContextPath = "";
  private GroupingStrategy groupingStrategy = GroupingStrategy.resource_class;
  private PathSortStrategy pathSortStrategy = PathSortStrategy.breadth_first;
  private InterfaceDescriptionFile wadlFile = null;
  private final boolean disableExamples;

  public EnunciateJaxrsContext(EnunciateContext context, boolean disableExamples) {
    super(context);
    this.disableExamples = disableExamples;
    this.mediaTypeIds = loadKnownMediaTypes();
    this.rootResources = new TreeSet<RootResource>(new RootResourceComparator());
    this.providers = new TreeSet<TypeElement>(new TypeElementComparator());
    this.customResourceParameterAnnotations = loadKnownCustomResourceParameterAnnotations(context);
    this.systemResourceParameterAnnotations = loadKnownSystemResourceParameterAnnotations(context);
  }

  protected Map<String, String> loadKnownMediaTypes() {
    HashMap<String, String> mediaTypes = new HashMap<String, String>();
    mediaTypes.put("application/atom+xml", "atom");
    mediaTypes.put("application/x-www-form-urlencoded", "form");
    mediaTypes.put("application/json", "json");
    mediaTypes.put("application/octet-stream", "bin");
    mediaTypes.put("application/svg+xml", "svg");
    mediaTypes.put("application/xhtml+xml", "xhtml");
    mediaTypes.put("application/xml", "xml");
    mediaTypes.put("multipart/form-data", "multipart");
    mediaTypes.put("text/html", "html");
    mediaTypes.put("text/plain", "text");
    return mediaTypes;
  }

  protected Set<String> loadKnownCustomResourceParameterAnnotations(EnunciateContext context) {
    TreeSet<String> customResourceParameterAnnotations = new TreeSet<String>();

    //Jersey 1
    customResourceParameterAnnotations.add("com.sun.jersey.multipart.FormDataParam");

    //Jersey 2
    customResourceParameterAnnotations.add("org.glassfish.jersey.media.multipart.FormDataParam");

    //CXF
    customResourceParameterAnnotations.add("org.apache.cxf.jaxrs.ext.multipart.Multipart");

    //RESTEasy
    //(none?)

    //load the configured ones.
    List<HierarchicalConfiguration> configuredParameterAnnotations = context.getConfiguration().getSource().configurationsAt("modules.jaxrs.custom-resource-parameter-annotation");
    for (HierarchicalConfiguration configuredParameterAnnotation : configuredParameterAnnotations) {
      String fqn = configuredParameterAnnotation.getString("[@qualifiedName]", null);
      if (fqn != null) {
        customResourceParameterAnnotations.add(fqn);
      }
    }

    return customResourceParameterAnnotations;
  }

  protected Set<String> loadKnownSystemResourceParameterAnnotations(EnunciateContext context) {
    TreeSet<String> systemResourceParameterAnnotations = new TreeSet<String>();

    //JDK
    systemResourceParameterAnnotations.add("javax.inject.Inject");

    //Jersey
    systemResourceParameterAnnotations.add("com.sun.jersey.api.core.InjectParam");

    //CXF
    //(none?)

    //RESTEasy
    //(none?)

    //Spring
    systemResourceParameterAnnotations.add("org.springframework.beans.factory.annotation.Autowired");

    //load the configured ones.
    List<HierarchicalConfiguration> configuredSystemAnnotations = context.getConfiguration().getSource().configurationsAt("modules.jaxrs.custom-system-parameter-annotation");
    for (HierarchicalConfiguration configuredSystemAnnotation : configuredSystemAnnotations) {
      String fqn = configuredSystemAnnotation.getString("[@qualifiedName]", null);
      if (fqn != null) {
        systemResourceParameterAnnotations.add(fqn);
      }
    }

    return systemResourceParameterAnnotations;
  }

  public EnunciateContext getContext() {
    return context;
  }

  public Map<String, String> getMediaTypeIds() {
    //todo: configure media type ids?
    return mediaTypeIds;
  }

  public boolean isDisableExamples() {
    return disableExamples;
  }

  /**
   * Add a content type.
   *
   * @param mediaType The content type to add.
   */
  public void addMediaType(com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType mediaType) {
    if (!mediaTypeIds.containsKey(mediaType.getMediaType())) {
      String id = getDefaultContentTypeId(mediaType.getMediaType());
      if (id != null) {
        mediaTypeIds.put(mediaType.getMediaType(), id);
      }
    }
  }

  /**
   * Get the default content type id for the specified content type.
   *
   * @param contentType The content type.
   * @return The default content type id, or null if the content type is a wildcard type.
   */
  protected String getDefaultContentTypeId(String contentType) {
    String id = contentType;
    if (id.endsWith("/")) {
      throw new IllegalArgumentException("Illegal content type: " + id);
    }

    int semiColon = id.indexOf(';');
    if (semiColon > -1) {
      id = id.substring(0, semiColon);
    }

    int lastSlash = id.lastIndexOf('/');
    if (lastSlash > -1) {
      id = id.substring(lastSlash + 1);
    }

    int plus = id.indexOf('+');
    if (plus > -1) {
      id = id.substring(0, plus);
    }

    if (id.contains("*")) {
      //wildcard types have no ids.
      return null;
    }
    else {
      return id;
    }
  }

  public Set<RootResource> getRootResources() {
    return rootResources;
  }

  public Set<TypeElement> getProviders() {
    return providers;
  }

  public Set<String> getCustomResourceParameterAnnotations() {
    return this.customResourceParameterAnnotations;
  }

  public Set<String> getSystemResourceParameterAnnotations() {
    return this.systemResourceParameterAnnotations;
  }


  /**
   * Add a root resource to the model.
   *
   * @param rootResource The root resource to add to the model.
   */
  public void add(RootResource rootResource) {
    if (rootResource.isInterface()) {
      //if the root resource is an interface, don't add it if its implementation has already been added (avoid duplication).
      for (RootResource resource : this.rootResources) {
        if (((DecoratedTypeMirror)(resource.asType())).isInstanceOf(rootResource)) {
          debug("%s was identified as a JAX-RS root resource, but will be ignored because root resource %s implements it.", rootResource.getQualifiedName(), resource.getQualifiedName());
          return;
        }
      }
    }
    else {
      //remove any interfaces of this root resource that have been identified as root resources (avoid duplication)
      DecoratedTypeMirror rootResourceType = (DecoratedTypeMirror) rootResource.asType();
      Iterator<RootResource> it = this.rootResources.iterator();
      while (it.hasNext()) {
        RootResource resource = it.next();
        if (resource.isInterface() && rootResourceType.isInstanceOf(resource)) {
          debug("%s was identified as a JAX-RS root resource, but will be ignored because root resource %s implements it.", resource.getQualifiedName(), rootResource.getQualifiedName());
          it.remove();
        }
      }
    }

    this.rootResources.add(rootResource);
    debug("Added %s as a JAX-RS root resource.", rootResource.getQualifiedName());

    if (getContext().getProcessingEnvironment().findSourcePosition(rootResource) == null) {
      OneTimeLogMessage.SOURCE_FILES_NOT_FOUND.log(getContext());
      if (OneTimeLogMessage.SOURCE_FILES_NOT_FOUND.getLogged() <= 3) {
        info("Unable to find source file for %s.", rootResource.getQualifiedName());
      }
      else {
        debug("Unable to find source file for %s.", rootResource.getQualifiedName());
      }
    }
  }

  /**
   * Add a JAX-RS provider to the model.
   *
   * @param declaration The declaration of the provider.
   */
  public void addJAXRSProvider(TypeElement declaration) {
    this.providers.add(declaration);
    debug("Added %s as a JAX-RS provider.", declaration.getQualifiedName());

    javax.ws.rs.Produces produces = declaration.getAnnotation(javax.ws.rs.Produces.class);
    jakarta.ws.rs.Produces produces2 = declaration.getAnnotation(jakarta.ws.rs.Produces.class);
    if(produces != null || produces2 != null){
        processMediaType(produces != null ? produces.value() : produces2.value());
    }
    javax.ws.rs.Consumes consumes = declaration.getAnnotation(javax.ws.rs.Consumes.class);
    jakarta.ws.rs.Consumes consumes2 = declaration.getAnnotation(jakarta.ws.rs.Consumes.class);
    if(consumes != null || consumes2 != null){
        processMediaType(consumes != null ? consumes.value() : consumes2.value());
    }
    
  }
  
  private void processMediaType(String[] values) {
      JaxrsUtil.value(values).forEach(e -> addMediaType(e));
  }

  public boolean isIncludeResourceGroupName() {
    return this.groupingStrategy != GroupingStrategy.path;
  }

  public String getRelativeContextPath() {
    return relativeContextPath;
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
    for (RootResource rootResource : rootResources) {
      // NOTE: do not call facetFilter.accept(rootResource) to check if evaluating resourceMethod's in the rootResource can be skipped
      // - If an "included" facet is defined, and not applied to the Resource; then checking facetFilter.accept(rootResource) will
      //   return false even if the included facet is applied to a method inside the rootResource.
      // - ResourceMethods inherit all the facets applied directly to the rootResource, so skipping the check on the rootResource won't cause
      //   problems with included/excluded facets that are applied to the rootResource
      if (rootResource.getResourceMethods().stream().noneMatch(facetFilter::accept)) {
        continue;
      }

      String slug = rootResource.getSimpleName().toString();
      if (slugs.contains(slug)) {
        slug = "";
        String[] qualifiedNameTokens = rootResource.getQualifiedName().toString().split("\\.");
        for (int i = qualifiedNameTokens.length - 1; i >= 0; i--) {
          slug = slug.isEmpty() ? qualifiedNameTokens[i] : slug + "_" + qualifiedNameTokens[i];
          if (!slugs.contains(slug)) {
            break;
          }
        }
      }
      slugs.add(slug);

      com.webcohesion.enunciate.metadata.rs.ServiceContextRoot context = rootResource.getAnnotation(com.webcohesion.enunciate.metadata.rs.ServiceContextRoot.class);
      com.webcohesion.enunciate.modules.jaxrs.model.Resource resource = rootResource.getParent();
      while (context == null && resource != null) {
        context = resource.getAnnotation(com.webcohesion.enunciate.metadata.rs.ServiceContextRoot.class);
        resource = resource.getParent();
      }

      String contextPath = context != null ? JaxrsModule.sanitizeContextPath(context.value()) : this.relativeContextPath;
      ResourceGroup group = new ResourceClassResourceGroupImpl(rootResource, slug, contextPath, registrationContext);

      if (!group.getResources().isEmpty()) {
        // group.getResources() should never be empty since we did a getResourceMethods().stream().noneMatch() pre-check at the start of the loop
        resourceGroups.add(group);
      }
    }

    Collections.sort(resourceGroups, new ResourceGroupComparator(this.pathSortStrategy));

    return resourceGroups;
  }

  public List<ResourceGroup> getResourceGroupsByPath(ApiRegistrationContext registrationContext) {
    Map<String, PathBasedResourceGroupImpl> resourcesByPath = new HashMap<String, PathBasedResourceGroupImpl>();

    FacetFilter facetFilter = registrationContext.getFacetFilter();
    for (RootResource rootResource : rootResources) {
      // NOTE: do not call facetFilter.accept(rootResource) to check if evaluating resourceMethod's in the rootResource can be skipped
      // - If an "included" facet is defined, and not applied to the Resource; then checking facetFilter.accept(rootResource) will
      //   return false even if the included facet is applied to a method inside the rootResource.
      // - ResourceMethods inherit all the facets applied directly to the rootResource, so skipping the check on the rootResource won't cause
      //   problems with included/excluded facets that are applied to the rootResource

      for (ResourceMethod method : rootResource.getResourceMethods(true)) {
        if (facetFilter.accept(method)) {
          com.webcohesion.enunciate.metadata.rs.ServiceContextRoot context = method.getAnnotation(com.webcohesion.enunciate.metadata.rs.ServiceContextRoot.class);
          com.webcohesion.enunciate.modules.jaxrs.model.Resource resource = method.getParent();
          while (context == null && resource != null) {
            context = resource.getAnnotation(com.webcohesion.enunciate.metadata.rs.ServiceContextRoot.class);
            resource = resource.getParent();
          }

          String path = method.getFullpath();
          PathBasedResourceGroupImpl resourceGroup = resourcesByPath.get(path);
          if (resourceGroup == null) {
            String contextPath = context != null ? JaxrsModule.sanitizeContextPath(context.value()) : this.relativeContextPath;
            resourceGroup = new PathBasedResourceGroupImpl(contextPath, path, new ArrayList<Resource>());
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
    for (RootResource rootResource : rootResources) {
      // NOTE: do not call facetFilter.accept(rootResource) to check if evaluating resourceMethod's in the rootResource can be skipped
      // - If an "included" facet is defined, and not applied to the Resource; then checking facetFilter.accept(rootResource) will
      //   return false even if the included facet is applied to a method inside the rootResource.
      // - ResourceMethods inherit all the facets applied directly to the rootResource, so skipping the check on the rootResource won't cause
      //   problems with included/excluded facets that are applied to the rootResource

      for (ResourceMethod method : rootResource.getResourceMethods(true)) {
        if (facetFilter.accept(method)) {
          com.webcohesion.enunciate.metadata.rs.ResourceGroup annotation = AnnotationUtils.getResourceGroup(method);
          com.webcohesion.enunciate.modules.jaxrs.model.Resource resource = method.getParent();
          while (annotation == null && resource != null) {
            annotation = AnnotationUtils.getResourceGroup(resource);
            resource = resource.getParent();
          }

          com.webcohesion.enunciate.metadata.rs.ServiceContextRoot context = method.getAnnotation(com.webcohesion.enunciate.metadata.rs.ServiceContextRoot.class);
          resource = method.getParent();
          while (context == null && resource != null) {
            context = resource.getAnnotation(com.webcohesion.enunciate.metadata.rs.ServiceContextRoot.class);
            resource = resource.getParent();
          }

          String label = annotation == null ? "Other" : annotation.value();
          String description = annotation == null ? null : annotation.description();
          if ("##default".equals(description)) {
            description = null;
          }
          AnnotationBasedResourceGroupImpl resourceGroup = resourcesByAnnotation.get(label);
          if (resourceGroup == null) {
            String contextPath = context != null ? JaxrsModule.sanitizeContextPath(context.value()) : this.relativeContextPath;
            resourceGroup = new AnnotationBasedResourceGroupImpl(contextPath, label, new SortedList<Resource>(new ResourceComparator(this.pathSortStrategy)), this.pathSortStrategy);
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

  private static class RootResourceComparator implements Comparator<RootResource> {
    @Override
    public int compare(RootResource r1, RootResource r2) {
      String key1 = r1.getPath() + r1.getQualifiedName();
      String key2 = r2.getPath() + r2.getQualifiedName();
      return key1.compareTo(key2);
    }
  }
}
