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

import org.codehaus.enunciate.rest.annotations.VerbType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.validation.FieldError;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * An xml exporter for a REST resource.
 *
 * @author Ryan Heaton
 */
public class RESTResourceXMLExporter extends AbstractController {

  private final DocumentBuilder documentBuilder;
  private final RESTResource resource;
  private HandlerExceptionResolver exceptionHandler = new RESTExceptionHandler();
  private Map<String, String> ns2prefix;
  private String[] supportedMethods;
  private MultipartResolverFactory multipartResolverFactory;

  public RESTResourceXMLExporter(RESTResource resource) {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(false);
    try {
      documentBuilder = builderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
    this.resource = resource;
  }

  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    if (resource == null) {
      throw new ApplicationContextException("A REST resource must be provided.");
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
    this.supportedMethods = supportedMethods;
    Map resolverBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), MultipartResolverFactory.class);
    if (resolverBeans.size() > 0) {
      //todo: add a configuration element to specify which one to use.
      this.multipartResolverFactory = (MultipartResolverFactory) resolverBeans.values().iterator().next();
    }
    else {
      CommonsMultipartResolverFactory resolverFactory = new CommonsMultipartResolverFactory();
      if (getApplicationContext() instanceof WebApplicationContext) {
        resolverFactory.setServletContext(getServletContext());
      }
      this.multipartResolverFactory = resolverFactory;
    }
    super.setSupportedMethods(new String[]{"GET", "PUT", "POST", "DELETE"});
  }

  public HandlerExceptionResolver getExceptionHandler() {
    return exceptionHandler;
  }

  public void setExceptionHandler(HandlerExceptionResolver exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }

  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String httpMethod = request.getHeader("X-HTTP-Method-Override");
    if ((httpMethod == null) || ("".equals(httpMethod.trim()))) {
      httpMethod = request.getMethod().toUpperCase();
    }
    else {
      httpMethod = httpMethod.toUpperCase();
    }

    VerbType verb;
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
      throw new MethodNotAllowedException(this.supportedMethods);
    }

    if (!resource.getSupportedVerbs().contains(verb)) {
      throw new MethodNotAllowedException(this.supportedMethods);
    }

    try {
      return handleRESTOperation(verb, request, response);
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
   * @param verb     The verb.
   * @param request  The request.
   * @param response The response.
   * @return The model and view.
   */
  protected ModelAndView handleRESTOperation(VerbType verb, HttpServletRequest request, HttpServletResponse response) throws Exception {
    RESTOperation operation = resource.getOperation(verb);
    if (!isOperationAllowed(operation)) {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Unsupported verb: " + verb);
      return null;
    }

    boolean isMultipart = this.multipartResolverFactory != null && this.multipartResolverFactory.isMultipart(request);
    if (isMultipart) {
      MultipartResolver multipartResolver = this.multipartResolverFactory.getMultipartResolver(resource.getNounContext(), resource.getNoun(), verb);
      if (multipartResolver != null && multipartResolver.isMultipart(request)) {
        request = multipartResolver.resolveMultipart(request);
      }
    }

    Document document = documentBuilder.newDocument();

    Unmarshaller unmarshaller = operation.getSerializationContext().createUnmarshaller();
    unmarshaller.setAttachmentUnmarshaller(RESTAttachmentUnmarshaller.INSTANCE);

    String requestContext = request.getRequestURI().substring(request.getContextPath().length());
    Map<String, String> contextParameters;
    try {
      contextParameters = resource.getContextParameterAndProperNounValues(requestContext);
    }
    catch (IllegalArgumentException e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, request.getRequestURI());
      return null;
    }

    Object properNounValue = null;
    HashMap<String, Object> contextParameterValues = new HashMap<String, Object>();
    for (Map.Entry<String, String> entry : contextParameters.entrySet()) {
      if (entry.getKey() == null) {
        if (operation.getProperNounType() != null) {
          if (!String.class.isAssignableFrom(operation.getProperNounType())) {
            Element element = document.createElement("unimportant");
            element.appendChild(document.createTextNode(contextParameters.get(entry.getKey())));
            properNounValue = unmarshaller.unmarshal(element, operation.getProperNounType()).getValue();
          }
          else {
            properNounValue = contextParameters.get(entry.getKey());
          }
        }
      }
      else {
        Class contextParameterType = operation.getContextParameterTypes().get(entry.getKey());
        if (contextParameterType != null) {
          if (!String.class.isAssignableFrom(contextParameterType)) {
            Element element = document.createElement("unimportant");
            element.appendChild(document.createTextNode(contextParameters.get(entry.getKey())));
            contextParameterValues.put(entry.getKey(), unmarshaller.unmarshal(element, contextParameterType).getValue());
          }
          else {
            contextParameterValues.put(entry.getKey(), contextParameters.get(entry.getKey()));
          }
        }
      }
    }

    if ((properNounValue == null) && (operation.isProperNounOptional() != null) && (!operation.isProperNounOptional())) {
      throw new MissingParameterException("A specific '" + resource.getNoun() + "' must be specified on the URL.");
    }

    HashMap<String, Object> adjectives = new HashMap<String, Object>();
    for (String adjective : operation.getAdjectiveTypes().keySet()) {
      Object adjectiveValue = null;

      if (!operation.getComplexAdjectives().contains(adjective)) {
        //not complex, map it.
        String[] parameterValues = request.getParameterValues(adjective);
        if ((parameterValues != null) && (parameterValues.length > 0)) {
          Class adjectiveType = operation.getAdjectiveTypes().get(adjective);
          Class componentType = adjectiveType;
          if (adjectiveType.isArray()) {
            componentType = adjectiveType.getComponentType();
          }
          Object adjectiveValues = Array.newInstance(componentType, parameterValues.length);

          for (int i = 0; i < parameterValues.length; i++) {
            if (!String.class.isAssignableFrom(componentType)) {
              Element element = document.createElement("unimportant");
              element.appendChild(document.createTextNode(parameterValues[i]));
              Array.set(adjectiveValues, i, unmarshaller.unmarshal(element, componentType).getValue());
            }
            else {
              Array.set(adjectiveValues, i, parameterValues[i]);
            }
          }

          if (adjectiveType.isArray()) {
            adjectiveValue = adjectiveValues;
          }
          else {
            adjectiveValue = Array.get(adjectiveValues, 0);
          }
        }

        if ((adjectiveValue == null) && (!operation.getAdjectivesOptional().get(adjective))) {
          throw new MissingParameterException("Missing request parameter: " + adjective);
        }
      }
      else {
        //use spring's binding to map the complex adjective to the request parameters.
        try {
          adjectiveValue = operation.getAdjectiveTypes().get(adjective).newInstance();
        }
        catch (Throwable e) {
          throw new IllegalArgumentException("A complex adjective must have a simple, no-arg constructor. Invalid type: " + operation.getAdjectiveTypes().get(adjective));
        }

        ServletRequestDataBinder binder = new ServletRequestDataBinder(adjectiveValue, adjective);
        binder.setIgnoreUnknownFields(true);
        binder.bind(request);
        BindException errors = binder.getErrors();
        if ((errors != null) && (errors.getAllErrors() != null) && (!errors.getAllErrors().isEmpty())) {
          ObjectError firstError = (ObjectError) errors.getAllErrors().get(0);
          String message = "Invalid parameter.";
          if (firstError instanceof FieldError) {
            message = String.format("Invalid parameter value: %s", ((FieldError) firstError).getRejectedValue());
          }
          response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
          return null;
        }
      }

      adjectives.put(adjective, adjectiveValue);
    }

    Object nounValue = null;
    if (operation.getNounValueType() != null) {
      Class type1 = operation.getNounValueType();
      if ((operation.getNounValueType().equals(DataHandler.class)) || ((type1.isArray() && type1.getComponentType().equals(DataHandler.class)))) {
        ArrayList<DataHandler> dataHandlers = new ArrayList<DataHandler>();
        if (request instanceof MultipartHttpServletRequest) {
          Collection<MultipartFile> multipartFiles = (Collection<MultipartFile>) ((MultipartHttpServletRequest) request).getFileMap().values();
          for (MultipartFile multipartFile : multipartFiles) {
            dataHandlers.add(new DataHandler(new MultipartFileDataSource(multipartFile)));
          }
        }
        else {
          dataHandlers.add(new DataHandler(new RESTRequestDataSource(request, resource.getNounContext() + resource.getNoun())));
        }

        if (operation.getNounValueType().equals(DataHandler.class)) {
          nounValue = dataHandlers.get(0);
        }
        else {
          Class type = operation.getNounValueType();
          if ((type.isArray() && type.getComponentType().equals(DataHandler.class))) {
            nounValue = dataHandlers.toArray(new DataHandler[dataHandlers.size()]);
          }
        }
      }
      else {
        try {
          //if the operation has a noun value type, unmarshall it from the body....
          nounValue = unmarshaller.unmarshal(request.getInputStream());
        }
        catch (Exception e) {
          //if we can't unmarshal the noun value, continue if the noun value is optional.
          if (!operation.isNounValueOptional()) {
            throw e;
          }
        }
      }
    }

    Object result = operation.invoke(properNounValue, contextParameterValues, adjectives, nounValue);
    return new ModelAndView(createView(operation, result));
  }

  /**
   * Create the REST view for the specified operation and result.
   *
   * @param operation The operation.
   * @param result    The result.
   * @return The view.
   */
  protected View createView(RESTOperation operation, Object result) {
    if (result instanceof DataHandler) {
      return createDataHandlerView(operation, (DataHandler) result);
    }
    else if (operation.isDeliversPayload()) {
      return createPayloadView(operation, result);
    }
    else {
      return createRESTView(operation, result);
    }
  }

  /**
   * Create the data handler view for the specified data handler.
   *
   * @param operation   The operation.
   * @param dataHandler The data handler.
   * @return The data handler.
   */
  protected RESTResultView createDataHandlerView(RESTOperation operation, DataHandler dataHandler) {
    return new DataHandlerView(operation, dataHandler, getNamespaces2Prefixes());
  }

  /**
   * Create the REST payload view for the specified operation and result.
   *
   * @param operation The operation.
   * @param result    The result of the invocation of the operation.
   * @return The payload view.
   */
  protected RESTResultView createPayloadView(RESTOperation operation, Object result) {
    return new RESTPayloadView(operation, result, getNamespaces2Prefixes());
  }

  /**
   * Create the REST view for the specified operation and result.
   *
   * @param operation The operation.
   * @param result    The result.
   * @return The view.
   */
  protected RESTResultView createRESTView(RESTOperation operation, Object result) {
    return new RESTResultView(operation, result, getNamespaces2Prefixes());
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

  /**
   * The map of namespaces to prefixes.
   *
   * @param ns2prefix The map of namespaces to prefixes.
   */
  public void setNamespaces2Prefixes(Map<String, String> ns2prefix) {
    this.ns2prefix = ns2prefix;
  }

  /**
   * The map of namespaces to prefixes.
   *
   * @return The map of namespaces to prefixes.
   */
  public Map<String, String> getNamespaces2Prefixes() {
    return this.ns2prefix;
  }

}
