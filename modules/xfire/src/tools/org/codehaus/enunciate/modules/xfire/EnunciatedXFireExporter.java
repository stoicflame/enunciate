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

package org.codehaus.enunciate.modules.xfire;

import org.codehaus.xfire.handler.HandlerSupport;
import org.codehaus.xfire.service.ServiceFactory;
import org.codehaus.xfire.spring.remoting.XFireExporter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Exports a SOAP endpoint for XFire.
 * 
 * @author Ryan Heaton
 */
public class EnunciatedXFireExporter extends XFireExporter {

  private EnunciatedXFireServletController delegate;
  private View wsdlView = null;

  public void afterPropertiesSet() throws Exception {
    if (getServiceBean() instanceof HandlerSupport) {
      //set the XFire in/out handlers that may possibly be configured.
      HandlerSupport handlerSupport = (HandlerSupport) getServiceBean();
      setInHandlers(handlerSupport.getInHandlers());
      setOutHandlers(handlerSupport.getOutHandlers());
      setFaultHandlers(handlerSupport.getFaultHandlers());
    }

    super.afterPropertiesSet();

    delegate = new EnunciatedXFireServletController(getXfire(), getXFireService().getName(), this.wsdlView);
  }

  //inherited.
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    return delegate.handleRequest(request, response);
  }

  /**
   * Set the view for the wsdl file for this exporter.
   *
   * @param wsdlView The view for the wsdl.
   */
  public void setWsdlView(View wsdlView) {
    this.wsdlView = wsdlView;
  }

  @Override
  public void setServiceFactory(ServiceFactory serviceFactory) {
    assertValid(serviceFactory);
    super.setServiceFactory(serviceFactory);
  }


  /**
   * For some reason, the XFireExporter expects requires the service class to be an interface.  This is
   * inconsistent with the JAXWS spec, so this fixes that inconsistency.
   *
   * @return The service bean.
   */
  @Override
  protected Object getProxyForService() {
    return getServiceBean();
  }

  /**
   * Asserts that the specified service factory is a valid service factory for this exporter.
   *
   * @param serviceFactory The service factory to validate.
   */
  protected void assertValid(ServiceFactory serviceFactory) {
    if (!(serviceFactory instanceof EnunciatedJAXWSServiceFactory)) {
      throw new IllegalArgumentException("Sorry, the service factory must be an instance of EnunciatedJAXWSServiceFactory...");
    }
  }

}
