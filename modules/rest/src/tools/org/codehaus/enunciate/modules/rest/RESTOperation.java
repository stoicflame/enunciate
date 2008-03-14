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

import org.codehaus.enunciate.rest.annotations.*;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * A REST operation.
 *
 * @author Ryan Heaton
 */
public class RESTOperation {

  private final VerbType verb;
  private final Object endpoint;
  final Method method;
  private final int properNounIndex;
  private final Class properNounType;
  private final Boolean properNounOptional;
  private final Map<String, Integer> adjectiveIndices;
  private final Map<String, Class> adjectiveTypes;
  private final Map<String, Boolean> adjectivesOptional;
  private final List<String> complexAdjectives;
  private final Map<String, Integer> contextParameterIndices;
  private final Map<String, Class> contextParameterTypes;
  private final int nounValueIndex;
  private final Class nounValueType;
  private final Boolean nounValueOptional;
  private final JAXBContext context;
  private final Class resultType;
  private final String contentType;
  private final String charset;
  private final String JSONPParameter;

  private final boolean deliversPayload;
  private final boolean deliversXMLPayload;
  private final Method payloadDeliveryMethod;
  private final Method payloadContentTypeMethod;
  private final Method payloadHeadersMethod;
  private final Method payloadXMLHintMethod;

