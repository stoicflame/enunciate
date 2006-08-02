package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.spring.remoting.XFireExporter;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Ryan Heaton
 */
public class EnunciatedXFireExporter extends XFireExporter {

  private EnunciatedXFireServletController delegate;

  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    delegate = new EnunciatedXFireServletController(getXfire(), getXFireService().getName());
  }

  //inherited.
  public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    return delegate.handleRequest(request, response);
  }
}
