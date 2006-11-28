package net.sf.enunciate.modules.xml;

import javax.jws.WebService;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * @author Ryan Heaton
 */
@WebService (
  targetNamespace = "urn:InvalidEndpointInterface"
)
public class InvalidEndpointInterface {

  @RequestWrapper (
    targetNamespace = "urn:different"
  )
  public Boolean requestWrapperHasDifferentTargetNS(int param1, short param2) {
    return null;
  }

  @ResponseWrapper (
    targetNamespace = "urn:different"
  )
  public Boolean responseWrapperHasDifferentTargetNS(int param1, short param2) {
    return null;
  }

  public Boolean webParamHasDifferentTargetNS(int param1, @WebParam (targetNamespace = "urn:different") short param2) {
    return null;
  }

  @WebResult (
    targetNamespace = "urn:different"
  )
  public Boolean webResultHasDifferentTargetNS(int param1, short param2) {
    return null;
  }
}
