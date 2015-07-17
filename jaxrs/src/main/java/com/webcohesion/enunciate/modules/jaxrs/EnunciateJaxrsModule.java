package com.webcohesion.enunciate.modules.jaxrs;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.jaxrs.api.impl.PathBasedResourceGroupImpl;
import com.webcohesion.enunciate.modules.jaxrs.api.impl.ResourceClassResourceGroupImpl;
import com.webcohesion.enunciate.modules.jaxrs.api.impl.ResourceImpl;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceEntityParameter;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceRepresentationMetadata;
import com.webcohesion.enunciate.modules.jaxrs.model.RootResource;
import org.reflections.adapters.MetadataAdapter;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJaxrsModule extends BasicEnunicateModule implements TypeFilteringModule, ApiRegistryProviderModule {

  public enum GroupingStrategy {
    path,
    resource_class
  }

  private final List<MediaTypeDefinitionModule> mediaTypeModules = new ArrayList<MediaTypeDefinitionModule>();
  private ApiRegistry apiRegistry;

  @Override
  public String getName() {
    return "jaxrs";
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Arrays.asList((DependencySpec) new MediaTypeDependencySpec());
  }

  @Override
  public void setApiRegistry(ApiRegistry registry) {
    this.apiRegistry = registry;
  }

  public GroupingStrategy getGroupingStrategy() {
    String groupBy = this.config.getString("[@groupBy]", "class");
    if ("class".equals(groupBy)) {
      return GroupingStrategy.resource_class;
    }
    else if ("path".equals(groupBy)) {
      return GroupingStrategy.path;
    }
    else {
      throw new EnunciateException("Unknown grouping strategy: " + groupBy);
    }
  }

  @Override
  public void call(EnunciateContext context) {
    EnunciateJaxrsContext jaxrsContext = new EnunciateJaxrsContext(context, this.mediaTypeModules);
    Set<Element> elements = context.getApiElements();
    for (Element declaration : elements) {
      if (declaration instanceof TypeElement) {
        TypeElement element = (TypeElement) declaration;
        Path pathInfo = declaration.getAnnotation(Path.class);
        if (pathInfo != null) {
          //add root resource.
          jaxrsContext.add(new RootResource(element, jaxrsContext));
        }

        Provider providerInfo = declaration.getAnnotation(Provider.class);
        if (providerInfo != null) {
          //add jax-rs provider
          jaxrsContext.addJAXRSProvider(element);
        }

        ApplicationPath applicationPathInfo = declaration.getAnnotation(ApplicationPath.class);
        if (applicationPathInfo != null) {
          //todo: configure application path
        }
      }
    }

    List<RootResource> rootResources = jaxrsContext.getRootResources();

    for (RootResource rootResource : rootResources) {
      LinkedList<Element> contextStack = new LinkedList<Element>();
      contextStack.push(rootResource);
      try {
        for (ResourceMethod resourceMethod : rootResource.getResourceMethods(true)) {
          addReferencedDataTypeDefinitions(resourceMethod, contextStack);
        }
      }
      finally {
        contextStack.pop();
      }
    }

    List<ResourceGroup> resourceGroups;
    if (getGroupingStrategy() == GroupingStrategy.path) {
      //group resources by path.
      Map<String, PathBasedResourceGroupImpl> resourcesByPath = new HashMap<String, PathBasedResourceGroupImpl>();

      FacetFilter facetFilter = context.getConfiguration().getFacetFilter();
      for (RootResource rootResource : rootResources) {
        for (ResourceMethod method : rootResource.getResourceMethods(true)) {
          if (facetFilter.accept(method)) {
            String path = method.getFullpath();
            PathBasedResourceGroupImpl resourceGroup = resourcesByPath.get(path);
            if (resourceGroup == null) {
              resourceGroup = new PathBasedResourceGroupImpl(path, new ArrayList<Resource>());
              resourcesByPath.put(path, resourceGroup);
            }

            resourceGroup.getResources().add(new ResourceImpl(method, resourceGroup));
          }
        }
      }

      resourceGroups = new ArrayList<ResourceGroup>(resourcesByPath.values());
    }
    else {
      resourceGroups = new ArrayList<ResourceGroup>();

      for (RootResource rootResource : rootResources) {
        ResourceGroup group = new ResourceClassResourceGroupImpl(rootResource);

        if (!group.getResources().isEmpty()) {
          resourceGroups.add(group);
        }
      }
    }

    Collections.sort(resourceGroups, new Comparator<ResourceGroup>() {
      @Override
      public int compare(ResourceGroup o1, ResourceGroup o2) {
        return o1.getPath().compareTo(o2.getPath());
      }
    });

    this.apiRegistry.getResourceGroups().addAll(resourceGroups);
  }

  /**
   * Add the referenced type definitions for the specified resource method.
   *
   * @param resourceMethod The resource method.
   */
  protected void addReferencedDataTypeDefinitions(ResourceMethod resourceMethod, LinkedList<Element> contextStack) {
    ResourceEntityParameter ep = resourceMethod.getEntityParameter();
    if (ep != null) {
      Set<String> consumes = resourceMethod.getConsumesMediaTypes();
      contextStack.push(ep.getDelegate());

      TypeMirror type = ep.getType();
      if (type instanceof DeclaredType) {
        contextStack.push(resourceMethod);
        try {
          for (MediaTypeDefinitionModule mediaTypeModule : this.mediaTypeModules) {
            mediaTypeModule.addDataTypeDefinition(((DeclaredType) type).asElement(), consumes, contextStack);
          }
        }
        finally {
          contextStack.pop();
        }
      }
    }

    ResourceRepresentationMetadata outputPayload = resourceMethod.getRepresentationMetadata();
    if (outputPayload != null) {
      TypeMirror type = outputPayload.getDelegate();
      if (type instanceof DeclaredType) {
        Set<String> produces = resourceMethod.getProducesMediaTypes();
        contextStack.push(resourceMethod);

        try {
          for (MediaTypeDefinitionModule mediaTypeModule : this.mediaTypeModules) {
            mediaTypeModule.addDataTypeDefinition(((DeclaredType) type).asElement(), produces, contextStack);
          }
        }
        finally {
          contextStack.pop();
        }
      }
    }

    //todo: include referenced type definitions from the errors?
  }

  @Override
  public boolean acceptType(Object type, MetadataAdapter metadata) {
    List<String> classAnnotations = metadata.getClassAnnotationNames(type);
    if (classAnnotations != null) {
      for (String classAnnotation : classAnnotations) {
        if ((Path.class.getName().equals(classAnnotation))
          || (Provider.class.getName().equals(classAnnotation))
          || (ApplicationPath.class.getName().equals(classAnnotation))) {
          return true;
        }
      }
    }
    return false;
  }

  public class MediaTypeDependencySpec implements DependencySpec {

    @Override
    public boolean accept(EnunciateModule module) {
      if (module instanceof MediaTypeDefinitionModule) {
        MediaTypeDefinitionModule definitionModule = (MediaTypeDefinitionModule) module;
        mediaTypeModules.add(definitionModule);

        // suggest to the media type definition module that it should take a passive approach to detecting data types
        // because this module will be aggressively adding the data type definitions to it.
        definitionModule.setDefaultDataTypeDetectionStrategy(MediaTypeDefinitionModule.DataTypeDetectionStrategy.PASSIVE);
        return true;
      }

      return false;
    }

    @Override
    public boolean isFulfilled() {
      // this spec is always fulfilled.
      return true;
    }
  }
}
