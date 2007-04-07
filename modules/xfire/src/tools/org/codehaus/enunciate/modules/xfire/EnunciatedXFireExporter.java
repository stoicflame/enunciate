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

import org.codehaus.xfire.annotations.AnnotationException;
import org.codehaus.xfire.annotations.WebServiceAnnotation;
import org.codehaus.xfire.handler.HandlerSupport;
import org.codehaus.xfire.service.ServiceFactory;
import org.codehaus.xfire.spring.remoting.XFireExporter;
import org.codehaus.xfire.util.ClassLoaderUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Exports a SOAP endpoint for XFire.
 * 
 * @author Ryan Heaton
 */
public class EnunciatedXFireExporter extends XFireExporter {

  private ApplicationContext ctx;
  private EnunciatedXFireServletController delegate;
  private View wsdlView = null;

  public void afterPropertiesSet() throws Exception {
    Object serviceBean = loadServiceBean();
    setServiceBean(serviceBean);
    if (serviceBean instanceof HandlerSupport) {
      //set the XFire in/out handlers that may possibly be configured.
      HandlerSupport handlerSupport = (HandlerSupport) serviceBean;
      setInHandlers(handlerSupport.getInHandlers());
      setOutHandlers(handlerSupport.getOutHandlers());
      setFaultHandlers(handlerSupport.getFaultHandlers());
    }

    super.afterPropertiesSet();

    delegate = new EnunciatedXFireServletController(getXfire(), getXFireService().getName(), this.wsdlView);
  }

  /**
   * Attempts to load the service bean by first looking for beans that implement the {@link #getServiceClass() service class}.
   * If there is only one, it will be used.  Otherwise, if there is more than one, it will attempt to find one that is named
   * the same as the service name or fail.  If there are no service beans in the context that can be
   * assigned to the service class, an attempt will be made to instantiate one.
   * 
   * @return The service bean.
   * @throws InstantiationException If an attempt was made to instantiate the bean but it failed.
   * @throws IllegalAccessException If an attempt was made to instantiate the bean but it couldn't get access.
   */
  protected Object loadServiceBean() throws InstantiationException, IllegalAccessException {
    Object serviceBean;
    Class serviceClass = getServiceClass();
    Class serviceInterface = null;
    EnunciatedJAXWSServiceFactory factory = (EnunciatedJAXWSServiceFactory) getServiceFactory();
    WebServiceAnnotation annotation = factory.getAnnotations().getWebServiceAnnotation(serviceClass);
    if (annotation == null) {
      throw new AnnotationException("Can't find the @javax.jws.WebService annotation on " + serviceClass.getName());
    }
    String eiValue = annotation.getEndpointInterface();
    if (eiValue != null && eiValue.length() > 0) {
      try {
        serviceInterface = ClassLoaderUtils.loadClass(eiValue, factory.getClass());
      }
      catch (ClassNotFoundException e) {
        throw new AnnotationException("Couldn't find endpoint interface " + annotation.getEndpointInterface(), e);
      }
    }

    Map serviceInterfaceBeans = serviceInterface == null ? Collections.EMPTY_MAP : this.ctx.getBeansOfType(serviceInterface);
    if (serviceInterfaceBeans.size() > 0) {
      String serviceName = factory.createServiceName(serviceClass, annotation, annotation.getServiceName());
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
      serviceBean = serviceClass.newInstance();
    }
    return serviceBean;
  }

  //inherited.
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    return delegate.handleRequest(request, response);
  }

  /**
   * Set the view for the wsdl file for this exporter.
   *
   * @param wsdlView The view for the wsdl.
   */
  public void setWsdlView(View wsdlView) {
    this.wsdlView = wsdlView;
  }

  @Override
  public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    super.setApplicationContext(ctx);
    this.ctx = ctx;
  }

  @Override
  public void setServiceFactory(ServiceFactory serviceFactory) {
    assertValid(serviceFactory);
    super.setServiceFactory(serviceFactory);
  }


  /**
   * For some reason, the XFireExporter expects requires the service class to be an interface.  This is
   * inconsistent with the JAXWS spec, so this fixes that inconsistency.
   *
   * @return The service bean.
   */
  @Override
  protected Object getProxyForService() {
    return getServiceBean();
  }

  /**
   * Asserts that the specified service factory is a valid service factory for this exporter.
   *
   * @param serviceFactory The service factory to validate.
   */
  protected void assertValid(ServiceFactory serviceFactory) {
    if (!(serviceFactory instanceof EnunciatedJAXWSServiceFactory)) {
      throw new IllegalArgumentException("Sorry, the service factory must be an instance of EnunciatedJAXWSServiceFactory...");
    }
  }
}
