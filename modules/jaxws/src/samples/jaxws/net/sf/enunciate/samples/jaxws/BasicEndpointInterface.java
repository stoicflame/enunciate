package net.sf.enunciate.samples.jaxws;

import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService
public class BasicEndpointInterface {

  public int alreadyExisting(boolean bool) throws BasicFault {
    return 0;
  }

  public Double notConflictingMethod(short s, long l) throws NonConflictingFault {
    return null;
  }

}
