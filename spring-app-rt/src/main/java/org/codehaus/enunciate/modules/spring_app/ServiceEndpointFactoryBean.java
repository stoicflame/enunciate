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

package org.codehaus.enunciate.modules.spring_app;

import org.aopalliance.aop.Advice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.SingletonTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Factory bean for creating service bean instances.
 *
 * @author Ryan Heaton
 */
public class ServiceEndpointFactoryBean extends ApplicationObjectSupport implements FactoryBean {

  private static final Log LOG = LogFactory.getLog(ServiceEndpointFactoryBean.class);

  private boolean initialized = false;
  private final List<Object> interceptors = new ArrayList<Object>();
  private final Class serviceInterface;
  private Object serviceImplementationBean;
  private String defaultImplementationBeanName;
  private Class defaultImplementationClass;
  private boolean requireInstanceOfImpl = false;

  public ServiceEndpointFactoryBean(Class serviceInterface) {
    if (serviceInterface == null) {
      throw new ApplicationContextException("A service interface must be provided to create a service endpoint.");
    }

    this.serviceInterface = serviceInterface;
  }

  @Override
  protected void initApplicationContext(ApplicationContext context) throws BeansException {
    if (isRequireInstanceOfImpl() && getDefaultImplementationClass() == null) {
      throw new ApplicationContextException("The service bean is required to implement a class that isn't supplied.");
    }

    Map adviceBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, EnunciateServiceAdvice.class);
    for (Object advice : adviceBeans.values()) {
      addInterceptor(advice);
    }

