package net.sf.enunciate.modules.rest;

import net.sf.enunciate.rest.annotations.*;

/**
 * @author Ryan Heaton
 */
@RESTEndpoint
public interface EndpointOne {
  @Noun ( "one" )
  RootElementExample getOne(@ProperNoun
  String which);

  @Noun ( "one")
  @Verb ( VerbType.create )
  void addOne(@NounValue RootElementExample ex);

  @Noun ( "one" )
  @Verb ( VerbType.update )
  RootElementExample setOne(@ProperNoun String which, @NounValue RootElementExample ex);

  @Noun ( "one" )
  @Verb ( VerbType.delete )
  void deleteOne(@ProperNoun String which);

  @Noun ( "two" )
  RootElementExample getTwo(@ProperNoun
  String which);

  @Noun ( "two")
  @Verb ( VerbType.create )
  void addTwo(@NounValue RootElementExample ex);

  @Noun ( "two" )
  @Verb ( VerbType.update )
  RootElementExample setTwo(@ProperNoun String which, @NounValue RootElementExample ex);

  @Noun ( "two" )
  @Verb ( VerbType.delete )
  void deleteTwo(@ProperNoun String which);
}