  /**
   * Construct a REST operation.
   *
   * @param verb     The verb for the operation.
   * @param endpoint The REST endpoint.
   * @param method   The method.
   * @param parameterNames The parameter names.
   */
  protected RESTOperation(VerbType verb, Object endpoint, Method method, String[] parameterNames) {
    this.verb = verb;
    this.endpoint = endpoint;
    this.method = method;

    int properNounIndex = -1;
    Class properNoun = null;
    Boolean properNounOptional = null;
    int nounValueIndex = -1;
    Class nounValue = null;
    Boolean nounValueOptional = null;
    adjectiveTypes = new HashMap<String, Class>();
    adjectiveIndices = new HashMap<String, Integer>();
    adjectivesOptional = new HashMap<String, Boolean>();
    complexAdjectives = new ArrayList<String>();
    contextParameterTypes = new HashMap<String, Class>();
    contextParameterIndices = new HashMap<String, Integer>();
    Class[] parameterTypes = method.getParameterTypes();
    HashSet<Class> contextClasses = new HashSet<Class>();
    for (int i = 0; i < parameterTypes.length; i++) {
      Class parameterType = Collection.class.isAssignableFrom(parameterTypes[i]) ? getCollectionTypeAsArrayType(method, i) : parameterTypes[i];

      boolean isAdjective = true;
      String adjectiveName = "arg" + i;
      if ((parameterNames != null) && (parameterNames.length > i) && (parameterNames[i] != null)) {
        adjectiveName = parameterNames[i];
      }
      boolean adjectiveOptional = !parameterType.isPrimitive();
      boolean adjectiveComplex = false;
      Annotation[] parameterAnnotations = method.getParameterAnnotations()[i];
      for (Annotation annotation : parameterAnnotations) {
        if (annotation instanceof ProperNoun) {
          if (parameterType.isArray()) {
            throw new IllegalStateException("Proper nouns must be simple types, found an array or collection for parameter " + i + " of method " +
              method.getDeclaringClass().getName() + "." + method.getName() + ".");
          }
          else if (properNoun == null) {
            if (((ProperNoun) annotation).optional()) {
              if (parameterType.isPrimitive()) {
                throw new IllegalStateException("An optional proper noun cannot be a primitive type for method " +
                  method.getDeclaringClass().getName() + "." + method.getName() + ".");
              }

              properNounOptional = true;
            }

            properNoun = parameterType;
            properNounIndex = i;
            isAdjective = false;
            break;
          }
          else {
            throw new IllegalStateException("There are two proper nouns for method " + method.getDeclaringClass().getName() + "." + method.getName() + ".");
          }
        }
        else if (annotation instanceof NounValue) {
          if ((!parameterType.isAnnotationPresent(XmlRootElement.class)) && (!parameterType.equals(DataHandler.class)) &&
              (!(parameterType.isArray() && parameterType.getComponentType().equals(DataHandler.class)))) {
            throw new IllegalStateException("Noun values must be either XML root elements, javax.activation.DataHandler, javax.activation.DataHandler[], or a collection of javax.activation.DataHandler.  Invalid noun value for parameter " + i + " of method " +
              method.getDeclaringClass().getName() + "." + method.getName() + ": " + parameterType.getName() + ".");
          }
          else if (nounValue == null) {
            if (((NounValue) annotation).optional()) {
              if (parameterType.isPrimitive()) {
                throw new IllegalStateException("An optional noun value cannot be a primitive type for method " +
                  method.getDeclaringClass().getName() + "." + method.getName() + ".");
              }

              nounValueOptional = true;
            }

            nounValue = parameterType;
            nounValueIndex = i;
            isAdjective = false;
            break;
          }
          else {
            throw new IllegalStateException("There are two proper nouns for method " + method.getDeclaringClass().getName() + "." + method.getName() + ".");
          }
        }
        else if (annotation instanceof ContextParameter) {
          String contextParameterName = ((ContextParameter) annotation).value();
          contextParameterTypes.put(contextParameterName, parameterType);
          contextParameterIndices.put(contextParameterName, i);
          isAdjective = false;
          break;
        }
        else if (annotation instanceof Adjective) {
          Adjective adjectiveInfo = (Adjective) annotation;
          adjectiveOptional = adjectiveInfo.optional();
          if (adjectiveOptional && parameterType.isPrimitive()) {
            throw new IllegalStateException("An optional adjective cannot be a primitive type for method " +
              method.getDeclaringClass().getName() + "." + method.getName() + ".");
          }

          if (!"##default".equals(adjectiveInfo.name())) {
            adjectiveName = adjectiveInfo.name();
          }

          adjectiveComplex = adjectiveInfo.complex();

          break;
        }
      }

      if (isAdjective) {
        this.adjectiveTypes.put(adjectiveName, parameterType);
        this.adjectiveIndices.put(adjectiveName, i);
        this.adjectivesOptional.put(adjectiveName, adjectiveOptional);
        if (adjectiveComplex) {
          this.complexAdjectives.add(adjectiveName);
        }
      }

      if (parameterType.isArray()) {
        contextClasses.add(parameterType.getComponentType());
      }
      else {
        contextClasses.add(parameterType);
      }
    }

    boolean deliversPayload = false;
    boolean deliversXMLPayload = false;
    Method payloadDeliveryMethod = null;
    Method payloadContentTypeMethod = null;
    Method payloadHeadersMethod = null;
    Method payloadXMLHintMethod = null;
    Class returnType = null;
    if (!Void.TYPE.equals(method.getReturnType())) {
      returnType = method.getReturnType();

      if (returnType.isAnnotationPresent(org.codehaus.enunciate.rest.annotations.RESTPayload.class)) {
        //load the payload methods if its a payload.
        deliversPayload = true;
        deliversXMLPayload = ((org.codehaus.enunciate.rest.annotations.RESTPayload)returnType.getAnnotation(org.codehaus.enunciate.rest.annotations.RESTPayload.class)).xml();
        for (Method payloadMethod : returnType.getMethods()) {
          if (payloadMethod.isAnnotationPresent(RESTPayloadBody.class)) {
            if (payloadDeliveryMethod != null) {
              throw new IllegalStateException("There may only be one payload body method on a REST payload.");
            }
            else {
              if (payloadMethod.getParameterTypes().length > 0) {
                throw new IllegalStateException("A payload body method must have no parameters.");
              }

              Class bodyType = payloadMethod.getReturnType();
              if (byte[].class.isAssignableFrom(bodyType) || InputStream.class.isAssignableFrom(bodyType) || DataHandler.class.isAssignableFrom(bodyType)) {
                payloadDeliveryMethod = payloadMethod;
              }
              else {
                throw new IllegalStateException("A payload body must be of type byte[], javax.activation.DataHandler, or java.io.InputStream.");
              }
            }
          }

          if (payloadMethod.isAnnotationPresent(RESTPayloadContentType.class)) {
            if (payloadContentTypeMethod != null) {
              throw new IllegalStateException("There may only be one payload content type method on a REST payload.");
            }
            else if (payloadMethod.getParameterTypes().length > 0) {
              throw new IllegalStateException("A payload content type method must have no parameters.");
            }
            else if (!String.class.isAssignableFrom(payloadMethod.getReturnType())) {
              throw new IllegalStateException("The payload content type method must return a string.");
            }
            else {
              payloadContentTypeMethod = payloadMethod;
            }
          }

          if (payloadMethod.isAnnotationPresent(RESTPayloadHeaders.class)) {
            if (payloadHeadersMethod != null) {
              throw new IllegalStateException("There may only be one payload headers method on a REST payload.");
            }
            else if (payloadMethod.getParameterTypes().length > 0) {
              throw new IllegalStateException("A payload headers method must have no parameters.");
            }
            else if (!Map.class.isAssignableFrom(payloadMethod.getReturnType())) {
              throw new IllegalStateException("The payload headers method must return a map.");
            }
            else {
              payloadHeadersMethod = payloadMethod;
            }
          }

          if (payloadMethod.isAnnotationPresent(RESTPayloadXMLHint.class)) {
            if (payloadHeadersMethod != null) {
              throw new IllegalStateException("There may only be one payload XML hint method on a REST payload.");
            }
            else if (payloadMethod.getParameterTypes().length > 0) {
              throw new IllegalStateException("A payload XML hint method must have no parameters.");
            }
            else if ((Boolean.TYPE.equals(payloadMethod.getReturnType())) || (Boolean.class.equals(payloadMethod.getReturnType()))) {
              payloadXMLHintMethod = payloadMethod;
            }
            else {
              throw new IllegalStateException("The payload headers method must return a map.");
            }
          }
        }
      }
      else if (!returnType.isAnnotationPresent(XmlRootElement.class) && (!DataHandler.class.isAssignableFrom(returnType))) {
        throw new IllegalStateException("REST operation results must be xml root elements, REST payloads or instances of javax.activation.DataHandler.  Invalid return type for method " +
          method.getDeclaringClass() + "." + method.getName() + ".");
      }

      contextClasses.add(returnType);
    }

    String jsonpParameter = null;
    JSONP jsonpInfo = method.getAnnotation(JSONP.class);
    if (jsonpInfo == null) {
      jsonpInfo = method.getDeclaringClass().getAnnotation(JSONP.class);
      if (jsonpInfo == null) {
        jsonpInfo = method.getDeclaringClass().getPackage().getAnnotation(JSONP.class);
      }
    }
    if (jsonpInfo != null) {
      jsonpParameter = jsonpInfo.paramName();
    }

    this.properNounType = properNoun;
    this.properNounIndex = properNounIndex;
    this.properNounOptional = properNounOptional;
    this.nounValueType = nounValue;
    this.nounValueIndex = nounValueIndex;
    this.nounValueOptional = nounValueOptional;
    this.resultType = returnType;
    this.contentType = this.method.isAnnotationPresent(ContentType.class) ? this.method.getAnnotation(ContentType.class).value() : "text/xml";
    this.charset = this.method.isAnnotationPresent(ContentType.class) ? this.method.getAnnotation(ContentType.class).charset() : "utf-8";
    this.JSONPParameter = jsonpParameter;
    this.deliversPayload = deliversPayload;
    this.deliversXMLPayload = deliversXMLPayload;
    this.payloadDeliveryMethod = payloadDeliveryMethod;
    this.payloadContentTypeMethod = payloadContentTypeMethod;
    this.payloadHeadersMethod = payloadHeadersMethod;
    this.payloadXMLHintMethod = payloadXMLHintMethod;
    try {
      this.context = JAXBContext.newInstance(contextClasses.toArray(new Class[contextClasses.size()]));
    }
    catch (JAXBException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Gets the collection type for the specified parameter as an array type.
   *
   * @param method The method.
   * @param i      The parameter index.
   * @return The conversion.
   */
  protected Class getCollectionTypeAsArrayType(Method method, int i) {
    Class parameterType;
    Type collectionType = method.getGenericParameterTypes()[i];
    if (collectionType instanceof ParameterizedType) {
      Type[] typeArgs = ((ParameterizedType) collectionType).getActualTypeArguments();
      if (typeArgs.length < 1) {
        throw new IllegalStateException("A type parameter must be supplied to the collection for parameter " + i + " of method " +
          method.getDeclaringClass().getName() + "." + method.getName() + ".");
      }

      Class componentType = (Class) typeArgs[0];
      parameterType = Array.newInstance(componentType, 0).getClass();
    }
    else {
      throw new IllegalStateException("Non-parameterized collection found at parameter" + i + " on method " + method.getDeclaringClass().getName() +
        "." + method.getName() + ". Only parameterized collections are supported.");
    }
    return parameterType;
  }

  /**
   * Invokes the operation with the specified proper noun, adjectives, and noun value.
   *
   * @param properNoun The value for the proper noun.
   * @param adjectives The value for the adjectives.
   * @param nounValue  The value for the noun.
   * @return The result of the invocation.  If the invocation has no return type (void), returns null.
   * @throws Exception if any problems occur.
   */
  public Object invoke(Object properNoun, Map<String, Object> contextParameters, Map<String, Object> adjectives, Object nounValue) throws Exception {
    Class[] parameterTypes = this.method.getParameterTypes();
    Object[] parameters = new Object[parameterTypes.length];
    if (properNounIndex > -1) {
      parameters[properNounIndex] = properNoun;
    }

    if (nounValueIndex > -1) {
      if ((nounValue != null) && (Collection.class.isAssignableFrom(parameterTypes[nounValueIndex]))) {
        //convert the noun value back into a collection...
        Object[] values;
        try {
          values = (Object[]) nounValue;
        }
        catch (ClassCastException e) {
          throw new IllegalArgumentException("Noun value should be an array...");
        }

        Collection collection = newCollectionInstance(parameterTypes[nounValueIndex]);
        collection.addAll(Arrays.asList(values));
        nounValue = collection;
      }
      
      parameters[nounValueIndex] = nounValue;
    }

    for (String adjective : adjectiveIndices.keySet()) {
      Object adjectiveValue = adjectives.get(adjective);
      Integer index = adjectiveIndices.get(adjective);
      if ((adjectiveValue != null) && (Collection.class.isAssignableFrom(parameterTypes[index]))) {
        //convert the array back into a collection...
        Object[] values;
        try {
          values = (Object[]) adjectiveValue;
        }
        catch (ClassCastException e) {
          throw new IllegalArgumentException("Adjective '" + adjective + "' should be an array...");
        }

        Collection collection = newCollectionInstance(parameterTypes[index]);
        collection.addAll(Arrays.asList(values));
        adjectiveValue = collection;
      }

      parameters[index] = adjectiveValue;
    }

    for (String contextParameterName : contextParameterIndices.keySet()) {
      parameters[contextParameterIndices.get(contextParameterName)] = contextParameters.get(contextParameterName);
    }

    try {
      return this.method.invoke(this.endpoint, parameters);
    }
    catch (InvocationTargetException e) {
      Throwable target = e.getTargetException();
      if (target instanceof Error) {
        throw (Error) target;
      }

      throw (Exception) target;
    }
  }

  /**
   * Create a new instance of something of the specified collection type.
   *
   * @param collectionType The collection type.
   * @return the new instance.
   */
  public static Collection newCollectionInstance(Class collectionType) {
    if (Collection.class.isAssignableFrom(collectionType)) {
      Collection collection;
      if ((collectionType.isInterface()) || (Modifier.isAbstract(collectionType.getModifiers()))) {
        if (Set.class.isAssignableFrom(collectionType)) {
          if (SortedSet.class.isAssignableFrom(collectionType)) {
            collection = new TreeSet();
          }
          else {
            collection = new HashSet();
          }
        }
        else {
          collection = new ArrayList();
        }
      }
      else {
        try {
          collection = (Collection) collectionType.newInstance();
        }
        catch (Exception e) {
          throw new IllegalArgumentException("Unable to create an instance of " + collectionType.getName() + ".", e);
        }
      }
      return collection;
    }
    else {
      throw new IllegalArgumentException("Invalid list type: " + collectionType.getName());
    }
  }

  @Override
  public String toString() {
    return this.method.toString();
  }

  /**
   * The verb for the operation.
   *
   * @return The verb for the operation.
   */
  public VerbType getVerb() {
    return verb;
  }

  /**
   * If this operation accepts a proper noun, return the type of the proper noun.  Otherwise, return null.
   *
   * @return The proper noun type, or null.
   */
  public Class getProperNounType() {
    return properNounType;
  }

  /**
   * Whether the proper noun is optional.
   *
   * @return Whether the proper noun is optional, or null if no proper noun.
   */
  public Boolean isProperNounOptional() {
    return properNounOptional;
  }

  /**
   * If this operation accepts a noun value, return the type of the noun value.  Otherwise, return null.
   *
   * @return The noun value type, or null.
   */
  public Class getNounValueType() {
    return nounValueType;
  }

  /**
   * Whether the noun value is optional.
   *
   * @return Whether the noun value is optional, or null if no noun value.
   */
  public Boolean isNounValueOptional() {
    return nounValueOptional;
  }

  /**
   * The adjective types for this operation.
   *
   * @return The adjective types for this operation.
   */
  public Map<String, Class> getAdjectiveTypes() {
    return adjectiveTypes;
  }

  /**
   * List of adjectives that are complex.
   *
   * @return List of adjectives that are complex.
   */
  public List<String> getComplexAdjectives() {
    return complexAdjectives;
  }

  /**
   * The context parameter types for this operation.
   *
   * @return The context parameter types for this operation.
   */
  public Map<String, Class> getContextParameterTypes() {
    return contextParameterTypes;
  }

  /**
   * The map of whether the adjectives are optional.
   *
   * @return The map of whether the adjectives are optional.
   */
  public Map<String, Boolean> getAdjectivesOptional() {
    return adjectivesOptional;
  }

  /**
   * Gets the serialization context for this operation.
   *
   * @return The serialization context.
   */
  public JAXBContext getSerializationContext() {
    return context;
  }

  /**
   * The endpoint handling this REST operation.
   *
   * @return The endpoint handling this REST operation.
   */
  public Object getEndpoint() {
    return endpoint;
  }

  /**
   * The result type for the operation.
   *
   * @return The result type for the operation.
   */
  public Class getResultType() {
    return resultType;
  }

  /**
   * The content type of this REST operation.
   *
   * @return The content type of this REST operation.
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * The method supporting this operation.
   *
   * @return The method supporting this operation.
   */
  public Method getMethod() {
    return method;
  }

  /**
   * The character set of this REST operation.
   *
   * @return The character set of this REST operation.
   */
  public String getCharset() {
    return charset;
  }

  /**
   * The JSONP parameter name for this operation.
   *
   * @return The JSONP parameter name for this operation, or null if none.
   */
  public String getJSONPParameter() {
    return JSONPParameter;
  }

  /**
   * Whether this operation wraps a payload.
   *
   * @return Whether this operation wraps a payload.
   */
  public boolean isDeliversPayload() {
    return deliversPayload;
  }

  /**
   * Whether this operation wraps an XML payload.
   *
   * @return Whether this operation wraps an XML payload.
   */
  public boolean isDeliversXMLPayload() {
    return deliversXMLPayload;
  }

  /**
   * The method to use to get the payload body.
   *
   * @return The method to use to get the payload body.
   */
  public Method getPayloadDeliveryMethod() {
    return payloadDeliveryMethod;
  }

  /**
   * The method to use to get the payload content type.
   *
   * @return The method to use to get the payload content type.
   */
  public Method getPayloadContentTypeMethod() {
    return payloadContentTypeMethod;
  }

  /**
   * The method to use to get the payload xml hint.
   *
   * @return The method to use to get the payload xml hint.
   */
  public Method getPayloadXmlHintMethod() {
    return payloadXMLHintMethod;
  }

  /**
   * The method to use to get the payload content type.
   *
   * @return The method to use to get the payload content type.
   */
  public Method getPayloadHeadersMethod() {
    return payloadHeadersMethod;
  }
}
