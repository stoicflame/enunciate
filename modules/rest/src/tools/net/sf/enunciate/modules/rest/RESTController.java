package net.sf.enunciate.modules.rest;

import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.w3c.dom.Document;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import net.sf.enunciate.rest.annotations.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * The controller for the REST API.
 *
 * @author Ryan Heaton
 */
public class RESTController extends AbstractController {

  private final Object[] endpoints;
  private Map<String, RESTResource> RESTResources = new HashMap<String, RESTResource>();
  private Pattern urlPattern;

  public RESTController(Object[] endpoints) {
    this.endpoints = endpoints;
    setSubcontext("rest");
  }

  /**
   * Sets up the controller for servicing the specified REST endpoints.
   *
   * @throws BeansException If there was a problem setting it up.
   */
  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    if (endpoints != null) {
      for (Object endpoint : endpoints) {
        Collection<Class> endpointTypes = findEndpointTypes(endpoint);
        
        if (endpointTypes.isEmpty()) {
          throw new ApplicationContextException("REST endpoint " + endpoint.getClass().getName() + " does not implement any REST endpoints.");
        }

        for (Class endpointType: endpointTypes) {
          Method[] restMethods = endpointType.getMethods();
          for (Method restMethod : restMethods) {
            int modifiers = restMethod.getModifiers();
            if ((Modifier.isPublic(modifiers)) && (!Modifier.isAbstract(modifiers)) && (!restMethod.isAnnotationPresent(Exclude.class))) {
              String noun = restMethod.isAnnotationPresent(Noun.class) ? restMethod.getAnnotation(Noun.class).value() : restMethod.getName();
              VerbType verb = restMethod.isAnnotationPresent(Verb.class) ? restMethod.getAnnotation(Verb.class).value() : VerbType.read;

              RESTResource resource = RESTResources.get(noun);
              if (resource == null) {
                resource = new RESTResource(noun);
                RESTResources.put(noun, resource);
              }

              if (!resource.addOperation(verb, endpoint, restMethod)) {
                RESTOperation duplicateOperation = resource.getOperation(verb);

                throw new ApplicationContextException("Noun '" + noun + "' has more than one '" + verb + "' verbs.  One was found at " +
                  restMethod.getDeclaringClass().getName() + "." + restMethod.getName() + ", the other at " +
                  duplicateOperation.method.getDeclaringClass().getName() + "." + duplicateOperation.method.getName() + ".");
              }
            }
          }
        }
      }
    }
  }

  /**
   * Finds the endpoint types that the specified object implements.
   *
   * @param endpoint The endpoint object.
   * @return The endpoint types.
   */
  protected Collection<Class> findEndpointTypes(Object endpoint) {
    Collection<Class> endpointTypes = new ArrayList<Class>();

    Class endpointType = endpoint.getClass();
    if (endpointType.isAnnotationPresent(RESTEndpoint.class)) {
      endpointTypes.add(endpointType);
    }

    for (Class implementedType : endpointType.getInterfaces()) {
      if (implementedType.isAnnotationPresent(RESTEndpoint.class)) {
        endpointTypes.add(implementedType);
      }
    }

    return endpointTypes;
  }

  /**
   * Extracts the noun and (possible) proper noun from the request and handles the REST operation.
   *
   * @param request The request.
   * @param response The response.
   * @return The model and view.
   */
  protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String noun = null;
    String properNoun = null;
    Matcher matcher = urlPattern.matcher(request.getRequestURI());
    if (matcher.find()) {
      noun = matcher.group(1);
      properNoun = matcher.group(2);

      if ("".equals(properNoun)) {
        properNoun = null;
      }
    }

    return handleRESTOperation(noun, properNoun, request, response);
  }

  protected ModelAndView handleRESTOperation(String noun, String properNoun, HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (noun == null) {
      //todo: think about spitting out the documentation in this case?
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "A REST resource must be specified.");
      return null;
    }

    RESTResource resource = RESTResources.get(noun);
    if (resource == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown REST resource: " + noun);
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

    RESTOperation operation = resource.getOperation(verb);
    if (operation == null) {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Unsupported HTTP method: " + httpMethod);
      return null;
    }

    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(false);
    DocumentBuilder builder = builderFactory.newDocumentBuilder();
    Document document = builder.newDocument();

    Unmarshaller unmarshaller = operation.getSerializationContext().createUnmarshaller();
    unmarshaller.setAttachmentUnmarshaller(RESTAttachmentUnmarshaller.INSTANCE);

    Object properNounValue = null;
    if ((properNoun != null) && (operation.getProperNounType() != null)) {
      properNounValue = unmarshaller.unmarshal(document.createTextNode(properNoun), operation.getProperNounType()).getValue();
    }

    HashMap<String, Object> adjectives = new HashMap<String, Object>();
    for (String adjective : operation.getAdjectiveTypes().keySet()) {
      Object adjectiveValue = null;

      String[] parameterValues = request.getParameterValues(adjective);
      if ((parameterValues != null) && (parameterValues.length > 0)) {
        Object[] adjectiveValues = new Object[parameterValues.length];
        Class adjectiveType = operation.getAdjectiveTypes().get(adjective);
        if (adjectiveType.isArray()) {
          adjectiveType = adjectiveType.getComponentType();
        }

        for (int i = 0; i < parameterValues.length; i++) {
          adjectiveValues[i] = unmarshaller.unmarshal(document.createTextNode(parameterValues[i]), adjectiveType).getValue();
        }

        if (adjectiveType.isArray()) {
          adjectiveValue = adjectiveValues;
        }
        else {
          adjectiveValue = adjectiveValues[0];
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
    return new ModelAndView(new RESTResultView(operation, result));
  }

  /**
   * Gets the REST resources for this controller.
   *
   * @return The REST resources for this controller.
   */
  public Map<String, RESTResource> getRESTResources() {
    return RESTResources;
  }

  /**
   * The REST endpoints that make up this API.
   *
   * @return The REST endpoints that make up this API.
   */
  public Object[] getEndpoints() {
    return endpoints;
  }

  /**
   * The REST controller needs to know under what subcontext it is mounted in order to
   * parse out the noun and proper noun from the request.
   *
   * @param subcontext The subcontext.  Default: "rest"
   */
  public void setSubcontext(String subcontext) {
    urlPattern = Pattern.compile(subcontext + "/([^/]+)/?(.*)$");
  }
}
