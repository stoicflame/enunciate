package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.transport.TransportManager;
import org.codehaus.xfire.util.ClassLoaderUtils;
import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.annotations.AnnotationServiceFactory;
import org.codehaus.xfire.annotations.WebAnnotations;
import org.codehaus.xfire.exchange.MessageSerializer;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.FaultInfo;
import org.codehaus.xfire.soap.AbstractSoapBinding;

import javax.xml.namespace.QName;
import java.io.IOException;

import net.sf.enunciate.modules.xfire_client.annotations.RequestWrapperAnnotation;
import net.sf.enunciate.modules.xfire_client.annotations.ResponseWrapperAnnotation;
import net.sf.enunciate.modules.xfire_client.annotations.WebFaultAnnotation;

/**
 * @author Ryan Heaton
 */
public class ExplicitJAXWSAnnotationServiceFactory extends AnnotationServiceFactory {

  private final ExplicitWebAnnotations annotations;

  public ExplicitJAXWSAnnotationServiceFactory(String typeSetId, TransportManager transportManager) throws IOException, ClassNotFoundException {
    super(
      ExplicitWebAnnotations.readFrom(ExplicitJAXWSAnnotationServiceFactory.class.getResourceAsStream("/" + typeSetId + ".annotations")),
      transportManager,
      new AegisBindingProvider(new IntrospectingTypeRegistry(typeSetId))
    );

    //irritating that we have to read the file twice, but we have to make sure the super get the right annotations, too...
    this.annotations = ExplicitWebAnnotations.readFrom(ExplicitJAXWSAnnotationServiceFactory.class.getResourceAsStream("/" + typeSetId + ".annotations"));
  }

  protected WebAnnotations getAnnotations() {
    return annotations;
  }

  protected MessageSerializer getSerializer(AbstractSoapBinding binding) {
    return new EnunciatedClientMessageBinding((ExplicitWebAnnotations) getAnnotations());
  }

  public void createBindingOperation(Service service, AbstractSoapBinding binding, OperationInfo op) {
    super.createBindingOperation(service, binding, op);

    try {
      binding.setSerializer(op, new EnunciatedClientOperationBinding(annotations, op));
    }
    catch (XFireFault e) {
      throw new XFireRuntimeException("Error setting the serializer on the operation binding.", e);
    }
  }

  protected FaultInfo addFault(Service service, OperationInfo op, Class faultClass) {
    WebFaultAnnotation faultInfo = annotations.getWebFaultAnnotation(faultClass);
    if (faultInfo != null) {
      QName name = new QName(faultInfo.targetNamespace(), faultInfo.name());
      FaultInfo info = op.addFault(name.getLocalPart());
      info.setExceptionClass(faultClass);
      try {
        info.addMessagePart(name, ClassLoaderUtils.loadClass(faultInfo.faultBean(), faultClass));
      }
      catch (ClassNotFoundException e) {
        throw new XFireRuntimeException("Unable to load fault bean.", e);
      }
      
      return info;
    }

    throw new XFireRuntimeException("Unknown web fault: " + faultClass.getName());
  }

  protected boolean isFaultInfoClass(Class faultClass) {
    WebFaultAnnotation faultInfo = annotations.getWebFaultAnnotation(faultClass);
    return ((faultInfo != null) && (!faultInfo.implicitFaultBean()));
  }

  protected QName createInputMessageName(final OperationInfo op) {
    RequestWrapperAnnotation requestWrapper = annotations.getRequestWrapperAnnotation(op.getMethod());
    if (requestWrapper != null) {
      return new QName(requestWrapper.targetNamespace(), requestWrapper.localName());
    }
    return super.createInputMessageName(op);
  }

  protected QName createOutputMessageName(final OperationInfo op) {
    ResponseWrapperAnnotation responseWrapper = annotations.getResponseWrapperAnnotation(op.getMethod());
    if (responseWrapper != null) {
      return new QName(responseWrapper.targetNamespace(), responseWrapper.localName());
    }
    return super.createOutputMessageName(op);
  }
}
