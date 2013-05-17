package org.codehaus.enunciate.modules.gwt;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.codehaus.enunciate.service.SecurityExceptionChecker;

public class TestGWTEndpointImplExceptions extends TestCase {

  private ServletConfig servletConfig;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private SecurityExceptionChecker exceptionChecker;

  public void setUp() throws IOException {
    servletConfig = createMock(ServletConfig.class);
    exceptionChecker = createMock(SecurityExceptionChecker.class);
    request = createMock(HttpServletRequest.class);
    response = createMock(HttpServletResponse.class);
    response.setContentLength(0);
    response.setContentType((String) anyObject());
    response.setHeader((String) anyObject(), (String) anyObject());
    expect(response.getWriter()).andReturn(new PrintWriter(new ByteArrayOutputStream()));
    expect(response.getOutputStream()).andReturn(new ServletOutputStream() {
      @Override
      public void write(int b) throws IOException {
      }
    });
  }

  public void testPost_successful() throws IOException {
    response.setStatus(200);
    doPost(false);
  }

  public void testPost_unexpectedException() throws IOException {
    response.setStatus(500);
    expect(exceptionChecker.isAccessDenied(anyThrowable())).andReturn(false);
    expect(exceptionChecker.isAuthenticationFailed(anyThrowable())).andReturn(false);
    doPost(true);
  }

  public void testPost_accessDeniedException() throws IOException {
    response.setStatus(403);
    expect(exceptionChecker.isAccessDenied(anyThrowable())).andReturn(true);
    expect(exceptionChecker.isAuthenticationFailed(anyThrowable())).andReturn(false);
    doPost(true);
  }

  public void testPost_authenticationFailedException() throws IOException {
    response.setStatus(401);
    expect(exceptionChecker.isAccessDenied(anyThrowable())).andReturn(false);
    expect(exceptionChecker.isAuthenticationFailed(anyThrowable())).andReturn(true);
    doPost(true);
  }

  private Throwable anyThrowable() {
    return (Throwable) anyObject();
  }

  private void doPost(boolean processThrows) {
    replay(response, exceptionChecker);
    GWTEndpointImpl impl = new MockGWTEndpointImpl(processThrows);
    impl.doPost(request, response);
  }

  private final class MockGWTEndpointImpl extends GWTEndpointImpl {
    private static final long serialVersionUID = 1L;
    private final boolean processThrows;

    private MockGWTEndpointImpl(boolean processThrows) {
      super(null);
      this.processThrows = processThrows;
      this.setSecurityChecker(exceptionChecker);
    }

    @Override
    public ServletConfig getServletConfig() {
      return servletConfig;
    }

    protected Class getServiceInterface() {
      return BeansService.class;
    }

    @Override
    protected String readContent(HttpServletRequest request) {
      return "";
    }

    @Override
    public ServletContext getServletContext() {
      return createMock(ServletContext.class);
    }

    @Override
    public String processCall(String payload) {
      if (processThrows) {
        throw new RuntimeException();
      } else {
        return "";
      }
    }
  }

}
