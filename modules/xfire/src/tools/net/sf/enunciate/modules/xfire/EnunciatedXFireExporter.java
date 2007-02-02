package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.spring.remoting.XFireExporter;
import org.codehaus.xfire.annotations.AnnotationServiceFactory;
import org.codehaus.xfire.annotations.WebServiceAnnotation;
import org.codehaus.xfire.service.ServiceFactory;
import org.codehaus.xfire.handler.HandlerSupport;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.beans.BeansException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.ArrayList;

/**
 * Exports a SOAP endpoint for XFire.
 * 
 * @author Ryan Heaton
 */
public class EnunciatedXFireExporter extends XFireExporter {

  private ApplicationContext ctx;
  private EnunciatedXFireServletController delegate;
  private View wsdlView = null;

  public void afterPropertiesSet() throws Exception {
    Object serviceBean = loadServiceBean();
    setServiceBean(serviceBean);
    if (serviceBean instanceof HandlerSupport) {
      //set the XFire in/out handlers that may possibly be configured.
      HandlerSupport handlerSupport = (HandlerSupport) serviceBean;
      setInHandlers(handlerSupport.getInHandlers());
      setOutHandlers(handlerSupport.getOutHandlers());
      setFaultHandlers(handlerSupport.getFaultHandlers());
    }

    super.afterPropertiesSet();

    delegate = new EnunciatedXFireServletController(getXfire(), getXFireService().getName(), this.wsdlView);
  }

  /**
   * Attempts to load the service bean by first looking for beans that implement the {@link #getServiceClass() service class}.
   * If there is only one, it will be used.  Otherwise, if there is more than one, it will attempt to find one that is named
   * the same as the service name or fail.  If there are no service beans in the context that can be
   * assigned to the service class, an attempt will be made to instantiate one.
   * 
   * @return The service bean.
   * @throws InstantiationException If an attempt was made to instantiate the bean but it failed.
   * @throws IllegalAccessException If an attempt was made to instantiate the bean but it couldn't get access.
   */
  protected Object loadServiceBean() throws InstantiationException, IllegalAccessException {
    Object serviceBean;
    Class serviceClass = getServiceClass();
    Map serviceClassBeans = this.ctx.getBeansOfType(serviceClass);
    if (serviceClassBeans.size() > 0) {
      EnunciatedJAXWSServiceFactory factory = (EnunciatedJAXWSServiceFactory) getServiceFactory();
      WebServiceAnnotation annotation = factory.getAnnotations().getWebServiceAnnotation(serviceClass);
      String serviceName = factory.createServiceName(serviceClass, annotation, annotation.getServiceName());
      if (serviceClassBeans.containsKey(serviceName)) {
        //first attempt will be to load the bean identified by the service name:
        serviceBean = serviceClassBeans.get(serviceName);
      }
      else if (serviceClassBeans.size() == 1) {
        // not there; use the only one if it exists...
        serviceBean = serviceClassBeans.values().iterator().next();
      }
      else {
        //panic: can't determine the service bean to use.
        ArrayList beanNames = new ArrayList(serviceClassBeans.keySet());
        throw new ApplicationContextException("There are more than one beans of type " + serviceClass.getName() +
          " in the application context " + beanNames + ".  Cannot determine which one to use to handle the soap requests.  " +
          "Either reduce the number of beans of this type to one, or specify which one to use by naming it the name of the service (\"" + serviceName + "\").");
      }
    }
    else {
      //try to instantiate the bean with the class...
      serviceBean = serviceClass.newInstance();
    }
    return serviceBean;
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
  public void setApplicationContext(ApplicationContext ctx) throws BeansException {
    super.setApplicationContext(ctx);
    this.ctx = ctx;
  }

  @Override
  public void setServiceFactory(ServiceFactory serviceFactory) {
    assertValid(serviceFactory);
    super.setServiceFactory(serviceFactory);
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
