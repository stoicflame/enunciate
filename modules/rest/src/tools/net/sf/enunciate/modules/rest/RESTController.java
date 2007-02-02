package net.sf.enunciate.modules.rest;

import net.sf.enunciate.rest.annotations.Noun;
import net.sf.enunciate.rest.annotations.RESTEndpoint;
import net.sf.enunciate.rest.annotations.Verb;
import net.sf.enunciate.rest.annotations.VerbType;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The controller for the REST API.
 *
 * @author Ryan Heaton
 */
public class RESTController extends AbstractController {

  private Object[] endpoints;
  private Class[] endpointClasses;
  private Map<String, RESTResource> RESTResources = new HashMap<String, RESTResource>();
  private Pattern urlPattern;
  private final DocumentBuilder documentBuilder;
  private HandlerExceptionResolver exceptionHandler;

  public RESTController() {
    setSubcontext("rest");
    setSupportedMethods(new String[] {"GET", "PUT", "POST", "DELETE"});

    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(false);
    try {
      documentBuilder = builderFactory.newDocumentBuilder();
    }
    catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }

    this.exceptionHandler = new RESTExceptionHandler();
  }

  /**
   * Sets up the controller for servicing the specified REST endpoints.
   *
   * @throws BeansException If there was a problem setting it up.
   */
  @Override
  protected void initApplicationContext() throws BeansException {
    super.initApplicationContext();

    if ((endpoints == null) && (endpointClasses != null)) {
      endpoints = new Object[endpointClasses.length];
      for (int i = 0; i < endpointClasses.length; i++) {
        Class endpointClass = endpointClasses[i];
        endpoints[i] = loadEndpointBean(endpointClass);
      }
    }

    if (endpoints != null) {
      for (Object endpoint : endpoints) {
        Collection<Class> endpointTypes = findEndpointTypes(endpoint);
        
        if (endpointTypes.isEmpty()) {
          throw new ApplicationContextException("REST endpoint " + endpoint.getClass().getName() + " does not implement any REST endpoint interfaces.");
        }

        for (Class endpointType: endpointTypes) {
          Method[] restMethods = endpointType.getDeclaredMethods();
          for (Method restMethod : restMethods) {
            int modifiers = restMethod.getModifiers();
            if ((Modifier.isPublic(modifiers)) && (restMethod.isAnnotationPresent(Verb.class)) && (!isImplMethod(restMethod, endpointTypes))) {
              VerbType verb = restMethod.getAnnotation(Verb.class).value();
              String noun = restMethod.isAnnotationPresent(Noun.class) ? restMethod.getAnnotation(Noun.class).value() : restMethod.getName();

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
   * Attempts to load the endpoint bean by first looking for beans that implement the specified endpoint class.
   * If there is only one, it will be used.  Otherwise, if there is more than one, it will attempt to find one that is named
   * the {@link net.sf.enunciate.rest.annotations.RESTEndpoint#name() same as the REST endpoint} or fail.  If there are
   * no endpoint beans in the context that can be assigned to the specified endpoint class, an attempt will be made to
   * instantiate one.
   *
   * @param endpointClass The class of the endpoint bean to attempt to load.
   * @return The endpoint bean.
   * @throws BeansException If an attempt was made to instantiate the bean but it failed.
   */
  protected Object loadEndpointBean(Class endpointClass) throws BeansException {
    Object endpointBean;
    Map endpointClassBeans = getApplicationContext().getBeansOfType(endpointClass);
    if (endpointClassBeans.size() > 0) {
      RESTEndpoint annotation = (RESTEndpoint) endpointClass.getAnnotation(RESTEndpoint.class);
      String endpointName = annotation == null ? "" : annotation.name();
      if (!"".equals(endpointName) && endpointClassBeans.containsKey(endpointName)) {
        //first attempt will be to load the bean identified by the endpoint name:
        endpointBean = endpointClassBeans.get(endpointName);
      }
      else if (endpointClassBeans.size() == 1) {
        // not there; use the only one if it exists...
        endpointBean = endpointClassBeans.values().iterator().next();
      }
      else {
        //panic: can't determine the endpoint bean to use.
        ArrayList beanNames = new ArrayList(endpointClassBeans.keySet());
        if ("".equals(endpointName)) {
          endpointName = "and supply an endpoint name with the @RESTEndpoint annotation";
        }
        throw new ApplicationContextException("There are more than one beans of type " + endpointClass.getName() +
          " in the application context " + beanNames + ".  Cannot determine which one to use to handle the REST requests.  " +
          "Either reduce the number of beans of this type to one, or specify which one to use by naming it the name of the REST endpoint (" +
          endpointName + ").");
      }
    }
    else {
      //try to instantiate the bean with the class...
      try {
        endpointBean = endpointClass.newInstance();
      }
      catch (Exception e) {
        throw new ApplicationContextException("Unable to instantiate REST endpoint bean of class " + endpointClass.getName() + ".", e);
      }
    }

    return endpointBean;
  }

  /**
   * Determines whether the specified rest method is an implementation of a method declared in one of
   * the specfied endpoint types.
   *
   * @param restMethod The method.
   * @param endpointTypes The endpoint types.
   * @return Whether it's an impl method.
   */
  protected boolean isImplMethod(Method restMethod, Collection<Class> endpointTypes) {
    if (restMethod.getDeclaringClass().isInterface()) {
      return false;
    }
    else {
      for (Class endpointType : endpointTypes) {
        if (!endpointType.isInterface()) {
          //only check the interfaces.
          continue;
        }

        try {
          endpointType.getMethod(restMethod.getName(), restMethod.getParameterTypes());
        }
        catch (NoSuchMethodException e) {
          continue;
        }
        
        return true;
      }

      return false;
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

    if (endpoint instanceof Advised) {
      endpointTypes.addAll(Arrays.asList(((Advised) endpoint).getProxiedInterfaces()));
    }
    else {
      Class endpointType = endpoint.getClass();
      if (endpointType.isAnnotationPresent(RESTEndpoint.class)) {
        endpointTypes.add(endpointType);
      }

      for (Class implementedType : endpointType.getInterfaces()) {
        if (implementedType.isAnnotationPresent(RESTEndpoint.class)) {
          endpointTypes.add(implementedType);
        }
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
    }

    try {
      return handleRESTOperation(noun, properNoun, verb, request, response);
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
   * @param noun The noun.
   * @param properNoun The proper noun, if supplied by the request.
   * @param verb The verb.
   * @param request The request.
   * @param response The response.
   * @return The model and view.
   */
  protected ModelAndView handleRESTOperation(String noun, String properNoun, VerbType verb, HttpServletRequest request, HttpServletResponse response) throws Exception {
    if (noun == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "A REST resource must be specified.");
      return null;
    }

    RESTResource resource = RESTResources.get(noun);
    if (resource == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown REST resource: " + noun);
      return null;
    }

    RESTOperation operation = resource.getOperation(verb);
    if (operation == null) {
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
    return new ModelAndView(new RESTResultView(operation, result));
  }

  /**
   * Gets the REST resources for this controller.
   *
   * @return The REST resources for this controller.
   */
  public Map<String, RESTResource> getRESTResources() {
    return Collections.unmodifiableMap(RESTResources);
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
   * The REST endpoints that make up this API.
   *
   * @param endpoints The REST endpoints that make up this API.
   */
  public void setEndpoints(Object[] endpoints) {
    this.endpoints = endpoints;
  }

  /**
   * The classes that make up this REST API.
   *
   * @return The classes that make up this REST API.
   */
  public Class[] getEndpointClasses() {
    return endpointClasses;
  }

  /**
   * The classes that make up this REST API.
   *
   * @param endpointClasses The classes that make up this REST API.
   */
  public void setEndpointClasses(Class[] endpointClasses) {
    this.endpointClasses = endpointClasses;
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

  /**
   * Set the the resolver for the case that an exception is thrown.
   *
   * @param exceptionHandler The exception handler.
   */
  public void setExceptionHandler(HandlerExceptionResolver exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }
}
