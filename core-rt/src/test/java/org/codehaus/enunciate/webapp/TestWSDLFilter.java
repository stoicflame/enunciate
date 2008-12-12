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

package org.codehaus.enunciate.webapp;

import static org.easymock.EasyMock.*;

import junit.framework.TestCase;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Ryan Heaton
 */
public class TestWSDLFilter extends TestCase {

  /**
   * tests the filtering.
   */
  public void testFiltering() throws Exception {
    final ServletContext context = createMock(ServletContext.class);
    WSDLFilter filter = new WSDLFilter() {
      @Override
      public ServletContext getServletContext() {
        return context;
      }
    };
    filter.setAssumedBaseAddress("http://localhost:8080/full");
    String wsdl = "<soap:address location=\"http://localhost:8080/full/soap-services/PersonServiceService\"/>\n" +
      "<somexml xmlns=\"http://localhost:8080/full/myns\"/>";
    HttpServletRequest req = createMock(HttpServletRequest.class);
    HttpServletResponse res = createMock(HttpServletResponse.class);
    FilterChain chain = createMock(FilterChain.class);
    expect(req.getRequestURL()).andReturn(new StringBuffer("http://myhost.com/mycontext/something/test.wsdl"));
    expect(req.getContextPath()).andReturn("/mycontext");
    expect(context.getResourceAsStream("/something/test.wsdl")).andReturn(new ByteArrayInputStream(wsdl.getBytes("utf-8")));
    StringWriter writer = new StringWriter();
    res.setContentType("application/xml");
    expect(res.getWriter()).andReturn(new PrintWriter(writer));
    replay(context, req, res, chain);
    filter.doFilter(req, res, chain);
    verify(context, req, res, chain);
    reset(context, req, res, chain);
    assertEquals("<soap:address location=\"http://myhost.com/mycontext/soap-services/PersonServiceService\"/>\n<somexml xmlns=\"http://localhost:8080/full/myns\"/>", writer.toString().trim());
  }

}
