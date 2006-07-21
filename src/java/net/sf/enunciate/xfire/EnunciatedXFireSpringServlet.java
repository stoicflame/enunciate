package net.sf.enunciate.xfire;

import org.codehaus.xfire.spring.XFireSpringServlet;
import org.codehaus.xfire.transport.http.XFireServletController;

import javax.servlet.ServletException;

/**
 * XFire servlet that will display the enunciated WSDL, documents.
 *
 * @author Ryan Heaton
 */
public class EnunciatedXFireSpringServlet extends XFireSpringServlet {

  @Override
  public XFireServletController createController() throws ServletException {
    return new EnunciatedXFireServletController(getXFire());
  }
}
