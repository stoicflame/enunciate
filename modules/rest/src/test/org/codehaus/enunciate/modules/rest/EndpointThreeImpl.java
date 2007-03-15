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

import org.codehaus.enunciate.rest.annotations.*;

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
  @Verb ( VerbType.read )
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

  public void hiddenMethod() {

  }

  protected void protectedMethod() {

  }

  public void dontExpose() {
  }
}
