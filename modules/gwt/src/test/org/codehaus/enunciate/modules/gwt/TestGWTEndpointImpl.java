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

import junit.framework.TestCase;
import org.codehaus.enunciate.service.DefaultEnunciateServiceFactory;
import org.codehaus.enunciate.service.EnunciateServiceFactory;
import org.codehaus.enunciate.service.SecurityExceptionChecker;
import org.codehaus.enunciate.service.DefaultSecurityExceptionChecker;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import static org.easymock.EasyMock.*;

import javax.xml.ws.Holder;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import java.util.UUID;

/**
 * @author Ryan Heaton
 */
public class TestGWTEndpointImpl extends TestCase {

  /**
   * Tests invoking an operation on a GWT endpoint.
   */
  public void testInvokeOperation() throws Exception {
    final Holder<Integer> counter = new Holder<Integer>(0);
    final ServletConfig servletConfig = createMock(ServletConfig.class);
    GWTEndpointImpl impl = new GWTEndpointImpl() {

      @Override
      public ServletConfig getServletConfig() {
        return servletConfig;
      }

      @Override
      protected ApplicationContext loadAppContext() {
        return new GenericApplicationContext();
      }

      @Override
      protected EnunciateServiceFactory loadServiceFactory(ApplicationContext applicationContext) throws ServletException {
        return new DefaultEnunciateServiceFactory() {
          @Override
          public Object getInstance(Object impl, Class... interfaces) {
            ProxyFactory proxyFactory = new ProxyFactory(impl);
            proxyFactory.setInterfaces(interfaces);
            proxyFactory.addAdvice(new MethodInterceptor() {
              public Object invoke(MethodInvocation methodInvocation) throws Throwable {
                counter.value = counter.value + 1;
                return methodInvocation.proceed();
              }
            });
            return proxyFactory.getProxy();
          }
        };
      }


      protected Class getServiceClass() {
        return BeansServiceImpl.class;
      }

      @Override
      protected SecurityExceptionChecker loadSecurityChecker(ApplicationContext applicationContext) {
        return new DefaultSecurityExceptionChecker();
      }
    };
    replay(servletConfig);
    impl.init();
    GWTBeanOneDotOne oneDotOne = new GWTBeanOneDotOne();
    String uuidValue = UUID.randomUUID().toString();
    oneDotOne.setProperty1(uuidValue);
    GWTBeanOne result = (GWTBeanOne) impl.invokeOperation("getSomething", "idOne", oneDotOne, 8);
    assertEquals("idOne", result.getProperty1());
    assertEquals(8, result.getProperty2());
    assertEquals(uuidValue, result.getProperty4().getProperty1());
    result = (GWTBeanOne) impl.invokeOperation("hooHahHah", "idOne", oneDotOne, 8);
    assertEquals("idOnehah", result.getProperty1());
    assertEquals(9, result.getProperty2());
    assertEquals(uuidValue, result.getProperty4().getProperty1());
    impl.invokeOperation("doSomethingVoid");
    try {
      impl.invokeOperation("throwAnException", (int) 555, (byte) 8, new Character('h'), "this is my message");
      fail("should have thrown a GWTBeansServiceException");
    }
    catch (GWTBeansServiceException ex) {
      assertEquals("this is my message", ex.getMessage());
      assertEquals(555, ex.getProperty1());
      assertEquals((byte) 8, ex.getProperty2());
      assertEquals(new Character('h'), ex.getProperty3());
    }
    assertEquals(new Integer(4), counter.value);
  }

}
