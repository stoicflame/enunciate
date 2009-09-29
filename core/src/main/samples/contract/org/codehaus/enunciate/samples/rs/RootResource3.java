package org.codehaus.enunciate.samples.rs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.codehaus.enunciate.samples.json.Address;

/**
 * @author Steven Cummings
 */
@Path("root3")
public class RootResource3 {
  @GET
  public Address getAddress()
  {
    return new Address();
  }
}
