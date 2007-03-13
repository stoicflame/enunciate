package org.codehaus.enunciate.modules.rest;

import org.codehaus.enunciate.rest.annotations.RESTEndpoint;
import org.codehaus.enunciate.rest.annotations.ProperNoun;
import org.codehaus.enunciate.rest.annotations.NounValue;
import org.codehaus.enunciate.rest.annotations.Adjective;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Ryan Heaton
 */
@RESTEndpoint
public class RESTOperationExamples {

  public void ping() {
  }

  public void defaultAdjectives(String adjective1, double adjective2) {
  }

  public void customAdjectives(@Adjective (name = "howdy") String adjective1,
                               @Adjective (name = "doody") double adjective2) {
  }

  public void adjectivesAsLists(@Adjective (name = "bools") boolean[] bools, @Adjective (name="ints") Collection<Integer> ints) {

  }

  public Object badReturnType() {
    return null;
  }

  public RootElementExample properNoun(@ProperNoun String properNoun) {
    return null;
  }

  public RootElementExample twoProperNouns(@ProperNoun String properNoun1, @ProperNoun String properNoun2) {
    return null;
  }

  public RootElementExample badProperNoun(@ProperNoun String[] properNoun) {
    return null;
  }

  public RootElementExample nounValue(@NounValue RootElementExample nounValue) {
    return null;
  }

  public RootElementExample twoNounValues(@NounValue RootElementExample nounValue1, @NounValue RootElementExample nounValue2) {
    return null;
  }

  public RootElementExample badNounValue(@NounValue Object nounValue) {
    return null;
  }

  public RootElementExample invokeableOp(@NounValue RootElementExample ex, String adjective1, @Adjective(name="hi") float adjective2, Collection<Short> adjective3) {
    if (!"adjective1Value".equals(adjective1)) {
      throw new RuntimeException();
    }

    if (1234.5 != adjective2) {
      throw new RuntimeException();
    }

    Iterator<Short> iterator = adjective3.iterator();
    if (8 != iterator.next()) {
      throw new RuntimeException();
    }
    if (7 != iterator.next()) {
      throw new RuntimeException();
    }
    if (6 != iterator.next()) {
      throw new RuntimeException();
    }

    return ex;
  }

}
