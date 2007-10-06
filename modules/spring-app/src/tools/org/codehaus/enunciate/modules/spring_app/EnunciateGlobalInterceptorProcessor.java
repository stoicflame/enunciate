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

package org.codehaus.enunciate.modules.spring_app;

import org.aopalliance.aop.Advice;
import org.codehaus.enunciate.service.EnunciateServiceFactory;
import org.codehaus.enunciate.service.EnunciateServiceFactoryAware;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ensures that objects that are {@link EnunciateServiceFactoryAware} are
 * injected with the correct service factory that will handle the necessary
 * interceptor support.
 *
 * @author Ryan Heaton
 */
public class EnunciateGlobalInterceptorProcessor extends ApplicationObjectSupport implements EnunciateServiceFactory {

  private final List<Object> globalServiceInterceptors = new ArrayList<Object>();


  @Override
  protected void initApplicationContext() throws BeansException {
    ApplicationContext ctx = getApplicationContext();

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

  // Inherited.
  public Object getInstance(Class implClass, Class... interfaces) throws IllegalAccessException, InstantiationException {
    return getInstance(implClass.newInstance(), interfaces);
  }

  // Inherited.
  public Object getInstance(Object impl, Class... interfaces) {
    if (globalServiceInterceptors.size() > 0) {
      ProxyFactory proxyFactory = new ProxyFactory(impl);
      proxyFactory.setInterfaces(interfaces);

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
