package net.sf.enunciate.samples.docs.pckg3;

import javax.jws.WebService;

/**
 * documentation for EIOne.
 *
 * @author Ryan Heaton
 */
@WebService
public interface EIOne {

  /**
   * docs for method1
   */
  void method1();

  /**
   * docs for method2
   * <i>should</i> be marked up however you like.
   *
   * @param param1 docs for method2.param1
   * @param param2 docs for method2.param2
   * @return return docs for method2
   * @someother someother value
   */
  int method2(String param1, String param2) throws FaultOne ;
}
