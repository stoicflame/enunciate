package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.spring.remoting.XFireExporter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Ryan Heaton
 */
public class EnunciatedXFireExporter extends XFireExporter {

  private EnunciatedXFireServletController delegate;
  private View wsdlView = null;

  public void afterPropertiesSet() throws Exception {
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
}
