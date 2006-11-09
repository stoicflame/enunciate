package net.sf.enunciate.samples.services;

import javax.jws.WebService;
import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.ws.RequestWrapper;
import java.util.Date;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Ryan Heaton
 */
@WebService
public class RequestWrapperExamples {

  @RequestWrapper (
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

  public int withHeader(@WebParam(header = true) Byte b, String str) {
    return 0;
  }

}
