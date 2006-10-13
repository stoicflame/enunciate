package net.sf.modules.xfire;

import net.sf.enunciate.modules.xfire.EnunciatedJAXWSServiceFactory;
import net.sf.enunciate.samples.petclinic.Owner;
import net.sf.enunciate.samples.petclinic.services.OwnerService;
import net.sf.enunciate.samples.petclinic.services.ServiceException;
import org.codehaus.xfire.annotations.AnnotationServiceFactory;
import org.codehaus.xfire.client.XFireProxyFactory;
import org.codehaus.xfire.service.Service;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;

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
    EnunciatedJAXWSServiceFactory serviceFactory = new EnunciatedJAXWSServiceFactory();
    HashMap<String, Object> properties = new HashMap<String, Object>();
    properties.put(AnnotationServiceFactory.ALLOW_INTERFACE, true);
    Service service = serviceFactory.create(OwnerService.class, properties);

    ownerService = (OwnerService) new XFireProxyFactory().create(service, "http://localhost:8090/basic/soap/OwnerServiceService");
  }

  public void testFindOwners() throws ServiceException {
    Collection<Owner> owners = ownerService.findOwners("Wednesday");
  }

}
