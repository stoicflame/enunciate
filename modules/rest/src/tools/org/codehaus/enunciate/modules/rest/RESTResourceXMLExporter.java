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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContextException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.lang.reflect.Array;
import java.util.*;
import java.io.IOException;

/**
 * An xml exporter for a REST resource.
 *
 * @author Ryan Heaton
 */
public class RESTResourceXMLExporter extends AbstractController {

  private final DocumentBuilder documentBuilder;
  private final RESTResource resource;
  private HandlerExceptionResolver exceptionHandler;
  private Map<String, String> ns2prefix;
  private String[] supportedMethods;
  private MultipartRequestHandler multipartRequestHandler;

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
        default:
          method = supportedVerb.toString().toUpperCase();
      }
      supportedMethods[i++] = method;
    }
    this.supportedMethods = supportedMethods;

    if (this.multipartRequestHandler == null) {
      Map resolverBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(getApplicationContext(), MultipartRequestHandler.class);
      if (resolverBeans.size() > 0) {
        //todo: add a configuration element to specify which one to use.
        this.multipartRequestHandler = (MultipartRequestHandler) resolverBeans.values().iterator().next();
      }
      else {
        DefaultMultipartRequestHandler defaultMultipartHandler = new DefaultMultipartRequestHandler();
        if (getApplicationContext() instanceof WebApplicationContext) {
          defaultMultipartHandler.setServletContext(getServletContext());
        }
        this.multipartRequestHandler = defaultMultipartHandler;
      }
    }

    super.setSupportedMethods(new String[]{"GET", "PUT", "POST", "DELETE"});

    if (this.exceptionHandler == null) {
      this.exceptionHandler = new JaxbXmlExceptionHandler(getNamespaces2Prefixes());
    }
  }

  public RESTResource getResource() {
    return resource;
  }

  public HandlerExceptionResolver getExceptionHandler() {
    return exceptionHandler;
  }

  public void setExceptionHandler(HandlerExceptionResolver exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }

  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    VerbType verb = getVerb(request);

    if (!resource.getSupportedVerbs().contains(verb)) {
      throw new MethodNotAllowedException(this.supportedMethods);
    }

    try {
      return handleRESTOperation(verb, request, response);
    }
    catch (Exception e) {
      if (getExceptionHandler() != null) {
        request.setAttribute(VerbType.class.getName(), verb);
        return getExceptionHandler().resolveException(request, response, this, e);
      }
      else {
        throw e;
      }
    }
  }

  /**
   * Gets the verb from an HTTP Servlet Request.
   *
   * @param request The request.
   * @return The verb.
   * @throws MethodNotAllowedException If the verb isn't recognized.
   */
  public VerbType getVerb(HttpServletRequest request) throws MethodNotAllowedException {
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
    
    return verb;
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
    request.setAttribute(RESTOperation.class.getName(), operation);

    if ((this.multipartRequestHandler != null) && (this.multipartRequestHandler.isMultipart(request))) {
      request = this.multipartRequestHandler.handleMultipartRequest(request);
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
      Class nounValueType = operation.getNounValueType();
      if ((nounValueType.equals(DataHandler.class)) || ((nounValueType.isArray() && nounValueType.getComponentType().equals(DataHandler.class)))) {
        Collection<DataHandler> dataHandlers;
        if (this.multipartRequestHandler != null) {
          dataHandlers = this.multipartRequestHandler.parseParts(request);
        }
        else {
          dataHandlers = new ArrayList<DataHandler>();
          dataHandlers.add(new DataHandler(new RESTRequestDataSource(request, resource.getNounContext() + resource.getNoun())));
        }

        nounValue = dataHandlers;
        if (operation.getNounValueType().equals(DataHandler.class)) {
          nounValue = dataHandlers.iterator().next();
        }
        else if (mustConvertNounValueToArray(operation)) {
          Class type = operation.getNounValueType();
          if ((type.isArray() && type.getComponentType().equals(DataHandler.class))) {
            nounValue = dataHandlers.toArray(new DataHandler[dataHandlers.size()]);
          }
        }
      }
      else {
        try {
          //if the operation has a noun value type, unmarshall it from the body....
          nounValue = unmarshalNounValue(request, unmarshaller);
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
    View view;
    if (result instanceof DataHandler) {
      view = createDataHandlerView(operation, request);
    }
    else if (operation.isDeliversPayload()) {
      view = createPayloadView(operation, request);
    }
    else {
      view = createRESTView(operation, request);
    }

    TreeMap<String, Object> model = new TreeMap<String, Object>();
    model.put(RESTOperationView.MODEL_RESULT, result);
    return new ModelAndView(view, model);
  }

  /**
   * Whether the noun value should be converted to an array for the given operation.
   *
   * @param operation The operation.
   * @return Whether the noun value should be converted to an array.
   */
  protected boolean mustConvertNounValueToArray(RESTOperation operation) {
    return operation.getMethod().getParameterTypes()[operation.getNounValueIndex()].isArray();
  }

  /**
   * Unmarshal the noun value from the request given the specified unmarshaller.
   *
   * @param request The request.
   * @param unmarshaller The unmarshaller.
   * @return The noun value.
   */
  protected Object unmarshalNounValue(HttpServletRequest request, Unmarshaller unmarshaller) throws JAXBException, IOException, XMLStreamException {
    return unmarshaller.unmarshal(request.getInputStream());
  }

  /**
   * Create the data handler view for the specified data handler.
   *
   * @param operation The operation.
   * @param request The request for which to create the view.
   * @return The data handler.
   */
  protected BasicRESTView createDataHandlerView(RESTOperation operation, HttpServletRequest request) {
    return new DataHandlerView(operation);
  }

  /**
   * Create the REST payload view for the specified operation and result.
   *
   * @param operation The operation.
   * @param request The request for which to create the view.
   * @return The payload view.
   */
  protected BasicRESTView createPayloadView(RESTOperation operation, HttpServletRequest request) {
    return new RESTPayloadView(operation);
  }

  /**
   * Create the REST view for the specified operation and result.
   *
   * @param operation The operation.
   * @param request The request for which to create the view.
   * @return The view.
   */
  protected BasicRESTView createRESTView(RESTOperation operation, HttpServletRequest request) {
    return new JaxbXmlView(operation, getNamespaces2Prefixes());
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

  /**
   * The multipart request handler.
   *
   * @return The multipart request handler.
   */
  public MultipartRequestHandler getMultipartRequestHandler() {
    return multipartRequestHandler;
  }

  /**
   * The multipart request handler.
   *
   * @param multipartRequestHandler The multipart request handler.
   */
  public void setMultipartRequestHandler(MultipartRequestHandler multipartRequestHandler) {
    this.multipartRequestHandler = multipartRequestHandler;
  }
}
