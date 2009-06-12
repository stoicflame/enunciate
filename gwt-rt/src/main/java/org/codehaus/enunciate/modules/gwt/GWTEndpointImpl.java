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

package org.codehaus.enunciate.modules.gwt;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.codehaus.enunciate.service.DefaultSecurityExceptionChecker;
import org.codehaus.enunciate.service.SecurityExceptionChecker;
import org.codehaus.enunciate.webapp.ComponentPostProcessor;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;

/**
 * Base implementation for a GWT endpoint.
 *
 * @author Ryan Heaton
 */
public abstract class GWTEndpointImpl extends RemoteServiceServlet {

  private final HashMap<String, Method> operationNames2Methods = new HashMap<String, Method>();
  protected Object serviceBean;
  private SecurityExceptionChecker securityChecker = new DefaultSecurityExceptionChecker();

  protected GWTEndpointImpl(Object serviceBean) {
    this.serviceBean = serviceBean;

    for (Method method : getServiceInterface().getMethods()) {
      String operationName = method.getName();
      WebMethod webMethodInfo = method.getAnnotation(WebMethod.class);
      if ((webMethodInfo != null) && (!"".equals(webMethodInfo.operationName()))) {
        operationName = webMethodInfo.operationName();
      }

      this.operationNames2Methods.put(operationName, method);
    }
  }

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);

    ServletContext servletContext = servletConfig.getServletContext();
    ComponentPostProcessor postProcessor = (ComponentPostProcessor) servletContext.getAttribute(ComponentPostProcessor.class.getName());
    if (postProcessor != null) {
      postProcessor.postProcess(this);
    }
  }

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
      GWTMapper paramMapper = GWTMapperIntrospector.getGWTMapperForGWTObject(params[i]);
      if (paramMapper == null) {
        paramMapper = GWTMapperIntrospector.getGWTMapper(paramTypes[i]);
      }
      mappedParams[i] = paramMapper.toJAXB(params[i], mappingContext);
    }

    Object returnValue;
    try {
      returnValue = method.invoke(serviceBean, mappedParams);
    }
    catch (InvocationTargetException e) {
      Throwable targetException = e.getTargetException();
      for (int i = 0; i < method.getExceptionTypes().length; i++) {
        Class exceptionType = method.getExceptionTypes()[i];
        if (exceptionType.isInstance(targetException)) {
          throw (Exception) GWTMapperIntrospector.getGWTMapper(targetException.getClass(), exceptionType, null, null).toGWT(targetException, mappingContext);
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
      returnValue = GWTMapperIntrospector.getGWTMapper(returnValue == null ? null : returnValue.getClass(), method.getGenericReturnType(), null, null).toGWT(returnValue, mappingContext);
    }
    
    return returnValue;
  }

  @Override
  protected void doUnexpectedFailure(Throwable throwable) {
    if ((securityChecker.isAuthenticationFailed(throwable)) || (securityChecker.isAccessDenied(throwable))) {
      //todo: handle the security exception?
      super.doUnexpectedFailure(throwable);
    }
    else {
      super.doUnexpectedFailure(throwable);
    }
  }

  /**
   * Set the security exception checker.
   *
   * @param securityChecker The security exception checker.
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
