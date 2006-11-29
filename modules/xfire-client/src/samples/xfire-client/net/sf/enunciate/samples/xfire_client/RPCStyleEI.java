package net.sf.enunciate.samples.xfire_client;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author Ryan Heaton
 */
@WebService
public class RPCStyleEI {

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public int doSomethingInRPCStyle(boolean param1) {
    return 0;
  }
}
