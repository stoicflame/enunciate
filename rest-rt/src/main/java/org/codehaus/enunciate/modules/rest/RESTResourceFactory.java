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

package org.codehaus.enunciate.modules.rest;

import org.codehaus.enunciate.rest.annotations.*;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.ApplicationObjectSupport;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * A factory for REST resources.
 *
 * @author Ryan Heaton
 */
public class RESTResourceFactory extends ApplicationObjectSupport {

  private Class[] endpointClasses;
  private List<RESTResource> RESTResources = new ArrayList<RESTResource>();

  /**
   * Sets up the controller for servicing the specified REST endpoints.
   *
   * @throws org.springframework.beans.BeansException
   *          If there was a problem setting it up.
   */
  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    Properties restParameterNames = new Properties();
    try {
      restParameterNames.load(getApplicationContext().getResource("classpath:/enunciate-rest-parameter-names.properties").getInputStream());
    }
    catch (Exception e) {
      //fall through... no parameter names found.
    }

    if (endpointClasses != null) {
      for (Class endpointClass : endpointClasses) {
        Collection<Class> endpointTypes = findEndpointTypes(endpointClass);

        if (endpointTypes.isEmpty()) {
          throw new ApplicationContextException("REST endpoint " + endpointClass.getName() + " does not implement any REST endpoint interfaces.");
        }

        for (Class endpointType : endpointTypes) {
          Method[] restMethods = endpointType.getDeclaredMethods();
          for (Method restMethod : restMethods) {
            int modifiers = restMethod.getModifiers();
            if ((Modifier.isPublic(modifiers)) && (restMethod.isAnnotationPresent(Verb.class)) && (!isImplMethod(restMethod, endpointTypes))) {
              //get the verbs...
              VerbType[] verbs = normalize(restMethod.getAnnotation(Verb.class).value());

              //get the noun...
              String noun = restMethod.getName();
              Noun nounInfo = restMethod.getAnnotation(Noun.class);
              NounContext nounContextInfo = ((NounContext) endpointType.getAnnotation(NounContext.class));
              String context = nounContextInfo != null ? nounContextInfo.value() : "";
              if (nounInfo != null) {
                noun = nounInfo.value();
                if (!"##default".equals(nounInfo.context())) {
                  context = nounInfo.context();
                }
              }

              //get the content types...
              Set<String> supportedContentTypes = new TreeSet<String>();
              supportedContentTypes.add("application/xml");
              supportedContentTypes.add("application/json");

              String defaultContentType = null;
              org.codehaus.enunciate.rest.annotations.ContentType contentTypeInfo = endpointType.getPackage() != null ?
                endpointType.getPackage().getAnnotation(org.codehaus.enunciate.rest.annotations.ContentType.class)
                : null;
              if (contentTypeInfo != null) {
                supportedContentTypes.removeAll(Arrays.asList(contentTypeInfo.unsupported()));
                supportedContentTypes.addAll(Arrays.asList(contentTypeInfo.value()));
                if (!"##undefined".equals(contentTypeInfo.defaultContentType())) {
                  defaultContentType = contentTypeInfo.defaultContentType();
                }
                else if (contentTypeInfo.value().length > 0) {
                  defaultContentType = contentTypeInfo.value()[0];
                }
              }

              contentTypeInfo = (org.codehaus.enunciate.rest.annotations.ContentType) endpointType.getAnnotation(org.codehaus.enunciate.rest.annotations.ContentType.class);
              if (contentTypeInfo != null) {
                supportedContentTypes.removeAll(Arrays.asList(contentTypeInfo.unsupported()));
                supportedContentTypes.addAll(Arrays.asList(contentTypeInfo.value()));
                if (!"##undefined".equals(contentTypeInfo.defaultContentType())) {
                  defaultContentType = contentTypeInfo.defaultContentType();
                }
                else if (contentTypeInfo.value().length > 0) {
                  defaultContentType = contentTypeInfo.value()[0];
                }
              }

              contentTypeInfo = restMethod.getAnnotation(org.codehaus.enunciate.rest.annotations.ContentType.class);
              if (contentTypeInfo != null) {
                supportedContentTypes.removeAll(Arrays.asList(contentTypeInfo.unsupported()));
                supportedContentTypes.addAll(Arrays.asList(contentTypeInfo.value()));
                if (!"##undefined".equals(contentTypeInfo.defaultContentType())) {
                  defaultContentType = contentTypeInfo.defaultContentType();
                }
                else if (contentTypeInfo.value().length > 0) {
                  defaultContentType = contentTypeInfo.value()[0];
                }
              }

              RESTResource resource = getRESTResource(noun, context);
              if (resource == null) {
                resource = new RESTResource(noun, context);
                resource.setParamterNames(restParameterNames);
                RESTResources.add(resource);
              }

              if (defaultContentType != null) {
                resource.setDefaultContentType(defaultContentType);
              }

              for (String contentType : supportedContentTypes) {
                for (VerbType verb : verbs) {
                  if (!resource.addOperation(contentType, verb, restMethod)) {
                    RESTOperation duplicateOperation = resource.getOperation(contentType, verb);

                    throw new ApplicationContextException("Noun '" + noun + "' in context '" + context + "' has more than one '" + verb +
                      "' verb for content type '" + contentType + "'.  One was found at " + restMethod.getDeclaringClass().getName() + "." +
                      restMethod.getName() + ", the other at " + duplicateOperation.method.getDeclaringClass().getName() + "." +
                      duplicateOperation.method.getName() + ".");
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Normalizes the verb types.
   *
   * @param verbs The verbs to normalize.
   * @return The normalized list of verbs.
   */
  private VerbType[] normalize(VerbType[] verbs) {
    EnumSet<VerbType> normalized = EnumSet.noneOf(VerbType.class);
    for (VerbType verb : verbs) {
      normalized.add(verb.getAlias() != null ? verb.getAlias() : verb);
    }
    return normalized.toArray(new VerbType[normalized.size()]);
  }

  /**
   * Gets the REST resource identified by the given context and noun.
   *
   * @param noun    The noun.
   * @param context The context.
   * @return The resource, or null if none was found.
   */
  public RESTResource getRESTResource(String noun, String context) {
    for (RESTResource restResource : RESTResources) {
      if ((restResource.getNounContext().equals(context)) && (restResource.getNoun().equals(noun))) {
        return restResource;
      }
    }

    return null;
  }

  /**
   * Determines whether the specified rest method is an implementation of a method declared in one of
   * the specfied endpoint types.
   *
   * @param restMethod    The method.
   * @param endpointTypes The endpoint types.
   * @return Whether it's an impl method.
   */
  protected boolean isImplMethod(Method restMethod, Collection<Class> endpointTypes) {
    if (restMethod.getDeclaringClass().isInterface()) {
      return false;
    }
    else {
      for (Class endpointType : endpointTypes) {
        if (!endpointType.isInterface()) {
          //only check the interfaces.
          continue;
        }

        try {
          endpointType.getMethod(restMethod.getName(), restMethod.getParameterTypes());
        }
        catch (NoSuchMethodException e) {
          continue;
        }

        return true;
      }

      return false;
    }
  }

  /**
   * Finds the endpoint types that define the API for the specified type.  (The API for some classes
   * is made up of multiple interfaces as well as the impl class itself.)
   *
   * @param endpointType The type for which to find types defining its API.
   * @return The endpoint types.
   */
  protected Collection<Class> findEndpointTypes(Class endpointType) {
    Collection<Class> endpointTypes = new ArrayList<Class>();

    if (endpointType.isAnnotationPresent(RESTEndpoint.class)) {
      endpointTypes.add(endpointType);
    }

    for (Class implementedType : endpointType.getInterfaces()) {
      if (implementedType.isAnnotationPresent(RESTEndpoint.class)) {
        endpointTypes.add(implementedType);
      }
    }

    return endpointTypes;
  }

  /**
   * Gets the REST resources for this controller.
   *
   * @return The REST resources for this controller.
   */
  public List<RESTResource> getRESTResources() {
    return Collections.unmodifiableList(RESTResources);
  }

  /**
   * The classes that make up this REST API.
   *
   * @return The classes that make up this REST API.
   */
  public Class[] getEndpointClasses() {
    return endpointClasses;
  }

  /**
   * The classes that make up this REST API.
   *
   * @param endpointClasses The classes that make up this REST API.
   */
  public void setEndpointClasses(Class[] endpointClasses) {
    this.endpointClasses = endpointClasses;
  }

}
