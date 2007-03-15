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
public interface EndpointOne {
  @Noun ( "one" )
  @Verb (VerbType.read)
  RootElementExample getOne(@ProperNoun String which);

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
  @Verb (VerbType.read)
  RootElementExample getTwo(@ProperNoun String which);

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
