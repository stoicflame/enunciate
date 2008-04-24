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

import org.codehaus.enunciate.rest.annotations.VerbType;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A REST resource is composed of the following:
 *
 * <ul>
 *   <li>A noun in its context.</li>
 *   <li>The set of verbs that are applicable to the noun.</li>
 * </ul>
 *
 * @author Ryan Heaton
 */
public class RESTResource implements Comparable<RESTResource> {

  private static final String CONTEXT_PARAM_PATTERN = "\\{([^\\}]+)\\}";

  private final String noun;
  private final String nounContext;
  private final Pattern regexpPattern;
  private final List<String> contextParameters;
  private final Set<RESTOperation> operations;
  private Properties paramterNames;
  private String defaultContentType = "application/xml";

  /**
   * Construct a REST resource for the specified noun, assuming the empty context.
   *
   * @param noun The noun for this REST resource.
   */
  public RESTResource(String noun) {
    this(noun, "");
  }

  /**
   * Construct a REST resource for the specified noun and noun context.
   * @param noun The noun.
   * @param nounContext The noun context.
   */
  public RESTResource(String noun, String nounContext) {
    this.noun = noun;
    this.nounContext = nounContext;
    contextParameters = new ArrayList<String>();
    Matcher contextParameterMatcher = Pattern.compile(CONTEXT_PARAM_PATTERN).matcher(nounContext);
    while (contextParameterMatcher.find()) {
      contextParameters.add(contextParameterMatcher.group(1));
    }

    this.regexpPattern = Pattern.compile(nounContext.replaceAll(CONTEXT_PARAM_PATTERN, "([^/]+)") + "/" + noun + "/?(.*)$");
    this.operations = new TreeSet<RESTOperation>(new Comparator<RESTOperation>() {
      public int compare(RESTOperation o1, RESTOperation o2) {
        int comparison = o1.getContentType().compareTo(o2.getContentType());
        if (comparison == 0) {
          comparison = o1.getVerb().compareTo(o2.getVerb());
        }
        return comparison;
      }
    });
  }

  /**
   * Adds an operation to this REST resource.
   *
   * @param contentType The content type of the operation.
   * @param verb The verb for the operation.
   * @param endpoint The endpoint on which to invoke the operation.
   * @param method The method to invoke on the endpoint.
   * @return Whether the operation was successfully added.  (false if the specified verb was already added).
   */
  public boolean addOperation(String contentType, VerbType verb, Object endpoint, Method method) {
    String[] parameterNames = null;
    if (getParamterNames() != null) {
      String parameterList = (String) getParamterNames().get(getCanonicalReference(verb));
      if (parameterList != null) {
        parameterNames = parameterList.split(",");
      }
    }

    return this.operations.add(createOperation(contentType, verb, endpoint, method, parameterNames));
  }

  /**
   * Create an operation.
   *
   * @param contentType The content type.
   * @param verb The verb.
   * @param endpoint The endpoint.
   * @param method The method.
   * @param parameterNames The parameter names.
   * @return The operation.
   */
  protected RESTOperation createOperation(String contentType, VerbType verb, Object endpoint, Method method, String[] parameterNames) {
    return new RESTOperation(this, contentType, verb, endpoint, method, parameterNames);
  }

  /**
   * Get a canonical reference to the specified verb for this resource.
   *
   * @param verb The verb.
   * @return The reference.
   */
  protected String getCanonicalReference(VerbType verb) {
    String context = getNounContext();
    if (context.startsWith("/")) {
      context = context.substring(1);
    }
    if (context.endsWith("/")) {
      context = context.substring(0, context.length() - 1);
    }

    StringBuilder canonicalForm = new StringBuilder(context);
    if (context.length() > 0) {
      canonicalForm.append('/');
    }

    canonicalForm.append(getNoun()).append('/').append(verb);
    return canonicalForm.toString();
  }

  /**
   * Gets the REST operation for the specified verb and content type.
   *
   * @param contentType The content type.
   * @param verb The verb.
   * @return The REST operation.
   */
  public RESTOperation getOperation(String contentType, VerbType verb) {
    for (RESTOperation operation : operations) {
      if ((operation.getVerb().equals(verb)) && (operation.getContentType().equals(contentType))) {
        return operation;
      }
    }
    return null;
  }

  /**
   * The operations available to this resource.
   *
   * @return The operations available to this resource.
   */
  public Collection<RESTOperation> getOperations() {
    return operations;
  }

  /**
   * Pulls out the values of the context parameters and proper noun into a map.  The value of the proper
   * noun is put under the null key.
   *
   * @param requestContext The request context.
   * @return The parameter values.
   * @throws IllegalArgumentException If the pattern for this resource doesn't match.
   */
  public Map<String, String> getContextParameterAndProperNounValues(String requestContext) {
    Matcher matcher = regexpPattern.matcher(requestContext);
    if (!matcher.find()) {
      throw new IllegalArgumentException();
    }

    Map<String, String> values = new HashMap<String, String>();
    for (int i = 0; i < contextParameters.size(); i++) {
      String parameterName = contextParameters.get(i);
      values.put(parameterName, matcher.group(i + 1));
    }
    String properNounValue = matcher.group(contextParameters.size() + 1);
    if ("".equals(properNounValue)) {
      properNounValue = null;
    }
    values.put(null, properNounValue);
    return values;
  }

  /**
   * The noun for this resource.
   *
   * @return The noun for this resource.
   */
  public String getNoun() {
    return noun;
  }

  /**
   * The noun context for this resource.
   *
   * @return The noun context for this resource.
   */
  public String getNounContext() {
    return nounContext;
  }

  /**
   * Compares the two REST resources.
   *
   * @param other The resource to compare to this one.
   * @return The comparison.
   */
  public int compareTo(RESTResource other) {
    return this.noun.compareTo(other.noun);
  }

  /**
   * The paramter names.
   *
   * @return The paramter names.
   */
  public Properties getParamterNames() {
    return paramterNames;
  }

  /**
   * The paramter names.
   *
   * @param paramterNames The paramter names.
   */
  public void setParamterNames(Properties paramterNames) {
    this.paramterNames = paramterNames;
  }

  /**
   * The default content type for this resource.
   *
   * @return The default content type for this resource.
   */
  public String getDefaultContentType() {
    return defaultContentType;
  }

  /**
   * The default content type for this resource.
   *
   * @param defaultContentType The default content type for this resource.
   */
  public void setDefaultContentType(String defaultContentType) {
    this.defaultContentType = defaultContentType;
  }

  @Override
  public String toString() {
    return noun;
  }
}
