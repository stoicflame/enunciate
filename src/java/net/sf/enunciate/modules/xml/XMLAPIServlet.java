package net.sf.enunciate.modules.xml;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Serves up the XML API per request.
 *
 * @author Ryan Heaton
 */
public class XMLAPIServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doPost(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String filename = request.getPathInfo();
    if (filename == null) {
      filename = "";
    }

    if (filename.startsWith("/")) {
      filename = filename.substring(1);
    }

    if (filename.trim().length() == 0) {
      //todo: send an index page?
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
    else {
      InputStream xmlResource = getClass().getResourceAsStream("/" + filename);
      if (xmlResource == null) {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      else {
        response.setContentType("text/xml");

        BufferedReader reader = new BufferedReader(new InputStreamReader(xmlResource));
        PrintWriter writer = response.getWriter();
        String line = reader.readLine();
        while (line != null) {
          writer.println(line);
          line = reader.readLine();
        }
        reader.close();
        writer.close();
      }
    }
  }
}
