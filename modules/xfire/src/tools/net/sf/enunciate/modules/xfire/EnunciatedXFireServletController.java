package net.sf.enunciate.modules.xfire;

import net.sf.enunciate.modules.xml.XMLAPILookup;
import org.codehaus.xfire.XFire;
import org.codehaus.xfire.spring.remoting.XFireServletControllerAdapter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;

/**
 * Xfire servlet controller the redirects to the generated documentation and WSDL.
 *
 * @author Ryan Heaton
 */
public class EnunciatedXFireServletController extends XFireServletControllerAdapter {

  private static final XMLAPILookup XML_API_LOOKUP = XMLAPILookup.load(EnunciatedXFireServletController.class.getResourceAsStream("/xml-api.lookup"));

  public EnunciatedXFireServletController(XFire xfire, QName serviceName) {
    super(xfire, serviceName);
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
    //todo: redirect instead of this....

    String artifact = XML_API_LOOKUP.getArtifactForService(service);
    InputStream wsdl = artifact == null ? null : getClass().getResourceAsStream("/" + artifact);
    if (wsdl != null) {
      response.setContentType("text/xml");
      ServletOutputStream out = response.getOutputStream();
      byte[] buffer = new byte[1024 * 2];
      int len;
      while ((len = wsdl.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      wsdl.close();
      out.close();
    }
    else {
      super.generateWSDL(response, service);
    }
  }
}
