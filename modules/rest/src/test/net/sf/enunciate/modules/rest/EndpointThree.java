package net.sf.enunciate.modules.rest;

import net.sf.enunciate.rest.annotations.*;

/**
 * @author Ryan Heaton
 */
@RESTEndpoint
public interface EndpointThree {

  @Noun ( "six" )
  @Verb ( VerbType.read )
  RootElementExample getSix(@ProperNoun String which);

  @Noun ( "six")
  @Verb ( VerbType.create )
  void addSix(@NounValue RootElementExample ex);

  @Noun ( "six" )
  @Verb ( VerbType.update )
  RootElementExample setSix(@ProperNoun String which, @NounValue RootElementExample ex);

  @Noun ( "six" )
  @Verb ( VerbType.delete )
  void deleteSix(@ProperNoun String which);

  void dontExpose();
}
