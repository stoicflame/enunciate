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
