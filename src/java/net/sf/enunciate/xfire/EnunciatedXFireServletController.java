package net.sf.enunciate.xfire;

import org.codehaus.xfire.XFire;
import org.codehaus.xfire.transport.http.XFireServletController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Xfire servlet controller the redirects to the generated documentation and WSDL.
 *
 * @author Ryan Heaton
 */
public class EnunciatedXFireServletController extends XFireServletController {

  public EnunciatedXFireServletController(XFire xfire) {
    super(xfire);
  }

  /**
   * Redirects the response to the generated documenatation for the specified service.
   *
   * @param response    The response.
   * @param serviceName The name of the service.
   */
  @Override
  protected void generateService(HttpServletResponse response, String serviceName) throws ServletException, IOException {
    //todo: redirect to the generated service documentation.
    super.generateService(response, serviceName);
  }

  /**
   * Redirects the response to the generated documentation for the services.
   *
   * @param request  The request.
   * @param response The response.
   */
  @Override
  protected void generateServices(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    //todo: redirect to the generated services documentation.
    super.generateServices(request, response);
  }

  /**
   * Writes out the WSDL for the specified service to the response.
   *
   * @param response The response.
   * @param service  The service name.
   */
  @Override
  protected void generateWSDL(HttpServletResponse response, String service) throws ServletException, IOException {
    //todo: write out the generated WSDL.
    super.generateWSDL(response, service);
  }
}
