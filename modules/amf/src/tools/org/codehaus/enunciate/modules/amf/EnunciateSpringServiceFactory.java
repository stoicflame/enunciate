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

import org.granite.messaging.service.SimpleServiceFactory;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.service.ServiceInvoker;
import org.granite.messaging.service.SpringServiceInvoker;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.context.GraniteContext;
import org.granite.config.flex.Destination;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.BeansException;
import org.springframework.util.ClassUtils;
import org.codehaus.enunciate.service.EnunciateServiceFactory;
import org.codehaus.enunciate.service.DefaultEnunciateServiceFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.jws.WebService;
import java.util.Map;
import java.util.ArrayList;

import flex.messaging.messages.RemotingMessage;

/**
 * @author Ryan Heaton
 */
public class EnunciateSpringServiceFactory extends SimpleServiceFactory {

  private ServiceBean serviceBean;

  @Override
  public void configure(Map<String, Object> properties) throws ServiceException {
    super.configure(properties);

    // getting spring context from container
    GraniteContext context = GraniteContext.getCurrentInstance();
    ServletContext sc = ((HttpGraniteContext) context).getServletContext();
    ApplicationContext springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);

    String serviceClassName = (String) properties.get("serviceClass");
    if (serviceClassName == null) {
      throw new ServiceException("A service class must be supplied in the properties of the EnunciateSpringServiceFactory.");
    }

    Class serviceClass;
    try {
      serviceClass = ClassUtils.forName(serviceClassName);
    }
    catch (ClassNotFoundException e) {
      throw new ServiceException("Service class not found: " + serviceClassName);
    }

    Class serviceInterface = serviceClass;
    WebService wsInfo = (WebService) serviceInterface.getAnnotation(WebService.class);
    if (wsInfo == null) {
      throw new ServiceException("Can't find the @javax.jws.WebService annotation on " + serviceClass.getName());
    }

    String eiValue = wsInfo.endpointInterface();
    if (eiValue != null && eiValue.length() > 0) {
      try {
        serviceInterface = ClassUtils.forName(eiValue);
        wsInfo = (WebService) serviceInterface.getAnnotation(WebService.class);
        if (wsInfo == null) {
          throw new ServiceException("No @javax.jws.WebService annotation on service interface " + serviceInterface.getName());
        }
      }
      catch (ClassNotFoundException e) {
        throw new ApplicationContextException("Couldn't find endpoint interface " + wsInfo.endpointInterface(), e);
      }
    }

    Object serviceBean;
    Map serviceInterfaceBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(springContext, serviceInterface);
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

    String serviceFactoryName = (String) properties.get("enunciateServiceFactoryBeanName");
    if (serviceInterface.isInterface()) {
      serviceBean = loadServiceFactory(springContext, serviceFactoryName).getInstance(serviceBean, serviceInterface);
    }

    this.serviceBean = new ServiceBean(serviceInterface, serviceBean);
  }

  @Override
  public ServiceInvoker<?> getServiceInstance(RemotingMessage request) throws ServiceException {
    String messageType = request.getClass().getName();
    String destinationId = request.getDestination();

    GraniteContext context = GraniteContext.getCurrentInstance();
    Destination destination = context.getServicesConfig().findDestinationById(messageType, destinationId);
    if (destination == null) {
      throw new ServiceException("No matching destination: " + destinationId);
    }

    return new EnunciateSpringServiceInvoker(destination, this, serviceBean);
  }

  /**
   * Loads the service factory from the application context.
   * @param applicationContext The app context.
   * @param serviceFactoryName The service factory name.
   * @return The service factory.
   */
  protected EnunciateServiceFactory loadServiceFactory(ApplicationContext applicationContext, String serviceFactoryName) {
    EnunciateServiceFactory enunciateServiceFactory = new DefaultEnunciateServiceFactory();
    if (serviceFactoryName != null) {
      enunciateServiceFactory = (EnunciateServiceFactory) applicationContext.getBean(serviceFactoryName);
    }
    else {
      Map factories = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, EnunciateServiceFactory.class);
      if (factories.size() > 1) {
        throw new ServiceException("Unable to determine which enunciate service factory to use.  Please disambiguate with a 'enunciateServiceFactoryBeanName' servlet parameter.");
      }
      else if (factories.size() == 1) {
        enunciateServiceFactory = (EnunciateServiceFactory) factories.values().iterator().next();
      }
    }
    return enunciateServiceFactory;
  }

}
