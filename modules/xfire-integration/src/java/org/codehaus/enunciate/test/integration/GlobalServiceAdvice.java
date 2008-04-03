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

package org.codehaus.enunciate.test.integration;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author Ryan Heaton
 */
public class GlobalServiceAdvice implements MethodInterceptor {

  public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    if (("addInfoSet".equals(methodInvocation.getMethod().getName())) && ("SPECIAL2".equals(methodInvocation.getArguments()[0]))) {
      return "intercepted2";
    }
    else if (("deletePerson".equals(methodInvocation.getMethod().getName())) && ("SPECIAL".equals(methodInvocation.getArguments()[0]))) {
      throw new IllegalArgumentException("Illegal delete argument: SPECIAL.");
    }

    return methodInvocation.proceed();

  }
}
