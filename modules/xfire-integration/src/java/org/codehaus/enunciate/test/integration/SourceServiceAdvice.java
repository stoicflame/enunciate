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

package org.codehaus.enunciate.test.integration;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.codehaus.xfire.handler.HandlerSupport;
import org.codehaus.xfire.handler.AbstractHandler;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.exchange.InMessage;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

import java.util.List;
import java.util.Arrays;

/**
 * Advice for a source service.
 *
 * @author Ryan Heaton
 */
public class SourceServiceAdvice extends DelegatingIntroductionInterceptor implements MethodInterceptor, HandlerSupport {

  public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    if (("addInfoSet".equals(methodInvocation.getMethod().getName())) && ("SPECIAL".equals(methodInvocation.getArguments()[0]))) {
      return "intercepted";
    }

    return methodInvocation.proceed();
  }

  public List getInHandlers() {
    return Arrays.asList(this);
  }

  public List getOutHandlers() {
    return null;
  }

  public List getFaultHandlers() {
    return null;
  }

  public void invoke(MessageContext context) throws Exception {
    InMessage inMessage = context.getInMessage();
    //todo: I don't know... do some stuff here to prove this code gets executed.
  }
}
