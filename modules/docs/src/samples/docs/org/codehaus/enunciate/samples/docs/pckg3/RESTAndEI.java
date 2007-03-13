package org.codehaus.enunciate.samples.docs.pckg3;

import org.codehaus.enunciate.rest.annotations.RESTEndpoint;
import org.codehaus.enunciate.rest.annotations.Verb;
import org.codehaus.enunciate.rest.annotations.VerbType;
import org.codehaus.enunciate.rest.annotations.ProperNoun;
import org.codehaus.enunciate.samples.docs.pckg1.BeanOne;

import javax.jws.WebService;

/**
 * <child>text</child>
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
