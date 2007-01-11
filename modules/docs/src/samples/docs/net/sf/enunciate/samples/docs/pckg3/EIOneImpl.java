package net.sf.enunciate.samples.docs.pckg3;

import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "net.sf.enunciate.samples.docs.pckg3.EIOne"
)
public class EIOneImpl implements EIOne {

  public void method1() {
  }

  public int method2(String param1, String param2) {
    return 0;
  }
}
