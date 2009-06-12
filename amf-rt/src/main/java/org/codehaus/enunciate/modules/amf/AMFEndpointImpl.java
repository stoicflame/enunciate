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

package org.codehaus.enunciate.modules.amf;

import flex.messaging.security.SecurityException;
import flex.messaging.util.PropertyStringResourceLoader;
import org.codehaus.enunciate.service.DefaultSecurityExceptionChecker;
import org.codehaus.enunciate.service.SecurityExceptionChecker;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Base class for an AMF endpoint.
 *
 * @author Ryan Heaton
 */
public abstract class AMFEndpointImpl {

  private SecurityExceptionChecker securityChecker = new DefaultSecurityExceptionChecker();
  private final HashMap<String, Method> operationNames2Methods = new HashMap<String, Method>();
  protected Object serviceBean;

  protected AMFEndpointImpl(Object serviceBean) {
    this.serviceBean = serviceBean;

    this.operationNames2Methods.clear();

    for (Method method : getServiceInterface().getMethods()) {
      String operationName = method.getName();
      WebMethod webMethodInfo = method.getAnnotation(WebMethod.class);
      if ((webMethodInfo != null) && (!"".equals(webMethodInfo.operationName()))) {
        operationName = webMethodInfo.operationName();
      }

      this.operationNames2Methods.put(operationName, method);
    }
  }

  /**
   * Invoke an operation on the underlying service bean. This will transform each of its AMF object parameters to JAXB object parameters and then invoke the
   * JAX-WS service bean.  The result will be transformed into an AMF object before being returned.
   *
   * @param operationName The operation name.
   * @param params        The (AMF) parameters.
   * @return The (AMF) result of the invocation.
   */
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
      AMFMapper mapper;
      if (params[i] instanceof AMFMapperAware) {
        mapper = ((AMFMapperAware) params[i]).loadAMFMapper();
      }
      else {
        mapper = AMFMapperIntrospector.getAMFMapper(paramTypes[i]);
      }
      mappedParams[i] = mapper.toJAXB(params[i], mappingContext);
    }

    Object returnValue;
    try {
      returnValue = method.invoke(serviceBean, mappedParams);
    }
    catch (InvocationTargetException e) {
      Throwable targetException = e.getTargetException();
      if ((securityChecker.isAuthenticationFailed(targetException)) || (securityChecker.isAccessDenied(targetException))) {
        flex.messaging.security.SecurityException se = new SecurityException(new PropertyStringResourceLoader("flex.messaging.vendors"));
        se.setMessage(targetException.getMessage());
        throw se;
      }
      else {
        for (int i = 0; i < method.getExceptionTypes().length; i++) {
          Class exceptionType = method.getExceptionTypes()[i];
          if (exceptionType.isInstance(targetException)) {
            throw (Exception) AMFMapperIntrospector.getAMFMapper(targetException.getClass(), exceptionType).toAMF(targetException, mappingContext);
          }
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
      returnValue = AMFMapperIntrospector.getAMFMapper(returnValue != null ? returnValue.getClass() : null, method.getGenericReturnType()).toAMF(returnValue, mappingContext);
    }

    return returnValue;
  }

  /**
   * Set the security checker for this endpoint.
   *
   * @param securityChecker The security checker.
   */
  @Resource
  public void setSecurityChecker(SecurityExceptionChecker securityChecker) {
    this.securityChecker = securityChecker;
  }

  /**
   * The service interface.
   *
   * @return The service interface.
   */
  protected abstract Class getServiceInterface();
}
