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

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.View;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.codehaus.enunciate.rest.annotations.VerbType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.bind.Unmarshaller;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Set;
import java.util.HashMap;
import java.lang.reflect.Array;

/**
 * An xml exporter for a REST resource.
 *
 * @author Ryan Heaton
 */
public class RESTResourceXMLExporter extends AbstractController {

  private final String noun;
  private final String nounContext;
  private final DocumentBuilder documentBuilder;
  private final Pattern urlPattern;
  private final RESTResource resource;
  private HandlerExceptionResolver exceptionHandler = new RESTExceptionHandler();

  public RESTResourceXMLExporter(String noun, String nounContext, RESTResourceFactory resourceFactory) {
    this(noun, nounContext, resourceFactory.getRESTResource(nounContext, noun));
  }

  public RESTResourceXMLExporter(String noun, String nounContext, RESTResource resource) {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(false);
    try {
      documentBuilder = builderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
    this.noun = noun;
    this.nounContext = nounContext;
    this.resource = resource;
    this.urlPattern = Pattern.compile(nounContext + "/" + noun + "/?(.*)$");
  }

  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    if (resource == null) {
      throw new ApplicationContextException("No REST resource available for noun '" + this.noun + "' in context '" + this.nounContext + "'.");
    }

    Set<VerbType> supportedVerbs = resource.getSupportedVerbs();
    String[] supportedMethods = new String[supportedVerbs.size()];
    int i = 0;
    for (VerbType supportedVerb : supportedVerbs) {
      String method;
      switch (supportedVerb) {
        case create:
          method = "PUT";
          break;
        case read:
          method = "GET";
          break;
        case update:
          method = "POST";
          break;
        case delete:
          method = "DELETE";
          break;
        default:
          throw new IllegalStateException("Unsupported verb: " + supportedVerb);
      }
      supportedMethods[i++] = method;
    }
    setSupportedMethods(supportedMethods);
  }

  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String properNoun = null;
    Matcher matcher = urlPattern.matcher(request.getRequestURI());
    if (matcher.find()) {
      properNoun = matcher.group(1);

      if ("".equals(properNoun)) {
        properNoun = null;
      }
    }
    else {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
      return null;
    }

    String httpMethod = request.getMethod().toUpperCase();
    VerbType verb = null;
    if ("PUT".equals(httpMethod)) {
      verb = VerbType.create;
    }
    else if ("GET".equals(httpMethod)) {
      verb = VerbType.read;
    }
    else if ("POST".equals(httpMethod)) {
      verb = VerbType.update;
    }
    else if ("DELETE".equals(httpMethod)) {
      verb = VerbType.delete;
    }
    else {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Unsupported HTTP operation: " + httpMethod);
      return null;
    }

    try {
      return handleRESTOperation(properNoun, verb, request, response);
    }
    catch (Exception e) {
      if (this.exceptionHandler != null) {
        return this.exceptionHandler.resolveException(request, response, this, e);
      }
      else {
        throw e;
      }
    }
  }

  /**
   * Handles a specific REST operation.
   *
   * @param properNoun The proper noun, if supplied by the request.
   * @param verb The verb.
   * @param request The request.
   * @param response The response.
   * @return The model and view.
   */
  protected ModelAndView handleRESTOperation(String properNoun, VerbType verb, HttpServletRequest request, HttpServletResponse response) throws Exception {
    RESTOperation operation = resource.getOperation(verb);
    if (!isOperationAllowed(operation)) {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Unsupported verb: " + verb);
      return null;
    }

    Document document = documentBuilder.newDocument();

    Unmarshaller unmarshaller = operation.getSerializationContext().createUnmarshaller();
    unmarshaller.setAttachmentUnmarshaller(RESTAttachmentUnmarshaller.INSTANCE);

    Object properNounValue = null;
    if ((properNoun != null) && (operation.getProperNounType() != null)) {
      Element element = document.createElement("unimportant");
      element.appendChild(document.createTextNode(properNoun));
      properNounValue = unmarshaller.unmarshal(element, operation.getProperNounType()).getValue();
    }

    HashMap<String, Object> adjectives = new HashMap<String, Object>();
    for (String adjective : operation.getAdjectiveTypes().keySet()) {
      Object adjectiveValue = null;

      String[] parameterValues = request.getParameterValues(adjective);
      if ((parameterValues != null) && (parameterValues.length > 0)) {
        Class adjectiveType = operation.getAdjectiveTypes().get(adjective);
        Class componentType = adjectiveType;
        if (adjectiveType.isArray()) {
          componentType = adjectiveType.getComponentType();
        }
        Object adjectiveValues = Array.newInstance(componentType, parameterValues.length);

        for (int i = 0; i < parameterValues.length; i++) {
          Element element = document.createElement("unimportant");
          element.appendChild(document.createTextNode(parameterValues[i]));
          Array.set(adjectiveValues, i, unmarshaller.unmarshal(element, componentType).getValue());
        }

        if (adjectiveType.isArray()) {
          adjectiveValue = adjectiveValues;
        }
        else {
          adjectiveValue = Array.get(adjectiveValues, 0);
        }
      }

      adjectives.put(adjective, adjectiveValue);
    }

    Object nounValue = null;
    if (operation.getNounValueType() != null) {
      //if the operation has a noun value type, unmarshall it from the body....
      nounValue = unmarshaller.unmarshal(request.getInputStream());
    }

    Object result = operation.invoke(properNounValue, adjectives, nounValue);
    return new ModelAndView(createView(operation, result));
  }

  /**
   * Create the view for the specified operation and result.
   *
   * @param operation The operation.
   * @param result The result.
   * @return The view.
   */
  protected View createView(RESTOperation operation, Object result) {
    return new RESTResultView(operation, result);
  }

  /**
   * Whether the specified operation is allowed.
   *
   * @param operation The operation to test whether it is allowed.
   * @return Whether the specified operation is allowed.
   */
  protected boolean isOperationAllowed(RESTOperation operation) {
    return operation != null;
  }


}
