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

package org.codehaus.enunciate.modules.xfire;

import org.aopalliance.aop.Advice;
import org.codehaus.enunciate.service.DefaultEnunciateServiceFactory;
import org.codehaus.enunciate.service.EnunciateServiceFactoryAware;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.core.Ordered;

import java.util.*;

/**
 * The EnunciateHandlerMapping is just a SimpleUrlHandlerMapping that extracts its interceptors from
 * the context, rather than sets them explicitly.  It's intended that there should be only one of
 * these per servlet context, as multiple instances will obtain the same list of interceptors.
 * 
 * @author Ryan Heaton
 */
public class EnunciateHandlerMapping extends SimpleUrlHandlerMapping {

  private final List<Object> globalServiceInterceptors = new ArrayList<Object>();

  /**
   * Attempts to find any instances of {@link EnunciateHandlerMapping} in the context, orders
   * them, and sets them as interceptors for this handler mapping.
   *
   * @throws BeansException
   */
  @Override
  public void initApplicationContext() throws BeansException {
    loadInterceptors();
    super.initApplicationContext();
  }

  @Override
  public void setMappings(Properties mappings) {
    throw new UnsupportedOperationException("The EnunciateHandlerMapping doesn't support setting its mappings via bean id.");
  }

  @Override
  public void setUrlMap(Map urlMap) {
    EnunciateHandlerMappingServiceFactory serviceFactory = new EnunciateHandlerMappingServiceFactory();
    for (Object handler : urlMap.values()) {
      if (handler instanceof EnunciateServiceFactoryAware) {
        ((EnunciateServiceFactoryAware) handler).setEnunciateServiceFactory(serviceFactory);
      }
    }

    super.setUrlMap(urlMap);
  }

  /**
   * Loads the interceptors found in the context.
   */
  protected void loadInterceptors() {
    ApplicationContext ctx = getApplicationContext();
    Map interceptorBeans = ctx.getBeansOfType(EnunciateHandlerInterceptor.class);
    if (interceptorBeans.size() > 0) {
      ArrayList<EnunciateHandlerInterceptor> interceptors = new ArrayList<EnunciateHandlerInterceptor>();
      interceptors.addAll(interceptorBeans.values());
      Collections.sort(interceptors, EnunciateHandlerInterceptorComparator.INSTANCE);
      setInterceptors(interceptors.toArray(new EnunciateHandlerInterceptor[interceptors.size()]));
    }

    Map adviceBeans = ctx.getBeansOfType(EnunciateServiceAdvice.class);
    for (Object advice : adviceBeans.values()) {
      addGlobalInterceptor(advice);
    }

    Map advisorBeans = ctx.getBeansOfType(EnunciateServiceAdvisor.class);
    for (Object advisor : advisorBeans.values()) {
      addGlobalInterceptor(advisor);
    }
  }

  /**
   * Ordered list of interceptors to inject on all service beans.
   *
   * @param globalServiceInterceptors The list of interceptors.
   */
  public void setGlobalServiceInterceptors(List<Object> globalServiceInterceptors) {
    for (Object globalServiceInterceptor : globalServiceInterceptors) {
      addGlobalInterceptor(globalServiceInterceptor);
    }
  }

  /**
   * Adds a global service interceptor to this list in order.
   *
   * @param globalInterceptor The global interceptor to add to the list in order.
   */
  protected void addGlobalInterceptor(Object globalInterceptor) {
    if (!((globalInterceptor instanceof Advice) || (globalInterceptor instanceof Advisor))) {
      throw new ApplicationContextException("Attempt to inject an interceptor that is neither advice nor an advisor (class: "
        + globalInterceptor.getClass() + ").");
    }

    int order = 0;

    if (globalInterceptor instanceof Ordered) {
      order = ((Ordered) globalInterceptor).getOrder();
    }

    int index;
    for (index = this.globalServiceInterceptors.size() - 1; index >= 0; index--) {
      Object interceptor = this.globalServiceInterceptors.get(index);
      int itemOrder = 0;

      if (interceptor instanceof Ordered) {
        itemOrder = ((Ordered) interceptor).getOrder();
      }

      if (order >= itemOrder) {
        break;
      }
    }

    this.globalServiceInterceptors.add(index + 1, globalInterceptor);

  }

  /**
   * Service factory that wraps its implementations with the configured advice/advisors.
   */
  private class EnunciateHandlerMappingServiceFactory extends DefaultEnunciateServiceFactory {

    @Override
    public Object getInstance(Object impl) {
      impl = super.getInstance(impl);

      if (globalServiceInterceptors.size() > 0) {
        ProxyFactory proxyFactory = new ProxyFactory(impl);

        for (Object globalServiceInterceptor : globalServiceInterceptors) {
          if (globalServiceInterceptor instanceof Advice) {
            proxyFactory.addAdvice((Advice) globalServiceInterceptor);
          }
          else if (globalServiceInterceptor instanceof Advisor) {
            proxyFactory.addAdvisor((Advisor) globalServiceInterceptor);
          }
          else {
            throw new ApplicationContextException("Attempt to inject an interceptor that is neither advice nor an advisor (class: "
              + globalServiceInterceptor.getClass() + ").");
          }
        }

        impl = proxyFactory.getProxy();
      }
      
      return impl;
    }
  }

}
