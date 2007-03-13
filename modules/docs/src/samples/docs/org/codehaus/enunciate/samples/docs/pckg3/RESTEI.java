package org.codehaus.enunciate.samples.docs.pckg3;

import org.codehaus.enunciate.rest.annotations.*;
import org.codehaus.enunciate.samples.docs.pckg2.BeanTwo;

/**
 * docs for RESTEI
 * 
 * @author Ryan Heaton
 */
@RESTEndpoint
public class RESTEI {

  /**
   * documentation for <span>method1</span>
   *
   * @param two docs for two
   * @param id docs for id
   * @param param1 docs for param1
   * @param param2 docs for param2
   * @return docs for return
   * @throws FaultTwo if something bad happens
   */
  @Verb ( VerbType.create )
  public BeanTwo method1(@NounValue BeanTwo two, @ProperNoun String id, String param1, @Adjective( name="param2") String param2) throws FaultTwo {
    return null;
  }

}
