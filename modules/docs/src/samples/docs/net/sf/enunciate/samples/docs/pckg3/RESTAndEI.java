package net.sf.enunciate.samples.docs.pckg3;

import net.sf.enunciate.rest.annotations.RESTEndpoint;
import net.sf.enunciate.rest.annotations.Verb;
import net.sf.enunciate.rest.annotations.VerbType;
import net.sf.enunciate.rest.annotations.ProperNoun;
import net.sf.enunciate.samples.docs.pckg1.BeanOne;

import javax.jws.WebService;

/**
 * docs for RESTAndEI
 *
 * @author Ryan Heaton
 */
@WebService
@RESTEndpoint
public class RESTAndEI {

  /**
   * docs for getBeanOne
   *
   * @param id The id
   * @return The bean
   * that has the return
   * value one multiple lines
   * @sometag sometag value
   */
  @Verb (
    VerbType.read
  )
  public BeanOne getBeanOne(@ProperNoun String id) {
    return null;
  }
}
