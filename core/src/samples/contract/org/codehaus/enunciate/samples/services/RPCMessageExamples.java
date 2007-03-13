package org.codehaus.enunciate.samples.services;

import javax.jws.WebService;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.Duration;
import javax.xml.ws.Holder;
import java.net.URI;
import java.util.Calendar;

/**
 * @author Ryan Heaton
 */
@WebService
public class RPCMessageExamples {

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public void voidMethod(Calendar param) {

  }

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public boolean simpleMethod(URI param1, XMLGregorianCalendar xmlCalendar) {
    return false;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public short withHeader(@WebParam (header = true) Duration param1, Short param2) {
    return 0;
  }

  @SOAPBinding (
    style = SOAPBinding.Style.RPC
  )
  public boolean withInOut(@WebParam (mode = WebParam.Mode.INOUT)
  Holder<Float> param1, Double param2) {
    return false;
  }


}
