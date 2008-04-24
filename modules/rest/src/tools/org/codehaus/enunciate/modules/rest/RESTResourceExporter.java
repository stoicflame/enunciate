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
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.reflect.Array;

/**
 * A exporter for a REST resource.
 *
 * @author Ryan Heaton
 */
public class RESTResourceExporter extends AbstractController {

  public static final Log LOG = LogFactory.getLog(RESTResourceExporter.class);

  private final RESTResource resource;
  private MultipartRequestHandler multipartRequestHandler;
  private ContentTypeSupport contentTypeSupport;
  private Pattern contentTypeIdPattern = Pattern.compile("^/?([^/]+)");

  public RESTResourceExporter(RESTResource resource) {
    this.resource = resource;
    super.setSupportedMethods(new String[]{"GET", "PUT", "POST", "DELETE"});
  }

  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    if (resource == null) {
      throw new ApplicationContextException("A REST resource must be provided.");
    }

    if (contentTypeSupport == null) {
      throw new ApplicationContextException("No content type support was supplied.");
    }

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
  }

  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    request.setAttribute(RESTResource.class.getName(), getResource());
    
    String contentTypeId = findContentTypeId(request);
    String contentType = getContentTypeSupport().lookupContentTypeById(contentTypeId);
    VerbType verb = getVerb(request);
    RESTOperation operation = getResource().getOperation(contentType, verb);

    if (operation == null) {
      throw new MethodNotAllowedException("Method not allowed for content type '" + contentType + "'.");
    }
    request.setAttribute(RESTOperation.class.getName(), operation);

    RESTRequestContentTypeHandler handler = getContentTypeSupport().lookupHandlerById(contentTypeId);
    if (handler == null) {
      throw new IllegalStateException("No handler found for content type " + contentTypeId + " (" + contentType + ").");
    }
    request.setAttribute(RESTRequestContentTypeHandler.class.getName(), handler);

    return handleRESTOperation(operation, handler, request, response);
  }

  /**
   * Find the content type id from the request.
   *
   * @param request The request from which to lookup the content type id.
   * @return The content type id.
   */
  protected String findContentTypeId(HttpServletRequest request) {
    String requestContext = request.getRequestURI().substring(request.getContextPath().length());
    Matcher matcher = getContentTypeIdPattern().matcher(requestContext);
    String contentTypeId = null;
    if (matcher.find()) {
      contentTypeId = matcher.group(1);
    }
    else {
      if (LOG.isErrorEnabled()) {
        LOG.error("No content type id found in request context " + requestContext);
      }
    }
    return contentTypeId;
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
      throw new MethodNotAllowedException("Method not supported: " + httpMethod);
    }

    return verb;
  }

  /**
   * Handles a specific REST operation.
   *
   * @param operation The operation.
   * @param handler The handler for the operation.
   * @param request  The request.
   * @param response The response.
   * @return The model and view.
   */
  protected ModelAndView handleRESTOperation(RESTOperation operation, RESTRequestContentTypeHandler handler, HttpServletRequest request, HttpServletResponse response) throws Exception {

    if ((this.multipartRequestHandler != null) && (this.multipartRequestHandler.isMultipart(request))) {
      request = this.multipartRequestHandler.handleMultipartRequest(request);
    }

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
          Class componentType = adjectiveType.isArray() ? adjectiveType.getComponentType() : adjectiveType;
          Object adjectiveValues = Array.newInstance(componentType, parameterValues.length);
          for (int i = 0; i < parameterValues.length; i++) {
            try {
              Array.set(adjectiveValues, i, ConvertUtils.convert(parameterValues[i], componentType));
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
          nounValue = handler.read(request);
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
    response.setContentType(String.format("%s;charset=%s", operation.getContentType(), operation.getCharset()));
    if (result instanceof DataHandler) {
      ((DataHandler)result).writeTo(response.getOutputStream());
    }
    else {
      handler.write(result, request, response);
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
   * The content type support.
   *
   * @return The content type support.
   */
  public ContentTypeSupport getContentTypeSupport() {
    return contentTypeSupport;
  }

  /**
   * Set the content type support.
   *
   * @param support the content type support.
   */
  public void setContentTypeSupport(ContentTypeSupport support) {
    this.contentTypeSupport = support;
  }

  /**
   * The pattern for matching the content type id from a request URL.
   *
   * @return The pattern for matching the content type id from a request URL.
   */
  public Pattern getContentTypeIdPattern() {
    return contentTypeIdPattern;
  }

  /**
   * The pattern for matching the content type id from a request URL.
   *
   * @param contentTypeIdPattern The pattern for matching the content type id from a request URL.
   */
  public void setContentTypeIdPattern(Pattern contentTypeIdPattern) {
    this.contentTypeIdPattern = contentTypeIdPattern;
  }
}