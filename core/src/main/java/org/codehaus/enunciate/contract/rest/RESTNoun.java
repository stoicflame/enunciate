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

package org.codehaus.enunciate.contract.rest;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * A REST noun, consisting of the noun name and context.
 *
 * @author Ryan Heaton
 * @deprecated The Enunciate-proprietary REST model has been deprecated in favor of JAX-RS.
 */
public class RESTNoun {

  private static final String CONTEXT_PARAM_PATTERN = "\\{([^\\}]+)\\}";

  private final String name;
  private final String context;
  private final String canonicalForm;
  private final String antPattern;
  private final List<String> servletPatterns;
  private final List<String> contextParameters;

  RESTNoun(String name, String context) {
    this.name = name;
    if (context == null) {
      context = "";
    }
    
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
    StringBuilder servletPattern = new StringBuilder();
    servletPattern.append('/');
    contextParameterMatcher.reset();
    if (contextParameterMatcher.find()) {
      servletPattern.append(context, 0, contextParameterMatcher.start()).append('*');
      this.servletPatterns = Arrays.asList(servletPattern.toString());
    }
    else {
      if (context.length() > 0) {
        servletPattern.append(context).append('/');
      }
      servletPattern.append(name);

      //this noun can be accessed by it's path AND by anything following its path.
      this.servletPatterns = Arrays.asList(servletPattern.toString(), servletPattern.append("/*").toString());
    }

    String canonicalContext = context;
    for (int i = 0; i < contextParameters.size(); i++) {
      String contextParameter = contextParameters.get(i);
      canonicalContext = canonicalContext.replaceFirst("\\{" + contextParameter + "\\}", "{context-parameter-" + i + "}");
    }
    this.canonicalForm = canonicalContext + "/" + name;
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

  /**
   * The servlet patterns for matching this noun. Appropriate for matching according to the rules of the servlet spec.  Since
   * the servlet spec is restrictive in its matching ability, the noun can be defined by multiple servlet patterns.
   *
   * @return The servlet patterns for matching this noun.
   */
  public List<String> getServletPatterns() {
    return servletPatterns;
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
    return canonicalForm.equals(restNoun.canonicalForm);
  }

  @Override
  public int hashCode() {
    return canonicalForm.hashCode();
  }
}
