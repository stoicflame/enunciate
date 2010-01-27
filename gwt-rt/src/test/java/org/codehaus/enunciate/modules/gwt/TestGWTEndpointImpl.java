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

import junit.framework.TestCase;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

import javax.servlet.ServletConfig;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ryan Heaton
 */
public class TestGWTEndpointImpl extends TestCase {

  /**
   * tests escaping a line
   */
  public void testLineEscape() throws Exception {
    assertEquals("hi%%20", "hi%20".replaceAll("%", "%%"));
  }

  /**
   * tests parsing a gwt version.
   */
  public void testParseGWTVersion() throws Exception {
    Pattern pattern = Pattern.compile("[\\d\\.]+");
    Matcher matcher = pattern.matcher("Google Web Toolkit 2.0.0");
    assertTrue(matcher.find());
    assertEquals("2.0.0", matcher.group());
    matcher = pattern.matcher("Google Web Toolkit 1.6.0");
    assertTrue(matcher.find());
    assertEquals("1.6.0", matcher.group());
  }

  /**
   * Tests invoking an operation on a GWT endpoint.
   */
  public void testInvokeOperation() throws Exception {
    final ServletConfig servletConfig = createMock(ServletConfig.class);
    org.codehaus.enunciate.modules.gwt.GWTEndpointImpl impl = new org.codehaus.enunciate.modules.gwt.GWTEndpointImpl(new BeansServiceImpl()) {

      @Override
      public ServletConfig getServletConfig() {
        return servletConfig;
      }

      protected Class getServiceInterface() {
        return BeansService.class;
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
  }

}
