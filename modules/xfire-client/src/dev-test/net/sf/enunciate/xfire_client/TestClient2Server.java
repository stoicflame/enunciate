package net.sf.enunciate.xfire_client;

import org.testng.annotations.Test;
import net.sf.enunciate.samples.petclinic.client.services.impl.OwnerServiceImpl;
import static org.testng.Assert.*;

import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class TestClient2Server {

  @Test
  public void testBasicCall() throws Exception {
    OwnerServiceImpl service = new OwnerServiceImpl(null, 8090, "/basic/soap/OwnerServiceService");
    Collection owners = service.findOwners("Wednesday");
    assertNotNull(owners);
  }
}
