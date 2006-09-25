package net.sf.enunciate.modules.xfire_client;

import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.aegis.AegisBindingProvider;
import org.codehaus.xfire.annotations.AnnotationServiceFactory;
import org.codehaus.xfire.service.OperationInfo;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class ExplicitJAXWSAnnotationServiceFactory extends AnnotationServiceFactory {

  private final Map requestWrappers;
  private final Map responseWrappers;

  public ExplicitJAXWSAnnotationServiceFactory(String typeSetId, Map requestWrappers, Map responseWrappers) throws IOException, ClassNotFoundException {
    super(
      ExplicitWebAnnotations.readFrom(ExplicitJAXWSAnnotationServiceFactory.class.getResourceAsStream("/" + typeSetId + ".annotations")),
      XFireFactory.newInstance().getXFire().getTransportManager(),
      new AegisBindingProvider(new IntrospectingTypeRegistry(typeSetId))
    );

    this.requestWrappers = requestWrappers;
    this.responseWrappers = responseWrappers;
  }

  protected QName createInputMessageName(final OperationInfo op) {
    return (QName) requestWrappers.get(op.getName());
  }

  protected QName createOutputMessageName(final OperationInfo op) {
    return (QName) responseWrappers.get(op.getName());
  }
}