    Map advisorBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, EnunciateServiceAdvisor.class);
    for (Object advisor : advisorBeans.values()) {
      addInterceptor(advisor);
    }

    if ((serviceImplementationBean == null) && (defaultImplementationBeanName != null)) {
      try {
        serviceImplementationBean = context.getBean(defaultImplementationBeanName, this.serviceInterface);
      }
      catch (BeansException e) {
        //fall through...
      }
    }

    if (serviceImplementationBean == null) {
      Map serviceInterfaceBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, serviceInterface);
      if (serviceInterfaceBeans.size() > 1) {
        //panic: can't determine the service bean to use.
        StringBuilder builder = new StringBuilder("There are more than one beans of type ");
        builder.append(serviceInterface.getName());
        builder.append(" in the application context (");
        Iterator beanNameIt = serviceInterfaceBeans.keySet().iterator();
        while (beanNameIt.hasNext()) {
          Object beanName = beanNameIt.next();
          builder.append(beanName);
          if (beanNameIt.hasNext()) {
            builder.append(", ");
          }
        }
        builder.append("). Cannot determine which one to use to handle the service requests.");
        if (defaultImplementationBeanName != null) {
          builder.append("Either reduce the number of beans of this type to one, or specify which one to use by naming it \"");
          builder.append(this.defaultImplementationBeanName);
          builder.append("\".");
        }
        else {
          builder.append("Please reduce the number of beans of this type to one.");
        }
        throw new ApplicationContextException(builder.toString());
      }
      else if (serviceInterfaceBeans.size() == 1) {
        //if there is only one defined, use that instead.
        serviceImplementationBean = serviceInterfaceBeans.values().iterator().next();
      }
    }

    if (serviceImplementationBean == null) {
      if (defaultImplementationClass == null) {
        throw new ApplicationContextException("Unable to create a service implementation bean for interface " + this.serviceInterface + ". Please provide a default implementation class.");
      }

      try {
        serviceImplementationBean = defaultImplementationClass.newInstance();

        //do the bean autowiring
        SpringComponentPostProcessor.autowire(serviceImplementationBean, (WebApplicationContext) getApplicationContext());
      }
      catch (Exception e) {
        throw new ApplicationContextException("Unable to instantiate " + defaultImplementationClass.getName(), e);
      }
    }
    else if (isRequireInstanceOfImpl() && !defaultImplementationClass.isInstance(serviceImplementationBean)) {
      throw new ApplicationContextException("Found service implementation bean is not an instance of " + defaultImplementationClass.getName());      
    }

    initialized = true;
  }

  /**
   * Ordered list of interceptors to inject on all service beans.
   *
   * @param interceptors The list of interceptors.
   */
  public void setInterceptors(List<Object> interceptors) {
    this.interceptors.clear();
    for (Object globalServiceInterceptor : interceptors) {
      addInterceptor(globalServiceInterceptor);
    }
  }

  /**
   * Adds an interceptor to this list in order.
   *
   * @param interceptor The interceptor to add to the list in order.
   */
  protected void addInterceptor(Object interceptor) {
    if (!((interceptor instanceof Advice) || (interceptor instanceof Advisor))) {
      throw new ApplicationContextException("Attempt to inject an interceptor that is neither advice nor an advisor (class: "
        + interceptor.getClass() + ").");
    }

    int order = 0;

    if (interceptor instanceof Ordered) {
      order = ((Ordered) interceptor).getOrder();
    }

    int index;
    for (index = this.interceptors.size() - 1; index >= 0; index--) {
      Object item = this.interceptors.get(index);
      int itemOrder = 0;

      if (item instanceof Ordered) {
        itemOrder = ((Ordered) item).getOrder();
      }

      if (order >= itemOrder) {
        break;
      }
    }

    this.interceptors.add(index + 1, interceptor);
  }

  // Inherited.
  public Object getObject() throws Exception {
    if (!initialized) {
      throw new FactoryBeanNotInitializedException();
    }
    
    return wrapEndpoint(this.serviceInterface, this.serviceImplementationBean);
  }

  // Inherited.
  public Class getObjectType() {
    return this.serviceInterface;
  }

  // Inherited.
  public boolean isSingleton() {
    return true;
  }

  /**
   * Wraps a specific endpoint bean with the necessary interceptors.
   *
   * @param iface        The interface.
   * @param endpointImpl The implementation.
   * @return The wrapped endpoint.
   */
  public Object wrapEndpoint(final Class iface, Object endpointImpl) throws Exception {
    Object endpoint = endpointImpl;

    final boolean isInterface = iface.isInterface();
    final boolean requireInstanceOfImpl = isRequireInstanceOfImpl();

    if (interceptors.size() > 0) {
      ProxyFactory proxyFactory = new ProxyFactory();
      if (!isInterface || requireInstanceOfImpl) {
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.setTargetSource(new SingletonTargetSource(endpointImpl) {
          @Override
          public Class getTargetClass() {
            return requireInstanceOfImpl ? getDefaultImplementationClass() : iface;
          }
        });
      }
      else {
        proxyFactory.setTarget(endpointImpl);
        proxyFactory.setInterfaces(new Class[]{iface});
      }

      for (Object interceptor : interceptors) {
        if (interceptor instanceof Advice) {
          proxyFactory.addAdvice((Advice) interceptor);
        }
        else if (interceptor instanceof Advisor) {
          proxyFactory.addAdvisor((Advisor) interceptor);
        }
        else {
          throw new ApplicationContextException("Attempt to inject an interceptor that is neither advice nor an advisor (class: " + interceptor.getClass() + ").");
        }
      }

      endpoint = proxyFactory.getProxy();

      if (requireInstanceOfImpl && !getDefaultImplementationClass().isInstance(endpoint)) {
        throw new ApplicationContextException("Created proxy is not an instance of " + getDefaultImplementationClass().getName());
      }
    }

    return endpoint;
  }

  /**
   * The service interface.
   * 
   * @return The service interface.
   */
  public Class getServiceInterface() {
    return serviceInterface;
  }

  /**
   * The implementation bean.
   *
   * @return The implementation bean.
   */
  public Object getServiceImplementationBean() {
    return serviceImplementationBean;
  }

  /**
   * The implementation bean.
   *
   * @param serviceImplementationBean The implementation bean.
   */
  public void setServiceImplementationBean(Object serviceImplementationBean) {
    this.serviceImplementationBean = serviceImplementationBean;
  }

  /**
   * The bean name to use to look up the implementation bean if it hasn't been explicitly set.
   *
   * @return The bean name to use to look up the implementation bean if it hasn't been explicitly set.
   */
  public String getDefaultImplementationBeanName() {
    return defaultImplementationBeanName;
  }

  /**
   * The bean name to use to look up the implementation bean if it hasn't been explicitly set.
   *
   * @param defaultImplementationBeanName The bean name to use to look up the implementation bean if it hasn't been explicitly set.
   */
  public void setDefaultImplementationBeanName(String defaultImplementationBeanName) {
    this.defaultImplementationBeanName = defaultImplementationBeanName;
  }

  /**
   * The class to use if no other implementations are provided.
   *
   * @return The class to use if no other implementations are provided.
   */
  public Class getDefaultImplementationClass() {
    return defaultImplementationClass;
  }

  /**
   * The class to use if no other implementations are provided.
   *
   * @param defaultImplementationClass The class to use if no other implementations are provided.
   */
  public void setDefaultImplementationClass(Class defaultImplementationClass) {
    this.defaultImplementationClass = defaultImplementationClass;
  }

  /**
   * Whether to require that the service bean instance be an instance of the default implementation class.
   *
   * @return Whether to require that the service bean instance be an instance of the default implementation class.
   */
  public boolean isRequireInstanceOfImpl() {
    return requireInstanceOfImpl;
  }

  /**
   * Whether to require that the service bean instance be an instance of the default implementation class.
   *
   * @param requireInstanceOfImpl Whether to require that the service bean instance be an instance of the default implementation class.
   */
  public void setRequireInstanceOfImpl(boolean requireInstanceOfImpl) {
    this.requireInstanceOfImpl = requireInstanceOfImpl;
  }
}