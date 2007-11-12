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

import org.granite.messaging.amf.io.util.DefaultConverter;
import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.config.flex.Destination;
import org.granite.util.StringUtil;

import java.util.Map;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author Ryan Heaton
 */
public class EnunciateConverter extends DefaultConverter {

  /**
   * The enunciate converter expects to be passed a {@link ServiceBean}.
   *
   * @param headers     The headers of the invocation.
   * @param destination The destination of the invocation.
   * @param serviceBean The service bean.
   * @param methodName  The method name.
   * @param params      The parameters.
   * @return the invocation context.
   */
  @Override
  public ServiceInvocationContext findServiceMethod(Map<String, Object> headers, Destination destination, Object serviceBean, String methodName, Object[] params) throws NoSuchMethodException {
    Method serviceMethod = null;
    Class serviceClass = ((ServiceBean) serviceBean).getServiceInterface();
    if (params == null || params.length == 0) {
      serviceMethod = serviceClass.getMethod(methodName);
    }
    else {
      for (Method method : serviceClass.getMethods()) {
        if (!methodName.equals(method.getName())) {
          continue;
        }

        Type[] paramTypes = method.getGenericParameterTypes();
        if (paramTypes.length != params.length) {
          continue;
        }

        if (canConvertForMethodInvocation(params, paramTypes)) {
          serviceMethod = method;
          break;
        }
      }
    }

    if (serviceMethod == null) {
      throw new NoSuchMethodException(serviceClass.getName() + '.' + methodName + StringUtil.toString(params));
    }

    params = convertForDeserialization(params, serviceMethod.getGenericParameterTypes());
    return new ServiceInvocationContext(headers, destination, ((ServiceBean) serviceBean).getBean(), serviceMethod, params);
  }
}
