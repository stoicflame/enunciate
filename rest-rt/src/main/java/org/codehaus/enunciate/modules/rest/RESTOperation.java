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
import org.apache.commons.beanutils.Converter;
import org.codehaus.enunciate.rest.annotations.*;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * A REST operation.
 *
 * @author Ryan Heaton
 */
public class RESTOperation {

  private final RESTResource resource;
  private final VerbType verb;
  final Method method;
  private final int properNounIndex;
  private final Class properNounType;
  private final Boolean properNounOptional;
  private final int contentTypeParameterIndex;
  private final Map<String, Integer> adjectiveIndices;
  private final Map<String, Class> adjectiveTypes;
  private final Map<String, Boolean> adjectivesOptional;
  private final List<String> complexAdjectives;
  private final Map<String, Integer> contextParameterIndices;
  private final Map<String, Class> contextParameterTypes;
  private final int nounValueIndex;
  private final Class nounValueType;
  private final Boolean nounValueOptional;
  private final Class resultType;
  private final String contentType;
  private final String charset;
  private final String JSONPParameter;
  private final Set<Class> contextClasses;

  /**
   * Construct a REST operation.
   *
   * @param resource The resource for this operation.
   * @param contentType The content type of the operation.
   * @param verb     The verb for the operation.
   * @param method   The method.
   * @param parameterNames The parameter names.
   */
  protected RESTOperation(RESTResource resource, String contentType, VerbType verb, Method method, String[] parameterNames) {
    this.resource = resource;
    this.verb = verb;
    this.method = method;
    this.contentType = contentType;

    int properNounIndex = -1;
    Class properNoun = null;
    Boolean properNounOptional = null;
    int nounValueIndex = -1;
    Class nounValue = null;
    Boolean nounValueOptional = Boolean.FALSE;
    int contentTypeParameterIndex = -1;
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
            ProperNoun properNounInfo = (ProperNoun) annotation;
            if (properNounInfo.optional()) {
              if (parameterType.isPrimitive()) {
                throw new IllegalStateException("An optional proper noun cannot be a primitive type for method " +
                  method.getDeclaringClass().getName() + "." + method.getName() + ".");
              }

              properNounOptional = true;
            }

            if (!properNounInfo.converter().equals(ProperNoun.DEFAULT.class)) {
              try {
                ConvertUtils.register((Converter) properNounInfo.converter().newInstance(), parameterType);
              }
              catch (ClassCastException e) {
                throw new IllegalArgumentException("Illegal converter class for method " +
                  method.getDeclaringClass().getName() + "." + method.getName() + ". Must be an instance of org.apache.commons.beanutils.Converter.");
              }
              catch (Exception e) {
                throw new IllegalArgumentException("Unable to instantiate converter class " + properNounInfo.converter().getName() + " on method " +
                  method.getDeclaringClass().getName() + "." + method.getName() + ".", e);
              }
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
          ContextParameter contextParameterInfo = (ContextParameter) annotation;
          String contextParameterName = contextParameterInfo.value();

          if (!contextParameterInfo.converter().equals(ContextParameter.DEFAULT.class)) {
            try {
              ConvertUtils.register((Converter) contextParameterInfo.converter().newInstance(), parameterType);
            }
            catch (ClassCastException e) {
              throw new IllegalArgumentException("Illegal converter class for method " +
                method.getDeclaringClass().getName() + "." + method.getName() + ". Must be an instance of org.apache.commons.beanutils.Converter.");
            }
            catch (Exception e) {
              throw new IllegalArgumentException("Unable to instantiate converter class " + contextParameterInfo.converter().getName() + " on method " +
                method.getDeclaringClass().getName() + "." + method.getName() + ".", e);
            }
          }

          contextParameterTypes.put(contextParameterName, parameterType);
          contextParameterIndices.put(contextParameterName, i);
          isAdjective = false;
          break;
        }
        else if (annotation instanceof ContentTypeParameter) {
          contentTypeParameterIndex = i;
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

          if (!adjectiveInfo.converter().equals(Adjective.DEFAULT.class)) {
            try {
              ConvertUtils.register((Converter) adjectiveInfo.converter().newInstance(), parameterType);
            }
            catch (ClassCastException e) {
              throw new IllegalArgumentException("Illegal converter class for method " +
                method.getDeclaringClass().getName() + "." + method.getName() + ". Must be an instance of org.apache.commons.beanutils.Converter.");
            }
            catch (Exception e) {
              throw new IllegalArgumentException("Unable to instantiate converter class " + adjectiveInfo.converter().getName() + " on method " +
                method.getDeclaringClass().getName() + "." + method.getName() + ".", e);
            }
          }

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

    Class returnType = null;
    if (!Void.TYPE.equals(method.getReturnType())) {
      returnType = method.getReturnType();

      if (!returnType.isAnnotationPresent(XmlRootElement.class) && (!DataHandler.class.isAssignableFrom(returnType))) {
        throw new IllegalStateException("REST operation results must be xml root elements or instances of javax.activation.DataHandler.  Invalid return type for method " +
          method.getDeclaringClass() + "." + method.getName() + ".");
      }

      contextClasses.add(returnType);
    }

    for (Class exceptionClass : method.getExceptionTypes()) {
      for (Method exceptionMethod : exceptionClass.getMethods()) {
        if ((exceptionMethod.isAnnotationPresent(RESTErrorBody.class)) && (exceptionMethod.getReturnType() != Void.TYPE)) {
          //add the error body to the context classes.
          contextClasses.add(exceptionMethod.getReturnType());
        }
      }
    }

    //now load any additional context classes as specified by @RESTSeeAlso
    if (method.isAnnotationPresent(RESTSeeAlso.class)) {
      contextClasses.addAll(Arrays.asList(method.getAnnotation(RESTSeeAlso.class).value()));
    }
    if (method.getDeclaringClass().isAnnotationPresent(RESTSeeAlso.class)) {
      contextClasses.addAll(Arrays.asList(method.getDeclaringClass().getAnnotation(RESTSeeAlso.class).value()));
    }
    if ((method.getDeclaringClass().getPackage() != null) && (method.getDeclaringClass().getPackage().isAnnotationPresent(RESTSeeAlso.class))) {
      contextClasses.addAll(Arrays.asList(method.getDeclaringClass().getPackage().getAnnotation(RESTSeeAlso.class).value()));
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

    String charset = "utf-8";
    org.codehaus.enunciate.rest.annotations.ContentType contentTypeInfo = method.getAnnotation(org.codehaus.enunciate.rest.annotations.ContentType.class);
    if (contentTypeInfo == null) {
      contentTypeInfo = method.getDeclaringClass().getAnnotation(org.codehaus.enunciate.rest.annotations.ContentType.class);
      if (contentTypeInfo == null) {
        contentTypeInfo = method.getDeclaringClass().getPackage().getAnnotation(org.codehaus.enunciate.rest.annotations.ContentType.class);
      }
    }
    if (contentTypeInfo != null) {
      charset = contentTypeInfo.charset();
    }

    this.properNounType = properNoun;
    this.properNounIndex = properNounIndex;
    this.properNounOptional = properNounOptional;
    this.nounValueType = nounValue;
    this.nounValueIndex = nounValueIndex;
    this.nounValueOptional = nounValueOptional;
    this.resultType = returnType;
    this.charset = charset;
    this.JSONPParameter = jsonpParameter;
    this.contextClasses = contextClasses;
    this.contentTypeParameterIndex = contentTypeParameterIndex;
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
   * Invokes the operation with the specified proper noun, adjectives, and noun value on the given endpoint.
   *
   * @param properNoun The value for the proper noun.
   * @param contextParameters The context parametesr for this operation.
   * @param adjectives The value for the adjectives.
   * @param nounValue  The value for the noun.
   * @param endpoint The endpoint on which to invoke the operation.
   * @return The result of the invocation.  If the invocation has no return type (void), returns null.
   * @throws Exception if any problems occur.
   */
  public Object invoke(Object properNoun, Map<String, Object> contextParameters, Map<String, Object> adjectives, Object nounValue, Object endpoint) throws Exception {
    Class[] parameterTypes = this.method.getParameterTypes();
    Object[] parameters = new Object[parameterTypes.length];
    if (properNounIndex > -1) {
      parameters[properNounIndex] = properNoun;
    }

    if (nounValueIndex > -1) {
      if ((nounValue != null) && (Collection.class.isAssignableFrom(parameterTypes[nounValueIndex]))) {
        //convert the noun value back into a collection...
        nounValue = convertToCollection(nounValue, parameterTypes[nounValueIndex]);
      }
      
      parameters[nounValueIndex] = nounValue;
    }

    if (contentTypeParameterIndex > -1) {
      parameters[contentTypeParameterIndex] = getContentType();
    }

    for (String adjective : adjectiveIndices.keySet()) {
      Object adjectiveValue = adjectives.get(adjective);
      Integer index = adjectiveIndices.get(adjective);
      if ((adjectiveValue != null) && (Collection.class.isAssignableFrom(parameterTypes[index]))) {
        //convert the array back into a collection...
        adjectiveValue = convertToCollection(adjectiveValue, parameterTypes[index]);
      }

      parameters[index] = adjectiveValue;
    }

    for (String contextParameterName : contextParameterIndices.keySet()) {
      parameters[contextParameterIndices.get(contextParameterName)] = contextParameters.get(contextParameterName);
    }

    try {
      return this.method.invoke(endpoint, parameters);
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
   * Converts an array back into a collection.
   *
   * @param array The array.
   * @param collectionType The collection type.
   * @return The collection.
   */
  protected Object convertToCollection(Object array, Class collectionType) {
    if (array instanceof Object[]) {
      Collection collection = newCollectionInstance(collectionType);
      collection.addAll(Arrays.asList((Object[]) array));
      array = collection;
    }
    else if (!(array instanceof Collection)) {
      throw new IllegalArgumentException("Expected an array or collection.");
    }

    return array;
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
   * The resource for this operation.
   *
   * @return The resource for this operation.
   */
  public RESTResource getResource() {
    return resource;
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
   * The index to the noun value.
   *
   * @return The index to the noun value.
   */
  protected Integer getNounValueIndex() {
    return nounValueIndex;
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
   * The set of context classes for this operation.  I.e. the set of visible classes from the method declaration.
   *
   * @return The set of context classes for this operation.  I.e. the set of visible classes from the method declaration.
   */
  public Set<Class> getContextClasses() {
    return contextClasses;
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
   * The character set for the result of this operation.
   *
   * @return The character set for the result of this operation.
   */
  public String getCharset() {
    return charset;
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
   * The JSONP parameter name for this operation.
   *
   * @return The JSONP parameter name for this operation, or null if none.
   */
  public String getJSONPParameter() {
    return JSONPParameter;
  }

}
