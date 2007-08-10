/*
 * Copyright 2006 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                               @Adjective (name = "doody", optional = false) double adjective2) {
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

  public RootElementExample invokeableOp(@NounValue RootElementExample ex, String adjective1, @Adjective(name="hi") Float adjective2, Collection<Short> adjective3) {
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

  public RootElementExample invokeableOp2(@NounValue RootElementExample ex, @ProperNoun String properNoun, @Adjective(name="hi") Float adjective) {
    if (properNoun == null) {
      return null;
    }
    else if (!"properNounValue".equals(properNoun)) {
      throw new RuntimeException();
    }

    if (adjective == null) {
      return new RootElementExample();
    }
    else if (1234.5 != adjective) {
      throw new RuntimeException();
    }

    return ex;
  }

}
