/*
 * Copyright 2006-2008 Web Cohesion
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

  @Noun (
    value = "six",
    context = "evens"
  )
  @Verb ( VerbType.read )
  RootElementExample getSix(@ProperNoun String which);

  @Noun (
    value = "six",
    context = "evens"
  )
  @Verb ( VerbType.create )
  void addSix(@NounValue RootElementExample ex);

  @Noun (
    value = "six",
    context = "evens"
  )
  @Verb ( VerbType.update )
  RootElementExample setSix(@ProperNoun String which, @NounValue RootElementExample ex);

  @Noun (
    value = "six",
    context = "evens"
  )
  @Verb ( VerbType.delete )
  void deleteSix(@ProperNoun String which);

  void dontExpose();
}
