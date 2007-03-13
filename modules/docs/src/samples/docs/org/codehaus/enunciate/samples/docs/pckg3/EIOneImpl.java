package org.codehaus.enunciate.samples.docs.pckg3;

import javax.jws.WebService;

/**
 * @author Ryan Heaton
 */
@WebService (
  endpointInterface = "org.codehaus.enunciate.samples.docs.pckg3.EIOne"
)
public class EIOneImpl implements EIOne {

  public void method1() {
  }

  public int method2(String param1, String param2) {
    return 0;
  }
}
