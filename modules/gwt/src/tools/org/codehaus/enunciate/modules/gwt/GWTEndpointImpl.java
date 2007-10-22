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

package org.codehaus.enunciate.modules.gwt;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.codehaus.enunciate.service.DefaultEnunciateServiceFactory;
import org.codehaus.enunciate.service.EnunciateServiceFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public abstract class GWTEndpointImpl extends RemoteServiceServlet {

  private final HashMap<String, Method> operationNames2Methods = new HashMap<String, Method>();
  private Object serviceBean;

  protected final Object invokeOperation(String operationName, Object... params) throws Exception {
    Method method = this.operationNames2Methods.get(operationName);
    if (method == null) {
      throw new NoSuchMethodError("No such method: " + operationName);
    }
    GWTMappingContext mappingContext = new GWTMappingContext();

    Type[] paramTypes = method.getGenericParameterTypes();
    if (paramTypes.length != params.length) {
      throw new IllegalArgumentException(String.format("Wrong number of parameters for operation '%s'.  Expected %s, got %s.", operationName, paramTypes.length, params.length));
    }

    Object[] mappedParams = new Object[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      mappedParams[i] = GWTMapperIntrospector.getGWTMapper(paramTypes[i], null).toJAXB(params[i], mappingContext);
    }

    Object returnValue;
    try {
      returnValue = method.invoke(serviceBean, mappedParams);
    }
    catch (IllegalAccessException e) {
      throw e;
    }
    catch (IllegalArgumentException e) {
      throw e;
    }
    catch (InvocationTargetException e) {
      Throwable targetException = e.getTargetException();
      for (int i = 0; i < method.getExceptionTypes().length; i++) {
        Class exceptionType = method.getExceptionTypes()[i];
        if (exceptionType.isInstance(targetException)) {
          throw (Exception) GWTMapperIntrospector.getGWTMapper(exceptionType, null).toGWT(targetException, mappingContext);
        }
      }

      if (targetException instanceof Error) {
        throw (Error) targetException;
      }
      else {
        throw (Exception) targetException;
      }
    }

    if (method.getReturnType() != Void.TYPE) {
      returnValue = GWTMapperIntrospector.getGWTMapper(method.getGenericReturnType(), null).toGWT(returnValue, mappingContext);
    }
    
    return returnValue;
  }


  /**
   * Load the endpoint bean the conforms to the specified endpoint class.  And fill in the
   * operation names-to-methods map.
   */
  @Override
  public final void init() throws ServletException {
    // load the service class.
    Class serviceClass = getServiceClass();
    Class serviceInterface = serviceClass;
    WebService wsInfo = (WebService) serviceInterface.getAnnotation(WebService.class);
    if (wsInfo == null) {
      throw new ApplicationContextException("Can't find the @javax.jws.WebService annotation on " + getServiceClass().getName());
    }

    String eiValue = wsInfo.endpointInterface();
    if (eiValue != null && eiValue.length() > 0) {
      try {
        serviceInterface = ClassUtils.forName(eiValue);
        wsInfo = (WebService) serviceInterface.getAnnotation(WebService.class);
        if (wsInfo == null) {
          throw new ApplicationContextException("No @javax.jws.WebService annotation on service interface " + serviceInterface.getName());
        }
      }
      catch (ClassNotFoundException e) {
        throw new ApplicationContextException("Couldn't find endpoint interface " + wsInfo.endpointInterface(), e);
      }
    }

    ApplicationContext applicationContext = loadAppContext();
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
        throw new ApplicationContextException("Unable to create an instance of " + getServiceClass().getName(), e);
      }
    }

    if (serviceInterface.isInterface()) {
      serviceBean = loadServiceFactory(applicationContext).getInstance(serviceBean, serviceInterface);
    }

    this.operationNames2Methods.clear();

    for (Method method : serviceInterface.getMethods()) {
      String operationName = method.getName();
      WebMethod webMethodInfo = method.getAnnotation(WebMethod.class);
      if ((webMethodInfo != null) && (!"".equals(webMethodInfo.operationName()))) {
        operationName = webMethodInfo.operationName();
      }

      this.operationNames2Methods.put(operationName, method);
    }
  }

  /**
   * Loads the app context for this servlet.
   *
   * @return The app context for this servlet.
   */
  protected ApplicationContext loadAppContext() {
    WebApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
    //if spring isn't loaded (e.g. we're running from the GWT shell), just return an empty context.
    return appContext == null ? new GenericApplicationContext() : appContext;
  }

  /**
   * Loads the service factory from the application context.
   * @param applicationContext The app context.
   * @return The service factory.
   */
  protected EnunciateServiceFactory loadServiceFactory(ApplicationContext applicationContext) throws ServletException {
    EnunciateServiceFactory enunciateServiceFactory = new DefaultEnunciateServiceFactory();
    String serviceFactoryName = getServletConfig().getInitParameter("enunciateServiceFactoryBeanName");
    if (serviceFactoryName != null) {
      enunciateServiceFactory = (EnunciateServiceFactory) applicationContext.getBean(serviceFactoryName);
    }
    else {
      Map factories = BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, EnunciateServiceFactory.class);
      if (factories.size() > 1) {
        throw new ServletException("Unable to determine which enunciate service factory to use.  Please disambiguate with a 'enunciateServiceFactoryBeanName' servlet parameter.");
      }
      else if (factories.size() == 1) {
        enunciateServiceFactory = (EnunciateServiceFactory) factories.values().iterator().next();
      }
    }
    return enunciateServiceFactory;
  }

  /**
   * Get the class of the service implementation that will support this GWT endpoint.
   *
   * @return the class of the service implementation that will support this GWT endpoint.
   */
  protected abstract Class getServiceClass();

}
