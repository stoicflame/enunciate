/*
 * Copyright 2006 Web Cohesion
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
import org.codehaus.enunciate.service.DefaultEnunciateServiceFactory;
import org.codehaus.enunciate.service.EnunciateServiceFactory;
import org.codehaus.enunciate.service.EnunciateServiceFactoryAware;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
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
public class RESTResourceFactory extends ApplicationObjectSupport implements EnunciateServiceFactoryAware {

  private EnunciateServiceFactory enunciateServiceFactory = new DefaultEnunciateServiceFactory();
  private Class[] endpointClasses;
  private List<RESTResource> RESTResources = new ArrayList<RESTResource>();

  /**
   * Sets up the controller for servicing the specified REST endpoints.
   *
   * @throws org.springframework.beans.BeansException If there was a problem setting it up.
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
    
    Map<Class, Object> class2instances = new HashMap<Class, Object>();
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
              VerbType[] verbs = restMethod.getAnnotation(Verb.class).value();
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

              RESTResource resource = getRESTResource(noun, context);
              if (resource == null) {
                resource = new RESTResource(noun, context);
                resource.setParamterNames(restParameterNames);
                RESTResources.add(resource);
              }

              Object endpoint = class2instances.get(endpointType);
              if (endpoint == null) {
                endpoint = loadEndpointBean(endpointType, endpointClass);
                class2instances.put(endpointType, endpoint);
              }

              for (VerbType verb : verbs) {
                if (!resource.addOperation(verb, endpoint, restMethod)) {
                  RESTOperation duplicateOperation = resource.getOperation(verb);

                  throw new ApplicationContextException("Noun '" + noun + "' in context '" + context + "' has more than one '" + verb +
                    "' verbs.  One was found at " + restMethod.getDeclaringClass().getName() + "." + restMethod.getName() + ", the other at " +
                    duplicateOperation.method.getDeclaringClass().getName() + "." + duplicateOperation.method.getName() + ".");
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Gets the REST resource identified by the given context and noun.
   *
   * @param noun The noun.
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
   * Attempts to load the endpoint bean by first looking for beans that implement the specified endpoint type.
   * If there is only one, it will be used.  Otherwise, if there is more than one, it will attempt to find one that is named
   * the {@link org.codehaus.enunciate.rest.annotations.RESTEndpoint#name() same as the REST endpoint} or fail.  If there are
   * no endpoint beans in the context that can be assigned to the specified endpoint type, an attempt will be made to
   * instantiate type specified default implementation.
   *
   * @param endpointType The class of the endpoint bean to attempt to load.
   * @param defaultImpl The default implementation class.
   * @return The endpoint bean.
   * @throws org.springframework.beans.BeansException If an attempt was made to instantiate the bean but it failed.
   */
  protected Object loadEndpointBean(Class endpointType, Class defaultImpl) throws BeansException {
    Object endpointBean;
    Map endpointClassBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), endpointType);
    if (endpointClassBeans.size() > 0) {
      RESTEndpoint annotation = (RESTEndpoint) endpointType.getAnnotation(RESTEndpoint.class);
      String endpointName = annotation == null ? "" : annotation.name();
      if (!"".equals(endpointName) && endpointClassBeans.containsKey(endpointName)) {
        //first attempt will be to load the bean identified by the endpoint name:
        endpointBean = endpointClassBeans.get(endpointName);
      }
      else if (endpointClassBeans.size() == 1) {
        // not there; use the only one if it exists...
        endpointBean = endpointClassBeans.values().iterator().next();
      }
      else {
        //panic: can't determine the endpoint bean to use.
        ArrayList beanNames = new ArrayList(endpointClassBeans.keySet());
        if ("".equals(endpointName)) {
          endpointName = "and supply an endpoint name with the @RESTEndpoint annotation";
        }
        throw new ApplicationContextException("There are more than one beans of type " + endpointType.getName() +
          " in the application context " + beanNames + ".  Cannot determine which one to use to handle the REST requests.  " +
          "Either reduce the number of beans of this type to one, or specify which one to use by naming it the name of the REST endpoint (" +
          endpointName + ").");
      }
    }
    else {
      //try to instantiate the bean with the default impl...
      try {
        endpointBean = defaultImpl.newInstance();
      }
      catch (Exception e) {
        throw new ApplicationContextException("Unable to instantiate REST endpoint bean of class " + defaultImpl.getName() + ".", e);
      }
    }

    if (endpointType.isInterface()) {
      endpointBean = this.enunciateServiceFactory.getInstance(endpointBean, endpointType);
    }

    return endpointBean;
  }

  /**
   * Determines whether the specified rest method is an implementation of a method declared in one of
   * the specfied endpoint types.
   *
   * @param restMethod The method.
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

  /**
   * Sets the enunciate service factory for this REST controller.
   *
   * @param enunciateServiceFactory The enunciate service factory.
   */
  public void setEnunciateServiceFactory(EnunciateServiceFactory enunciateServiceFactory) {
    this.enunciateServiceFactory = enunciateServiceFactory;
  }
}
