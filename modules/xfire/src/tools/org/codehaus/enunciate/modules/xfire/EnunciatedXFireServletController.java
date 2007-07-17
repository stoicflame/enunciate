/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.modules.xfire;

import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.XFire;
import org.codehaus.xfire.soap.SoapConstants;
import org.codehaus.xfire.spring.remoting.XFireServletControllerAdapter;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.HashMap;

/**
 * Xfire servlet controller the redirects to the generated documentation and WSDL.
 *
 * @author Ryan Heaton
 */
public class EnunciatedXFireServletController extends XFireServletControllerAdapter {

  private final View wsdlView;

  public EnunciatedXFireServletController(XFire xfire, QName serviceName, View wsdlView) {
    super(xfire, serviceName);
    this.wsdlView = wsdlView;
  }

  /**
   * Redirects the response to the generated documenatation for the specified service.
   *
   * @param response    The response.
   * @param serviceName The name of the service.
   */
  @Override
  protected void generateService(HttpServletResponse response, String serviceName) throws ServletException, IOException {
    response.sendRedirect("/");
  }

  /**
   * Redirects the response to the generated documentation for the services.
   *
   * @param request  The request.
   * @param response The response.
   */
  @Override
  protected void generateServices(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.sendRedirect("/");
  }

  /**
   * Writes out the WSDL for the specified service to the response.
   *
   * @param response The response.
   * @param service  The service name.
   */
  @Override
  protected void generateWSDL(HttpServletResponse response, String service) throws ServletException, IOException {
    if (wsdlView == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "WSDL not found for service: " + service);
    }
    else {
      try {
        HashMap model = new HashMap();
        //the only thing I might have in the model at this point is the service name...
        model.put("service", service);
        wsdlView.render(model, getRequest(), response);
      }
      catch (ServletException e) {
        throw e;
      }
      catch (IOException e) {
        throw e;
      }
      catch (Exception e) {
        response.sendError(500, e.getMessage());
      }
    }
  }

  @Override
  protected MessageContext createMessageContext(HttpServletRequest request, HttpServletResponse response, String service) {
    MessageContext context = super.createMessageContext(request, response, service);

    String acceptHeader = request.getHeader("Accept");

    //is "contains" good enough or do I actually need to parse it?
    boolean mtomEnabled = ((acceptHeader != null) && (acceptHeader.toLowerCase().contains("application/xop+xml")));
    context.setProperty(SoapConstants.MTOM_ENABLED, String.valueOf(mtomEnabled));
    
    return context;
  }
}
