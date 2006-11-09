package net.sf.enunciate.samples.services;

import javax.jws.WebService;
import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.Holder;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author Ryan Heaton
 */
@WebService
public class ResponseWrapperExamples {

  @ResponseWrapper (
    localName = "fully-annotated",
    targetNamespace = "urn:fully-annotated",
    className = "net.sf.enunciate.samples.services.FullyAnnotatedMethod"
  )
  public String fullyAnnotated(Date date, QName qname) {
    return null;
  }

  public java.awt.Image defaultAnnotated(BigDecimal decimal, BigInteger integer) {
    return null;
  }

  public int withInOut(@WebParam(mode = WebParam.Mode.INOUT) Holder<Byte> b, String str) {
    return 0;
  }

}
