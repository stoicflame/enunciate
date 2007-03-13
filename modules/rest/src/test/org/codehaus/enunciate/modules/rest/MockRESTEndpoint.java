package org.codehaus.enunciate.modules.rest;

import org.codehaus.enunciate.rest.annotations.*;

/**
 * @author Ryan Heaton
 */
@RESTEndpoint
public class MockRESTEndpoint {

  @Noun ( "example" )
  @Verb ( VerbType.update )
  public RootElementExample updateExample(@ProperNoun String properNoun, @NounValue RootElementExample example, int adjective1, String[] adjective2) {
    if (!"id".equals(properNoun)) {
      throw new RuntimeException();
    }

    if (9999 != adjective1) {
      throw new RuntimeException();
    }

    if (!"value1".equals(adjective2[0])) {
      throw new RuntimeException();
    }

    if (!"value2".equals(adjective2[1])) {
      throw new RuntimeException();
    }

    return example;
  }

}
