package net.sf.enunciate.modules.xfire;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * Serves up the XML API per request.
 *
 * @author Ryan Heaton
 */
public class XMLAPIController implements Controller {

  /**
   * Handles a request for a wsdl or schema.
   *
   * @param request  The request.
   * @param response The response.
   * @return null, as the response is written directly to the stream.
   */
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String resource = request.getPathInfo();
    if (resource == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    else {
      if (!resource.startsWith("/")) {
        resource = "/" + resource;
      }

      InputStream in = getClass().getResourceAsStream(resource);
      if (in == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      else {
        response.setContentType("text/xml");
        ServletOutputStream out = response.getOutputStream();
        byte[] buffer = new byte[1024 * 2];
        int len;
        while ((len = in.read(buffer)) > 0) {
          out.write(buffer, 0, len);
        }
        in.close();
        out.close();
      }
    }

    return null;
  }
}
