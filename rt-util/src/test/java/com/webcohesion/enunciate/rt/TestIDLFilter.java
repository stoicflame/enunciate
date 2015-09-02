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

package com.webcohesion.enunciate.rt;

import junit.framework.TestCase;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ryan Heaton
 */
public class TestIDLFilter extends TestCase {

  /**
   * tests the filtering.
   */
  public void testFiltering() throws Exception {
    ServletContext context = mock(ServletContext.class);
    FilterConfig filterConfig = mock(FilterConfig.class);
    when(filterConfig.getServletContext()).thenReturn(context);
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    IDLFilter filter = new IDLFilter();

    when(req.getRequestURL()).thenReturn(new StringBuffer("http://myhost.com/mycontext/something/test.wsdl"));
    when(req.getContextPath()).thenReturn("/mycontext");
    when(context.getResourceAsStream("/something/test.wsdl")).thenReturn(getClass().getResourceAsStream("test.wsdl"));
    StringWriter writer = new StringWriter();
    when(res.getWriter()).thenReturn(new PrintWriter(writer));

    filter.init(filterConfig);
    filter.doFilter(req, res, chain);
    String actual = writer.toString();
    assertFalse(actual.contains("http://localhost:8080/base"));
  }

}
