package net.sf.enunciate.samples.jaxws;

import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService
public class AnotherEndpointInterface {

  public int alreadyExisting(boolean bool) throws BasicFault {
    return 0;
  }

  public Double notConflictingMethod(short s, long l) throws BasicFault, NonConflictingFault {
    return null;
  }

}
