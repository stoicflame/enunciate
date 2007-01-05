package net.sf.enunciate.modules.rest;

import net.sf.enunciate.rest.annotations.*;

/**
 * @author Ryan Heaton
 */
@RESTEndpoint
public class EndpointThreeImpl implements EndpointThree {
  public RootElementExample getSix(String which) {
    return null;
  }

  public void addSix(RootElementExample ex) {
  }

  public RootElementExample setSix(String which, RootElementExample ex) {
    return ex;
  }

  public void deleteSix(String which) {

  }

  @Noun ( "five" )
  public RootElementExample getFive(@ProperNoun
  String which) {
    return null;
  }

  @Noun ( "five")
  @Verb ( VerbType.create )
  public void addFive(RootElementExample ex) {

  }

  @Noun ( "five" )
  @Verb ( VerbType.update )
  public RootElementExample setFive(@ProperNoun String which, RootElementExample ex) {
    return ex;
  }

  @Noun ( "five" )
  @Verb ( VerbType.delete )
  public void deleteFive(@ProperNoun String which) {

  }

  @Exclude
  public void hiddenMethod() {

  }

  protected void protectedMethod() {

  }

  public void dontExpose() {
  }
}
