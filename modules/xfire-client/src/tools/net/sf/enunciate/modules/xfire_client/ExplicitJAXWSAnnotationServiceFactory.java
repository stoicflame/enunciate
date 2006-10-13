package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.XFireRuntimeException;
import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.annotations.AnnotationServiceFactory;
import org.codehaus.xfire.annotations.WebAnnotations;
import org.codehaus.xfire.exchange.MessageSerializer;
import org.codehaus.xfire.fault.XFireFault;
import org.codehaus.xfire.service.OperationInfo;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.soap.AbstractSoapBinding;

import javax.xml.namespace.QName;
import java.io.IOException;

/**
 * @author Ryan Heaton
 */
public class ExplicitJAXWSAnnotationServiceFactory extends AnnotationServiceFactory {

  private final ExplicitWebAnnotations annotations;

  public ExplicitJAXWSAnnotationServiceFactory(String typeSetId) throws IOException, ClassNotFoundException {
    super(
      ExplicitWebAnnotations.readFrom(ExplicitJAXWSAnnotationServiceFactory.class.getResourceAsStream("/" + typeSetId + ".annotations")),
      XFireFactory.newInstance().getXFire().getTransportManager(),
      new AegisBindingProvider(new IntrospectingTypeRegistry(typeSetId))
    );

    //irritating that we have to read the file twice, but we have to make sure the super get the right annotations, too...
    this.annotations = ExplicitWebAnnotations.readFrom(ExplicitJAXWSAnnotationServiceFactory.class.getResourceAsStream("/" + typeSetId + ".annotations"));
  }

  @Override
  protected WebAnnotations getAnnotations() {
    return annotations;
  }

  @Override
  protected MessageSerializer getSerializer(AbstractSoapBinding binding) {
    return new EnunciatedClientMessageBinding((ExplicitWebAnnotations) getAnnotations());
  }

  @Override
  public void createBindingOperation(Service service, AbstractSoapBinding binding, OperationInfo op) {
    super.createBindingOperation(service, binding, op);

    try {
      binding.setSerializer(op, new EnunciatedClientOperationBinding(annotations, op));
    }
    catch (XFireFault e) {
      throw new XFireRuntimeException("Error setting the serializer on the operation binding.", e);
    }
  }

  @Override
  protected QName createInputMessageName(final OperationInfo op) {
    //todo: get this from the annotations....
    return (QName) requestWrappers.get(op.getName());
  }

  @Override
  protected QName createOutputMessageName(final OperationInfo op) {
    //todo: get this from the annotations....
    return (QName) responseWrappers.get(op.getName());
  }
}
