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
