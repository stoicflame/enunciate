package net.sf.enunciate.samples.xfire_client;

import javax.jws.WebService;
import java.util.Date;

/**
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "urn:xfire_client"
)
public class BasicEIOne {

  public String doSomethingWithADate(Date date) throws BasicFaultOne {
    return null;
  }

  public Date doSomethingWithAString(String string) throws BasicFaultOne {
    return null;
  }
}
