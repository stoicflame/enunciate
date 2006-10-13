package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.annotations.AnnotationException;
import org.codehaus.xfire.annotations.WebAnnotations;
import org.codehaus.xfire.annotations.WebServiceAnnotation;
import org.codehaus.xfire.exchange.MessageSerializer;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.jaxws.JAXWSServiceFactory;
import org.codehaus.xfire.jaxws.handler.WebFaultHandler;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.soap.AbstractSoapBinding;

import java.util.List;

/**
 * Annotation service factory that adjusts for XFire noncompliance to the spec that makes the correct
 * service name from a class name.
 *
 * @author Ryan Heaton
 */
public class EnunciatedJAXWSServiceFactory extends JAXWSServiceFactory {

  public EnunciatedJAXWSServiceFactory() {
    this(null);
  }

  public EnunciatedJAXWSServiceFactory(String typeSetId) {
    super(XFireFactory.newInstance().getXFire().getTransportManager());
    ((AegisBindingProvider) getBindingProvider()).setTypeMappingRegistry(new EnunciatedJAXWSTypeRegistry(typeSetId));
  }

  @Override
  protected String createServiceName(Class clazz, WebServiceAnnotation annotation, String current) {
    WebAnnotations webAnnotations = getAnnotations();
    Class endpointInterface = clazz;
    String eiValue = annotation.getEndpointInterface();
    if (eiValue != null && eiValue.length() != 0) {
      try {
        endpointInterface = loadClass(annotation.getEndpointInterface());
        if (!webAnnotations.hasWebServiceAnnotation(endpointInterface)) {
          throw new AnnotationException("Endpoint interface " + endpointInterface.getName() + " does not have a WebService annotation");
        }

        WebServiceAnnotation eiAnnotation = webAnnotations.getWebServiceAnnotation(endpointInterface);
        String serviceName = eiAnnotation.getServiceName();
        if ((serviceName != null) && (serviceName.length() > 0)) {
          return serviceName;
        }
      }
      catch (ClassNotFoundException e) {
        throw new AnnotationException("Couldn't find endpoint interface " +
          annotation.getEndpointInterface(), e);
      }
    }
    else {
      String serviceName = annotation.getServiceName();
      if ((serviceName != null) && (serviceName.length() > 0)) {
        return serviceName;
      }
    }

    return makeServiceNameFromClassName(endpointInterface) + "Service";
  }

  @Override
  protected void registerHandlers(Service service) {
    super.registerHandlers(service);

    List faultHandlers = service.getFaultHandlers();
    for (int i = 0; i < faultHandlers.size(); i++) {
      Object faultHandler = faultHandlers.get(i);
      if (faultHandler instanceof WebFaultHandler) {
        faultHandlers.remove(i);
        faultHandlers.add(i, new EnunciatedJAXWSWebFaultHandler());
      }
    }

    service.setFaultHandlers(faultHandlers);
  }

  /**
   * The serializer for a SOAP message.
   *
   * @param binding The binding.
   * @return The default serializer for the binding.
   */
  @Override
  protected MessageSerializer getSerializer(AbstractSoapBinding binding) {
    return new EnunciatedJAXWSMessageBinding();
  }

  /**
   * Sets up the correct serializer for an SOAP-bound operation
   */
  @Override
  public void createBindingOperation(Service service, AbstractSoapBinding binding, OperationInfo op) {
    super.createBindingOperation(service, binding, op);

    try {
      binding.setSerializer(op, new EnunciatedJAXWSOperationBinding(op));
    }
    catch (XFireFault e) {
      throw new XFireRuntimeException("Error setting the serializer on the operation binding.", e);
    }
  }

}
