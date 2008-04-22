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

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.springframework.web.servlet.mvc.AbstractController;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * A exporter for a REST resource.
 *
 * @author Ryan Heaton
 */
public class RESTResourceExporter extends AbstractController {

  public static final Log LOG = LogFactory.getLog(RESTResourceExporter.class);

  private final RESTResource resource;
  private final RESTRequestContentTypeHandler contentTypeHandler;
  private final String contentType;
  private HandlerExceptionResolver exceptionHandler;
  private String[] supportedMethods;
  private MultipartRequestHandler multipartRequestHandler;
  private Map<String, String> namespaces2Prefixes = new HashMap<String, String>();

  public RESTResourceExporter(RESTResource resource, RESTRequestContentTypeHandler contentTypeHandler, String contentType) {
    this.resource = resource;
    this.contentTypeHandler = contentTypeHandler;
    this.contentType = contentType;
  }

  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    if (resource == null) {
      throw new ApplicationContextException("A REST resource must be provided.");
    }

    Set<VerbType> supportedVerbs = resource.getSupportedVerbs(contentType);
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

    if (this.contentTypeHandler instanceof RESTResourceAware) {
      ((RESTResourceAware)this.contentTypeHandler).setRESTResource(resource);
    }

    if (this.contentTypeHandler instanceof NamespacePrefixesAware) {
      ((NamespacePrefixesAware)this.contentTypeHandler).setNamespacesToPrefixes(getNamespaces2Prefixes());
    }

    if (this.contentTypeHandler instanceof ContentTypeAware) {
      ((ContentTypeAware)this.contentTypeHandler).setContentType(getContentType());
    }
  }

  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (!Arrays.asList(this.supportedMethods).contains(request.getMethod().toUpperCase())) {
      throw new MethodNotAllowedException(this.supportedMethods);
    }

    VerbType verb = getVerb(request);

    try {
      return handleRESTOperation(verb, request, response);
    }
    catch (Exception e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error invoking REST operation.", e);
      }

      if (getExceptionHandler() != null) {
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
   * @throws org.codehaus.enunciate.modules.rest.MethodNotAllowedException If the verb isn't recognized.
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
    RESTOperation operation = resource.getOperation(contentType, verb);
    if (!isOperationAllowed(operation)) {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Unsupported verb: " + verb);
      return null;
    }

    if ((this.multipartRequestHandler != null) && (this.multipartRequestHandler.isMultipart(request))) {
      request = this.multipartRequestHandler.handleMultipartRequest(request);
    }

    return handleRESTRequest(new RESTRequest(request, operation, getContentType()), response);
  }

  /**
   * Handle the specified REST request.
   *
   * @param request The request.
   * @param response The response.
   * @return The model and view.
   */
  protected ModelAndView handleRESTRequest(RESTRequest request, HttpServletResponse response) throws Exception {
    RESTOperation operation = request.getOperation();

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
      String parameterName = entry.getKey();
      String parameterValue = entry.getValue();
      if (parameterName == null) {
        Class nounType = operation.getProperNounType();
        if (nounType != null) {
          //todo: provide a hook to some other conversion mechanism?
          try {
            properNounValue = ConvertUtils.convert(parameterValue, nounType);
          }
          catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter value '" + parameterValue + "' on URL.");
            return null;
          }
        }
      }
      else {
        Class contextParameterType = operation.getContextParameterTypes().get(parameterName);
        if (contextParameterType != null) {
          //todo: provide a hook to some other conversion mechanism?
          try {
            contextParameterValues.put(parameterName, ConvertUtils.convert(parameterValue, contextParameterType));
          }
          catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter value '" + parameterValue + "' on URL.");
            return null;
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
          //todo: provide a hook to some other conversion mechanism?
          final Class adjectiveType = operation.getAdjectiveTypes().get(adjective);
          Object[] adjectiveValues = new Object[parameterValues.length];
          for (int i = 0; i < parameterValues.length; i++) {
            try {
              adjectiveValues[i] = ConvertUtils.convert(parameterValues[i], adjectiveType);
            }
            catch (Exception e) {
              response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid value '" + parameterValues[i] + "' for parameter '" + adjective + "'.");
              return null;
            }
          }

          if (adjectiveType.isArray()) {
            adjectiveValue = adjectiveValues;
          }
          else {
            adjectiveValue = adjectiveValues[0];
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
          nounValue = getContentTypeHandler().read(request);
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

    //successful invocation, set up the response...
    response.setContentType(String.format("%s;charset=%s", this.contentType, operation.getCharset()));
    if (result instanceof DataHandler) {
      ((DataHandler)result).writeTo(response.getOutputStream());
    }
    else {
      this.contentTypeHandler.write(result, request, response);
    }
    return null;
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
   * Whether the specified operation is allowed.
   *
   * @param operation The operation to test whether it is allowed.
   * @return Whether the specified operation is allowed.
   */
  protected boolean isOperationAllowed(RESTOperation operation) {
    return operation != null;
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

  /**
   * The resource associated with this exporter.
   *
   * @return The resource associated with this exporter.
   */
  public RESTResource getResource() {
    return resource;
  }

  /**
   * The data format handler used by this exporter.
   *
   * @return The data format handler used by this exporter.
   */
  public RESTRequestContentTypeHandler getContentTypeHandler() {
    return contentTypeHandler;
  }

  /**
   * The data format handled by this exporter.
   *
   * @return The data format handled by this exporter.
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * The exception handler.
   *
   * @return The exception handler.
   */
  public HandlerExceptionResolver getExceptionHandler() {
    return exceptionHandler;
  }

  /**
   * The exception handler.
   *
   * @param exceptionHandler The exception handler.
   */
  public void setExceptionHandler(HandlerExceptionResolver exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }

  /**
   * The map of namespaces to prefixes.
   *
   * @return The map of namespaces to prefixes.
   */
  public Map<String, String> getNamespaces2Prefixes() {
    return namespaces2Prefixes;
  }

  /**
   * The map of namespaces to prefixes.
   *
   * @param namespaces2Prefixes The map of namespaces to prefixes.
   */
  public void setNamespaces2Prefixes(Map<String, String> namespaces2Prefixes) {
    this.namespaces2Prefixes = namespaces2Prefixes;
  }
}