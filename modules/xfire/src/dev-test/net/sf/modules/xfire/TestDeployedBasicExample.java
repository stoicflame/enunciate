package net.sf.modules.xfire;

import org.testng.annotations.Test;
import org.testng.annotations.Configuration;
import org.codehaus.xfire.transport.TransportManager;
import org.codehaus.xfire.XFireFactory;
import org.codehaus.xfire.jaxws.JAXWSServiceFactory;
import org.codehaus.xfire.annotations.AnnotationServiceFactory;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.ServiceFactory;
import org.codehaus.xfire.service.Service;
import net.sf.enunciate.samples.petclinic.services.OwnerService;
import net.sf.enunciate.samples.petclinic.services.ServiceException;
import net.sf.enunciate.samples.petclinic.Owner;

import java.util.HashMap;
import java.util.Collection;
import java.net.MalformedURLException;

/**
 * @author Ryan Heaton
 */
@Test (
  groups = "container"
)
public class TestDeployedBasicExample {

  private OwnerService ownerService;

  @Configuration (
    beforeTestClass = true
  )
  public void setupServices() throws MalformedURLException {
    TransportManager transportManager = XFireFactory.newInstance().getXFire().getTransportManager();
    ServiceFactory serviceFactory = new JAXWSServiceFactory(transportManager);
    HashMap<String, Object> properties = new HashMap<String, Object>();
    properties.put(AnnotationServiceFactory.ALLOW_INTERFACE, true);
    Service service = serviceFactory.create(OwnerService.class, properties);

    ownerService = (OwnerService) new XFireProxyFactory().create(service, "http://localhost:8080/basic/soap/OwnerServiceService");
  }

  public void testFindOwners() throws ServiceException {
    Collection<Owner> owners = ownerService.findOwners("Wednesday");
  }

}
