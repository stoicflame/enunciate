package org.codehaus.enunciate.modules.jersey;

import com.sun.jersey.api.core.DefaultResourceConfig;
import junit.framework.TestCase;

import javax.servlet.http.HttpServletRequest;

import static org.easymock.EasyMock.*;

/**
 * @author Ryan Heaton
 */
public class TestJerseyAdaptedHttpServletRequest extends TestCase {

  /**
   * tests adapting a request with a space in it.
   */
  public void testAdaptRequestWithSpaceInIt() throws Exception {
    DefaultResourceConfig rc = new DefaultResourceConfig();
    HttpServletRequest req = createMock(HttpServletRequest.class);
    expect(req.getRequestURI()).andReturn("/tix/tcb/gqi3/rest/object/lo%20t1");

    replay(req);
    new JerseyAdaptedHttpServletRequest(req, null);
    verify(req);
    reset(req);

  }

}
