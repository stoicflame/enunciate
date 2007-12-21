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

package org.codehaus.enunciate.modules.amf;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.codehaus.enunciate.service.EnunciateServiceFactory;
import org.codehaus.enunciate.service.DefaultEnunciateServiceFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.jws.WebService;
import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;

/**
 * @author Ryan Heaton
 */
public class ApplicationContextFilter implements Filter {

  private static final ThreadLocal<ApplicationContext> APP_CONTEXT = new ThreadLocal<ApplicationContext>();
  private static final ThreadLocal<String> SERVICE_FACTORY_BEAN_NAME = new ThreadLocal<String>();

  public static ApplicationContext getApplicationContext() {
    if (APP_CONTEXT.get() == null) {
      throw new IllegalStateException("No application context has been set in a required servlet filter.");
    }
    
    return APP_CONTEXT.get();
  }

  public static String getEnunciateServiceFactoryBeanName() {
    return SERVICE_FACTORY_BEAN_NAME.get();
  }

  public static Object loadServiceBean(Class serviceClass, Class serviceInterface) {
    Object serviceBean;

    WebService wsInfo = (WebService) serviceClass.getAnnotation(WebService.class);
    ApplicationContext applicationContext = ApplicationContextFilter.getApplicationContext();
    Map serviceInterfaceBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, serviceInterface);
    if (serviceInterfaceBeans.size() > 0) {
      String serviceName = wsInfo.serviceName();
      if ((serviceName == null) || ("".equals(serviceName))) {
        serviceName = serviceInterface.getSimpleName() + "Service";
      }

      if (serviceInterfaceBeans.containsKey(serviceName)) {
        //first attempt will be to load the bean identified by the service name:
        serviceBean = serviceInterfaceBeans.get(serviceName);
      }
      else if (serviceInterfaceBeans.size() == 1) {
        // not there; use the only one if it exists...
        serviceBean = serviceInterfaceBeans.values().iterator().next();
      }
      else {
        //panic: can't determine the service bean to use.
        ArrayList beanNames = new ArrayList(serviceInterfaceBeans.keySet());
        throw new ApplicationContextException("There are more than one beans of type " + serviceInterface.getName() +
          " in the application context " + beanNames + ".  Cannot determine which one to use to handle the soap requests.  " +
          "Either reduce the number of beans of this type to one, or specify which one to use by naming it the name of the service (\"" + serviceName + "\").");
      }
    }
    else {
      //try to instantiate the bean with the class...
      try {
        serviceBean = serviceClass.newInstance();
      }
      catch (Exception e) {
        throw new ApplicationContextException("Unable to create an instance of " + serviceClass.getName(), e);
      }
    }

    if (serviceInterface.isInterface()) {
      EnunciateServiceFactory enunciateServiceFactory = new DefaultEnunciateServiceFactory();
      String serviceFactoryName = getEnunciateServiceFactoryBeanName();
      if (serviceFactoryName != null) {
        enunciateServiceFactory = (EnunciateServiceFactory) applicationContext.getBean(serviceFactoryName);
      }
      else {
        Map factories = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, EnunciateServiceFactory.class);
        if (factories.size() > 1) {
          throw new IllegalStateException("Unable to determine which enunciate service factory to use.  Please disambiguate with a 'enunciateServiceFactoryBeanName' servlet parameter.");
        }
        else if (factories.size() == 1) {
          enunciateServiceFactory = (EnunciateServiceFactory) factories.values().iterator().next();
        }
      }
      serviceBean = enunciateServiceFactory.getInstance(serviceBean, serviceInterface);
    }
    return serviceBean;
  }

  public void init(FilterConfig filterConfig) throws ServletException {
    //do nothing.
  }

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    APP_CONTEXT.set(WebApplicationContextUtils.getRequiredWebApplicationContext(((HttpServletRequest)request).getSession(true).getServletContext()));
    SERVICE_FACTORY_BEAN_NAME.set(((HttpServletRequest) request).getSession(true).getServletContext().getInitParameter("enunciateServiceFactoryBeanName"));

    //just pass the request through...
    filterChain.doFilter(request, response);

    APP_CONTEXT.remove();
  }

  public void destroy() {
    //no-op
  }
}
