package net.sf.enunciate.modules.rest;

import junit.framework.TestCase;
import net.sf.enunciate.rest.annotations.VerbType;
import static org.easymock.EasyMock.*;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class TestRESTController extends TestCase {

  /**
   * tests the initialization of the application context.
   */
  public void testInitApplicationContext() throws Exception {
    RESTController controller = new RESTController();
    controller.setEndpoints(new Object[]{new EndpointOneImpl(), new EndpointTwo(), new EndpointThreeImpl()});
    controller.initApplicationContext();

    String[] nouns = {"one", "two", "three", "four", "five", "six"};
    Map<String, RESTResource> resources = new HashMap<String, RESTResource>(controller.getRESTResources());
    for (String noun : nouns) {
      RESTResource resource = resources.remove(noun);
      assertNotNull(resource);

      for (VerbType verbType : VerbType.values()) {
        RESTOperation operation = resource.getOperation(verbType);
        assertNotNull(resource.toString() + " does not contain an operation for " + verbType + ".", operation);
      }
    }

    assertTrue(resources.isEmpty());

    //now we want to make sure that we can advise the impls...
    ProxyFactoryBean advisedEndpoint = new ProxyFactoryBean();
    advisedEndpoint.setTarget(new EndpointOneImpl());
    advisedEndpoint.setProxyInterfaces(new String[]{"net.sf.enunciate.modules.rest.EndpointOne"});
    controller = new RESTController();
    controller.setEndpoints(new Object[]{advisedEndpoint});
    controller.initApplicationContext();
    nouns = new String[]{"one", "two"};
    resources = new HashMap<String, RESTResource>(controller.getRESTResources());
    for (String noun : nouns) {
      RESTResource resource = resources.remove(noun);
      assertNotNull(resource);

      for (VerbType verbType : VerbType.values()) {
        RESTOperation operation = resource.getOperation(verbType);
        assertNotNull(operation);
      }
    }

    assertTrue(resources.isEmpty());
  }

  /**
   * Tests handling that the noun and proper noun are property extracted from the request.
   */
  public void testHandleRequestInternal() throws Exception {
    RESTController controller = new RESTController() {
      @Override
      protected ModelAndView handleRESTOperation(String noun, String properNoun, VerbType verb, HttpServletRequest request, HttpServletResponse response) throws Exception {
        request.setAttribute(noun, properNoun);
        request.setAttribute("verb", verb);
        return null;
      }
    };
    controller.setSubcontext("subcontext");

    HttpServletRequest request = createMock(HttpServletRequest.class);
    HttpServletResponse response = createMock(HttpServletResponse.class);
    expect(request.getRequestURI()).andReturn("http://localhost:8080/context/subcontext/mynoun/mypropernoun");
    expect(request.getMethod()).andReturn("GET");
    request.setAttribute("mynoun", "mypropernoun");
    request.setAttribute("verb", VerbType.read);
    replay(request, response);
    controller.handleRequestInternal(request, response);
    verify(request, response);

    reset(request, response);
    expect(request.getRequestURI()).andReturn("http://localhost:8080/some/nested/weird/context/subcontext/mynoun/mypropernoun");
    expect(request.getMethod()).andReturn("PUT");
    request.setAttribute("mynoun", "mypropernoun");
    request.setAttribute("verb", VerbType.create);
    replay(request, response);
    controller.handleRequestInternal(request, response);
    verify(request, response);

    reset(request, response);
    expect(request.getRequestURI()).andReturn("http://localhost:8080/context/subcontext/mynoun/");
    expect(request.getMethod()).andReturn("POST");
    request.setAttribute("mynoun", null);
    request.setAttribute("verb", VerbType.update);
    replay(request, response);
    controller.handleRequestInternal(request, response);
    verify(request, response);

    reset(request, response);
    expect(request.getRequestURI()).andReturn("http://localhost:8080/context/subcontext/mynoun");
    expect(request.getMethod()).andReturn("DELETE");
    request.setAttribute("mynoun", null);
    request.setAttribute("verb", VerbType.delete);
    replay(request, response);
    controller.handleRequestInternal(request, response);
    verify(request, response);

    reset(request, response);
    expect(request.getRequestURI()).andReturn("http://localhost:8080/context/subcontext/");
    expect(request.getMethod()).andReturn("GET");
    request.setAttribute(null, null);
    request.setAttribute("verb", VerbType.read);
    replay(request, response);
    controller.handleRequestInternal(request, response);
    verify(request, response);

    reset(request, response);
    expect(request.getRequestURI()).andReturn("http://localhost:8080/context/some/really/strange/requst");
    expect(request.getMethod()).andReturn("GET");
    request.setAttribute(null, null);
    request.setAttribute("verb", VerbType.read);
    replay(request, response);
    controller.handleRequestInternal(request, response);
    verify(request, response);
  }

  /**
   * tests the handleRESTOperation method.
   */
  public void testHandleRESTOperation() throws Exception {
    RESTController controller = new RESTController();
    controller.setEndpoints(new Object[]{new MockRESTEndpoint()});
    controller.initApplicationContext();

    HttpServletRequest request = createMock(HttpServletRequest.class);
    HttpServletResponse response = createMock(HttpServletResponse.class);
    response.sendError(HttpServletResponse.SC_NOT_FOUND, "A REST resource must be specified.");
    replay(request, response);
    controller.handleRESTOperation(null, null, null, request, response);
    verify(request, response);
    reset(request, response);

    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown REST resource: unknown");
    replay(request, response);
    controller.handleRESTOperation("unknown", null, null, request, response);
    verify(request, response);
    reset(request, response);

    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Unsupported verb: " + VerbType.create);
    replay(request, response);
    controller.handleRESTOperation("example", null, VerbType.create, request, response);
    verify(request, response);
    reset(request, response);

    JAXBContext context = JAXBContext.newInstance(RootElementExample.class);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    context.createMarshaller().marshal(new RootElementExample(), bytes);
    expect(request.getParameterValues("arg2")).andReturn(new String[] {"9999"});
    expect(request.getParameterValues("arg3")).andReturn(new String[] {"value1", "value2"});
    expect(request.getInputStream()).andReturn(new ByteArrayServletInputStream(bytes.toByteArray()));
    replay(request, response);
    ModelAndView modelAndView = controller.handleRESTOperation("example", "id", VerbType.update, request, response);
    verify(request, response);
    RESTResultView view = (RESTResultView) modelAndView.getView();
    assertNotNull(view.getResult());
    assertTrue(view.getResult() instanceof RootElementExample);
    reset(request, response);
  }

  private static class ByteArrayServletInputStream extends ServletInputStream {

    private final ByteArrayInputStream stream;

    public ByteArrayServletInputStream(byte[] bytes) {
      stream = new ByteArrayInputStream(bytes);
    }

    public int read() throws IOException {
      return stream.read();
    }
  }


}
