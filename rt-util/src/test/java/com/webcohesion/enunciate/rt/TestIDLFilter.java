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
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ryan Heaton
 */
public class TestIDLFilter extends TestCase {
  private static final String EXPECTED_HEADER_LINE_1 = "<soap:address location=\"http://myhost.com/mycontext/soap-services/PersonServiceService\"/>";
  private static final String EXPECTED_HEADER_LINE_2 = "<somexml xmlns=\"http://localhost:8080/full/myns\"/>";

  private static final String WSDL_HEADER = "<soap:address location=\"http://localhost:8080/full/soap-services/PersonServiceService\"/>\n" +
    "<somexml xmlns=\"http://localhost:8080/full/myns\"/>";


  /**
   * tests the filtering.
   */
  public void testFiltering() throws Exception {
    final ServletContext context = mock(ServletContext.class);
    IDLFilter filter = new IDLFilter() {
      @Override
      public ServletContext getServletContext() {
        return context;
      }
    };

    filter.setAssumedBaseAddress("http://localhost:8080/full");
    filter.setMatchPrefix(":address location=\"");
    HttpServletRequest req = mock(HttpServletRequest.class);
    HttpServletResponse res = mock(HttpServletResponse.class);
    FilterChain chain = mock(FilterChain.class);
    when(req.getRequestURL()).thenReturn(new StringBuffer("http://myhost.com/mycontext/something/test.wsdl"));
    when(req.getContextPath()).thenReturn("/mycontext");
    when(context.getResourceAsStream("/something/test.wsdl")).thenReturn(new ByteArrayInputStream(WSDL_HEADER.getBytes("utf-8")));
    StringWriter writer = new StringWriter();
    res.setContentType("application/xml");
    when(res.getWriter()).thenReturn(new PrintWriter(writer));

    filter.doFilter(req, res, chain);
    String actualHeader = writer.toString().trim();
    String expectedOut = buildExpectedOut();
    assertEquals(expectedOut, actualHeader);
  }

  /**
   * Windows uses /r/n instead of only /n to write new lines.
   * This builds the expected string so it works right across systems.
   * Or so I hope i didn't break anything
   *
   * @return the expected output using PrintWriter, like the filter does it
   */
  private String buildExpectedOut() {
    StringWriter expectedStringWriter = new StringWriter();
    PrintWriter expectedWriter = new PrintWriter(expectedStringWriter);

    expectedWriter.println(EXPECTED_HEADER_LINE_1);
    expectedWriter.println(EXPECTED_HEADER_LINE_2);
    String expectedHeader = expectedStringWriter.toString().trim();

    return expectedHeader;
  }

}
