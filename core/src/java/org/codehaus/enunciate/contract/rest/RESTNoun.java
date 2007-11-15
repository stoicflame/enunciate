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

package org.codehaus.enunciate.contract.rest;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * A REST noun, consisting of the noun name and context.
 *
 * @author Ryan Heaton
 */
public class RESTNoun {

  private static final String CONTEXT_PARAM_PATTERN = "\\{([^\\}]+)\\}";

  private final String name;
  private final String context;
  private final String canonicalContext;
  private final String antPattern;
  private final List<String> contextParameters;

  RESTNoun(String name, String context) {
    this.name = name;
    if (context.startsWith("/")) {
      context = context.substring(1);
    }
    if (context.endsWith("/")) {
      context = context.substring(0, context.length() - 1);
    }
    this.context = context;

    contextParameters = new ArrayList<String>();
    Matcher contextParameterMatcher = Pattern.compile(CONTEXT_PARAM_PATTERN).matcher(context);
    while (contextParameterMatcher.find()) {
      contextParameters.add(contextParameterMatcher.group(1));
    }

    this.antPattern = context.length() == 0 ? name : (context.replaceAll(CONTEXT_PARAM_PATTERN, "*") + "/" + name);

    String canonicalContext = context;
    for (int i = 0; i < contextParameters.size(); i++) {
      String contextParameter = contextParameters.get(i);
      canonicalContext = canonicalContext.replaceFirst("\\{" + contextParameter + "\\}", "{context-parameter-" + i + "}");
    }
    this.canonicalContext = canonicalContext;
  }

  /**
   * The name of the noun.
   *
   * @return The name of the noun.
   */
  public String getName() {
    return name;
  }

  /**
   * The context of the noun.
   *
   * @return The context of the noun.
   */
  public String getContext() {
    return context;
  }

  /**
   * The context parameters for this noun, in the order then appear in the context.
   *
   * @return The context parameters for this noun, in the order then appear in the context.
   */
  public List<String> getContextParameters() {
    return contextParameters;
  }

  /**
   * The ant pattern for this noun.
   *
   * @return The ant pattern for this noun.
   */
  public String getAntPattern() {
    return antPattern;
  }

  @Override
  public String toString() {
    if (this.context.length() == 0) {
      return this.name;
    }
    else {
      return this.context + "/" + this.name;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || !(o instanceof RESTNoun)) {
      return false;
    }

    RESTNoun restNoun = (RESTNoun) o;
    return canonicalContext.equals(restNoun.canonicalContext);
  }

  @Override
  public int hashCode() {
    return canonicalContext.hashCode();
  }
}
