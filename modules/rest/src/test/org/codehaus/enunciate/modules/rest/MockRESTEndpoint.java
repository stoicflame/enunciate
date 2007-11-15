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
public class MockRESTEndpoint {

  @Noun (
    value = "example",
    context = "/ctx/{uriParam1}/{otherParam}"

  )
  @Verb ( VerbType.update )
  public RootElementExample updateExample(@ProperNoun String properNoun, @NounValue RootElementExample example, int adjective1, String[] adjective2, @ContextParameter("uriParam1") String contextParameter1, @ContextParameter("otherParam") String contextParameter2) {
    if (!"id".equals(properNoun)) {
      throw new RuntimeException();
    }

    if (9999 != adjective1) {
      throw new RuntimeException();
    }

    if (!"value1".equals(adjective2[0])) {
      throw new RuntimeException();
    }

    if (!"value2".equals(adjective2[1])) {
      throw new RuntimeException();
    }

    if (!"ctxValueOne".equals(contextParameter1)) {
      throw new RuntimeException();
    }

    if (!"otherValue".equals(contextParameter2)) {
      throw new RuntimeException();
    }

    return example;
  }

}
