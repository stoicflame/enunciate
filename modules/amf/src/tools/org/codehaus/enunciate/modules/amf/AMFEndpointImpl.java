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

import org.springframework.context.ApplicationContextException;
import org.springframework.util.ClassUtils;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
public abstract class AMFEndpointImpl {

  private final HashMap<String, Method> operationNames2Methods = new HashMap<String, Method>();
  private Object serviceBean;

  protected AMFEndpointImpl() {
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

    this.serviceBean = ApplicationContextFilter.loadServiceBean(serviceClass, serviceInterface);
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

  protected final Object invokeOperation(String operationName, Object... params) throws Exception {
    Method method = this.operationNames2Methods.get(operationName);
    if (method == null) {
      throw new NoSuchMethodError("No such method: " + operationName);
    }
    AMFMappingContext mappingContext = new AMFMappingContext();

    Type[] paramTypes = method.getGenericParameterTypes();
    if (paramTypes.length != params.length) {
      throw new IllegalArgumentException(String.format("Wrong number of parameters for operation '%s'.  Expected %s, got %s.", operationName, paramTypes.length, params.length));
    }

    Object[] mappedParams = new Object[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      mappedParams[i] = AMFMapperIntrospector.getAMFMapper(paramTypes[i], null, null).toJAXB(params[i], mappingContext);
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
          throw (Exception) AMFMapperIntrospector.getAMFMapper(exceptionType, null, null).toAMF(targetException, mappingContext);
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
      returnValue = AMFMapperIntrospector.getAMFMapper(method.getGenericReturnType(), null, null).toAMF(returnValue, mappingContext);
    }

    return returnValue;
  }

  /**
   * Get the class of the service implementation that will support this AMF endpoint.
   *
   * @return the class of the service implementation that will support this AMF endpoint.
   */
  protected abstract Class getServiceClass();

}
