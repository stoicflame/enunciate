package net.sf.enunciate.modules.xfire;

import org.codehaus.xfire.annotations.AnnotationException;
import org.codehaus.xfire.annotations.AnnotationServiceFactory;
import org.codehaus.xfire.annotations.WebAnnotations;
import org.codehaus.xfire.annotations.WebServiceAnnotation;
import org.codehaus.xfire.service.binding.BindingProvider;
import org.codehaus.xfire.transport.TransportManager;

/**
 * Annotation service factory that adjusts for XFire noncompliance to the spec that makes the correct
 * service name from a class name.
 *
 * @author Ryan Heaton
 */
public class EnunciatedAnnotationServiceFactory extends AnnotationServiceFactory {

  public EnunciatedAnnotationServiceFactory() {
  }

  public EnunciatedAnnotationServiceFactory(final TransportManager transportManager) {
    super(transportManager);
  }

  public EnunciatedAnnotationServiceFactory(WebAnnotations webAnnotations, final TransportManager transportManager) {
    super(webAnnotations, transportManager);
  }

  public EnunciatedAnnotationServiceFactory(WebAnnotations webAnnotations, final TransportManager transportManager, final BindingProvider provider) {
    super(webAnnotations, transportManager, provider);
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

}
