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

import junit.framework.TestCase;
import org.codehaus.enunciate.rest.annotations.VerbType;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class TestRESTResourceFactory extends TestCase {

  /**
   * tests the initialization of the application context.
   */
  public void testInitApplicationContext() throws Exception {
    RESTResourceFactory factory = new RESTResourceFactory();
    factory.setEndpointClasses(new Class[]{EndpointOneImpl.class, EndpointTwo.class, EndpointThreeImpl.class});
    factory.setApplicationContext(new GenericWebApplicationContext());

    String[] nouns = {"one", "two", "three", "four", "five", "six"};
    ArrayList<RESTResource> resources = new ArrayList<RESTResource>(factory.getRESTResources());
    for (String noun : nouns) {
      String context = "";
      if (("three".equals(noun)) || ("four".equals(noun))) {
        context = "three/or/four";
      }
      else if ("six".equals(noun)) {
        context = "evens";
      }

      RESTResource resource = factory.getRESTResource(noun, context);
      assertNotNull(resource);

      for (VerbType verbType : VerbType.values()) {
        if (verbType.getAlias() != null) {
          verbType = verbType.getAlias();
        }
        
        RESTOperation operation = resource.getOperation("text/xml", verbType);
        assertNotNull(resource.toString() + " does not contain an operation for " + verbType + ".", operation);
      }

      resources.remove(resource);
    }

    assertTrue(resources.isEmpty());

    //now we want to make sure that we can advise the impls...
    final ProxyFactoryBean advisedEndpoint = new ProxyFactoryBean();
    advisedEndpoint.setTarget(new EndpointOneImpl());
    advisedEndpoint.setProxyInterfaces(new String[]{"org.codehaus.enunciate.modules.rest.EndpointOne"});
    factory = new RESTResourceFactory();
    GenericWebApplicationContext ctx = new GenericWebApplicationContext() {
      @Override
      public Map getBeansOfType(Class aClass) throws BeansException {
        if (org.codehaus.enunciate.modules.rest.EndpointOne.class.equals(aClass)) {
          HashMap<String, Object> beansOfType = new HashMap<String, Object>();
          beansOfType.put("doesntmatter", advisedEndpoint);
          return beansOfType;
        }

        return super.getBeansOfType(aClass);
      }
    };

    factory.setEndpointClasses(new Class[]{EndpointOneImpl.class});
    factory.setApplicationContext(ctx);

    nouns = new String[]{"one", "two"};
    resources = new ArrayList<RESTResource>(factory.getRESTResources());
    for (String noun : nouns) {
      String context = "";
      if (("three".equals(noun)) || ("four".equals(noun))) {
        context = "three/or/four";
      }
      else if ("six".equals(noun)) {
        context = "evens";
      }

      RESTResource resource = factory.getRESTResource(noun, context);
      assertNotNull(resource);

      for (VerbType verbType : VerbType.values()) {
        verbType = verbType.getAlias() != null ? verbType.getAlias() : verbType;
        RESTOperation operation = resource.getOperation("text/xml", verbType);
        assertSame(advisedEndpoint, operation.getEndpoint());
        assertNotNull(operation);
      }
      resources.remove(resource);
    }

    assertTrue(resources.isEmpty());
  }


}
