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
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.spring_web.model.*;
import com.webcohesion.enunciate.util.PathSortStrategy;
import org.reflections.adapters.MetadataAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class SpringWebModule extends BasicProviderModule implements TypeFilteringModule, ApiRegistryProviderModule, ApiFeatureProviderModule {

  private DataTypeDetectionStrategy defaultDataTypeDetectionStrategy;
  private final List<MediaTypeDefinitionModule> mediaTypeModules = new ArrayList<MediaTypeDefinitionModule>();
  private ApiRegistry apiRegistry;
  private EnunciateSpringWebContext springContext;
  static final String NAME = "spring-web";
  private PathSortStrategy defaultSortStrategy = PathSortStrategy.breadth_first;

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<DependencySpec> getDependencySpecifications() {
    return Arrays.asList((DependencySpec) new MediaTypeDependencySpec());
  }

  public DataTypeDetectionStrategy getDataTypeDetectionStrategy() {
    String dataTypeDetection = this.config.getString("[@datatype-detection]", null);

    if (dataTypeDetection != null) {
      try {
        return DataTypeDetectionStrategy.valueOf(dataTypeDetection);
      }
      catch (IllegalArgumentException e) {
        //fall through...
      }
    }

    if (this.defaultDataTypeDetectionStrategy != null) {
      return this.defaultDataTypeDetectionStrategy;
    }

    if (this.enunciate.getIncludePatterns().isEmpty()) {
      //if there are no configured include patterns, then we'll just stick with "local" detection so we don't include too much.
      return DataTypeDetectionStrategy.local;
    }
    else {
      //otherwise, we'll assume the user knows what (s)he's doing and aggressively include everything.
      return DataTypeDetectionStrategy.aggressive;
    }
  }

  public void setDefaultDataTypeDetectionStrategy(DataTypeDetectionStrategy strategy) {
    this.defaultDataTypeDetectionStrategy = strategy;
  }

  public PathSortStrategy getPathSortStrategy()  {
    PathSortStrategy strategy = defaultSortStrategy;
    try {
      strategy = PathSortStrategy.valueOf(this.config.getString("[@path-sort-strategy]", this.defaultSortStrategy.name()));
    } catch (IllegalArgumentException e) {
      // Ignore?  Log?
    }
    return strategy;
  }

  public void setDefaultSortStrategy(PathSortStrategy defaultSortStrategy) {
    this.defaultSortStrategy = defaultSortStrategy;
  }

  @Override
  public void setApiRegistry(ApiRegistry registry) {
    this.apiRegistry = registry;
  }

  public EnunciateSpringWebContext getSpringWebContext() {
    return springContext;
  }

  @Override
  public void call(EnunciateContext context) {
    springContext = new EnunciateSpringWebContext(context);

    DataTypeDetectionStrategy detectionStrategy = getDataTypeDetectionStrategy();
    if (detectionStrategy != DataTypeDetectionStrategy.passive) {
      Set<? extends Element> elements = detectionStrategy == DataTypeDetectionStrategy.local ? context.getLocalApiElements() : context.getApiElements();
      for (Element declaration : elements) {
        //first loop through and gather all the controller advice.
        if (declaration instanceof TypeElement) {
          TypeElement element = (TypeElement) declaration;
          Controller controllerInfo = declaration.getAnnotation(Controller.class);
          RestController restControllerInfo = declaration.getAnnotation(RestController.class);
          ControllerAdvice controllerAdvice = declaration.getAnnotation(ControllerAdvice.class);
          if (controllerInfo != null || restControllerInfo != null || controllerAdvice != null) {
            springContext.add(new SpringControllerAdvice(element, springContext));
          }
        }
      }

      for (Element declaration : elements) {
        if (declaration instanceof TypeElement) {
          TypeElement element = (TypeElement) declaration;
          Controller controllerInfo = declaration.getAnnotation(Controller.class);
          RestController restControllerInfo = declaration.getAnnotation(RestController.class);
          if (controllerInfo != null || restControllerInfo != null) {
            //add root resource.
            SpringController springController = new SpringController(element, springContext);
            LinkedList<Element> contextStack = new LinkedList<Element>();
            contextStack.push(springController);
            try {
              List<RequestMapping> requestMappings = springController.getRequestMappings();
              if (!requestMappings.isEmpty()) {
                springContext.add(springController);

                for (RequestMapping requestMapping : requestMappings) {
                  addReferencedDataTypeDefinitions(requestMapping, contextStack);
                }
              }
            }
            finally {
              contextStack.pop();
            }
          }
        }
      }
    }


    //tidy up the application path.
    String relativeContextPath = this.config.getString("application[@path]", "");
    while (relativeContextPath.startsWith("/")) {
      relativeContextPath = relativeContextPath.substring(1);
    }

    while (relativeContextPath.endsWith("/")) {
      //trim off any leading slashes
      relativeContextPath = relativeContextPath.substring(0, relativeContextPath.length() - 1);
    }

    springContext.setRelativeContextPath(relativeContextPath);
    springContext.setGroupingStrategy(getGroupingStrategy());
    springContext.setPathSortStrategy(getPathSortStrategy());

    if (!springContext.getControllers().isEmpty()) {
      this.apiRegistry.getResourceApis().add(this.springContext);
    }
  }

  /**
   * Add the referenced type definitions for the specified resource method.
   *
   * @param requestMapping The resource method.
   */
  protected void addReferencedDataTypeDefinitions(RequestMapping requestMapping, LinkedList<Element> contextStack) {
    ResourceEntityParameter ep = requestMapping.getEntityParameter();
    if (ep != null) {
      Set<String> consumes = requestMapping.getConsumesMediaTypes();
      contextStack.push(ep.getDelegate());

      TypeMirror type = ep.getType();
      contextStack.push(requestMapping);
      try {
        for (MediaTypeDefinitionModule mediaTypeModule : this.mediaTypeModules) {
          mediaTypeModule.addDataTypeDefinitions(type, consumes, contextStack);
        }
      }
      finally {
        contextStack.pop();
      }
    }

    ResourceRepresentationMetadata outputPayload = requestMapping.getRepresentationMetadata();
    if (outputPayload != null) {
      TypeMirror type = outputPayload.getDelegate();
      Set<String> produces = requestMapping.getProducesMediaTypes();
      contextStack.push(requestMapping);

      try {
        for (MediaTypeDefinitionModule mediaTypeModule : this.mediaTypeModules) {
          mediaTypeModule.addDataTypeDefinitions(type, produces, contextStack);
        }
      }
      finally {
        contextStack.pop();
      }
    }

    List<? extends ResponseCode> statusCodes = requestMapping.getStatusCodes();
    if (statusCodes != null) {
      for (ResponseCode statusCode : statusCodes) {
        TypeMirror type = statusCode.getType();
        if (type != null) {
          Set<String> produces = requestMapping.getProducesMediaTypes();
          contextStack.push(requestMapping);

          try {
            for (MediaTypeDefinitionModule mediaTypeModule : this.mediaTypeModules) {
              mediaTypeModule.addDataTypeDefinitions(type, produces, contextStack);
            }
          }
          finally {
            contextStack.pop();
          }
        }
      }
    }
  }

  public EnunciateSpringWebContext.GroupingStrategy getGroupingStrategy() {
    String groupBy = this.config.getString("[@groupBy]", "class");
    if ("class".equals(groupBy)) {
      return EnunciateSpringWebContext.GroupingStrategy.resource_class;
    }
    else if ("path".equals(groupBy)) {
      return EnunciateSpringWebContext.GroupingStrategy.path;
    }
    else if ("annotation".equals(groupBy)) {
      return EnunciateSpringWebContext.GroupingStrategy.annotation;
    }
    else {
      throw new EnunciateException("Unknown grouping strategy: " + groupBy);
    }
  }

  @Override
  public boolean acceptType(Object type, MetadataAdapter metadata) {
    List<String> classAnnotations = metadata.getClassAnnotationNames(type);
    if (classAnnotations != null) {
      for (String classAnnotation : classAnnotations) {
        if ((Controller.class.getName().equals(classAnnotation))
          || (RestController.class.getName().equals(classAnnotation))) {
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
        definitionModule.setDefaultDataTypeDetectionStrategy(DataTypeDetectionStrategy.passive);
        return true;
      }

      return false;
    }

    @Override
    public boolean isFulfilled() {
      // this spec is always fulfilled.
      return true;
    }

    @Override
    public String toString() {
      return "media type definition modules";
    }
  }
}
