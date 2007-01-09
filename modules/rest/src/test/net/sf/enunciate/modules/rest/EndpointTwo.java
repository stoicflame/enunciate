package net.sf.enunciate.modules.rest;

import net.sf.enunciate.rest.annotations.*;

/**
 * @author Ryan Heaton
 */
@RESTEndpoint
public class EndpointTwo {

  @Noun ( "three" )
  @Verb (VerbType.read)
  public RootElementExample getThree(@ProperNoun String which) {
    return null;
  }

  @Noun ( "three")
  @Verb ( VerbType.create )
  public void addThree(@NounValue RootElementExample ex) {

  }

  @Noun ( "three" )
  @Verb ( VerbType.update )
  public RootElementExample setThree(@ProperNoun String which, @NounValue RootElementExample ex) {
    return ex;
  }

  @Noun ( "three" )
  @Verb ( VerbType.delete )
  public void deleteThree(@ProperNoun String which) {

  }

  @Noun ( "four" )
  @Verb (VerbType.read)
  public RootElementExample getFour(@ProperNoun String which) {
    return null;
  }

  @Noun ( "four")
  @Verb ( VerbType.create )
  public void addFour(@NounValue RootElementExample ex) {

  }

  @Noun ( "four" )
  @Verb ( VerbType.update )
  public RootElementExample setFour(@ProperNoun String which, @NounValue RootElementExample ex) {
    return ex;
  }

  @Noun ( "four" )
  @Verb ( VerbType.delete )
  public void deleteFour(@ProperNoun String which) {

  }
}
