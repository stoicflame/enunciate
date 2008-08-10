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

package org.codehaus.enunciate.contract.rest;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.ReferenceType;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.ServiceEndpoint;
import org.codehaus.enunciate.rest.annotations.*;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;

import javax.activation.DataHandler;
import java.util.*;

/**
 * A REST method.
 *
 * @author Ryan Heaton
 */
public class RESTMethod extends DecoratedMethodDeclaration implements ServiceEndpoint {

  private final RESTNoun noun;
  private final RESTParameter properNoun;
  private final RESTParameter nounValue;
  private final Collection<RESTParameter> adjectives;
  private final Collection<RESTParameter> contextParameters;
  private final Collection<RESTParameter> contentTypeParameters;
  private final Collection<RESTError> RESTErrors;
  private final String jsonpParameter;
  private final Set<String> contentTypes;
  private final RESTEndpoint endpoint;

  public RESTMethod(MethodDeclaration delegate, RESTEndpoint endpoint) {
    super(delegate);

    this.endpoint = endpoint;
    RESTParameter properNoun = null;
    RESTParameter nounValue = null;
    this.adjectives = new ArrayList<RESTParameter>();
    this.contextParameters = new ArrayList<RESTParameter>();
    this.contentTypeParameters = new ArrayList<RESTParameter>();
    for (ParameterDeclaration parameterDeclaration : getParameters()) {
      RESTParameter restParameter = new RESTParameter(parameterDeclaration);
      if (restParameter.isProperNoun()) {
        if (properNoun != null) {
          throw new ValidationException(properNoun.getPosition(), "REST method has more than one proper noun.  The other found at " + restParameter.getPosition());
        }
        else if (restParameter.isContextParam()) {
          throw new ValidationException(restParameter.getPosition(), "A REST context parameter cannot also be a proper noun.");
        }
        
        properNoun = restParameter;
      }
      else if (restParameter.isNounValue()) {
        if (nounValue != null) {
          throw new ValidationException(nounValue.getPosition(), "REST method has more than one noun value.  The other found at " + restParameter.getPosition());
        }
        else if (restParameter.isContextParam()) {
          throw new ValidationException(restParameter.getPosition(), "A REST context parameter cannot also be the noun value.");
        }

        nounValue = restParameter;
      }
      else if (restParameter.isContextParam()) {
        contextParameters.add(restParameter);
      }
      else if (restParameter.isContentTypeParameter()) {
        contentTypeParameters.add(restParameter);
      }
      else {
        adjectives.add(restParameter);
      }
    }

    this.nounValue = nounValue;
    this.properNoun = properNoun;

    this.RESTErrors = new ArrayList<RESTError>();
    for (ReferenceType referenceType : getThrownTypes()) {
      ClassDeclaration throwableDeclaration = ((ClassType) referenceType).getDeclaration();
      this.RESTErrors.add(new RESTError(throwableDeclaration));
    }

    String noun = getSimpleName();
    Noun nounInfo = getAnnotation(Noun.class);
    if ((nounInfo != null) && (!"".equals(nounInfo.value()))) {
      noun = nounInfo.value();
    }

    String nounContext = "";
    NounContext nounContextInfo = delegate.getDeclaringType().getAnnotation(NounContext.class);
    if (nounContextInfo != null) {
      nounContext = nounContextInfo.value();
    }
    if ((nounInfo != null) && (!"##default".equals(nounInfo.context()))) {
      nounContext = nounInfo.context();
    }
    
    this.noun = new RESTNoun(noun, nounContext);

    String jsonpParameter = null;
    JSONP jsonpInfo = getAnnotation(JSONP.class);
    if (jsonpInfo == null) {
      jsonpInfo = delegate.getDeclaringType().getAnnotation(JSONP.class);
      if (jsonpInfo == null) {
        jsonpInfo = delegate.getDeclaringType().getPackage().getAnnotation(JSONP.class);
      }
    }

    if (jsonpInfo != null) {
      jsonpParameter = jsonpInfo.paramName();
    }
    this.jsonpParameter = jsonpParameter;

    this.contentTypes = new TreeSet<String>();
    this.contentTypes.add("application/xml");
    this.contentTypes.add("application/json");

    ContentType contentTypeInfo = delegate.getDeclaringType().getPackage() != null ? delegate.getDeclaringType().getPackage().getAnnotation(ContentType.class) : null;
    if (contentTypeInfo != null) {
      this.contentTypes.addAll(Arrays.asList(contentTypeInfo.value()));
      this.contentTypes.removeAll(Arrays.asList(contentTypeInfo.unsupported()));
    }

    contentTypeInfo = delegate.getDeclaringType().getAnnotation(ContentType.class);
    if (contentTypeInfo != null) {
      this.contentTypes.addAll(Arrays.asList(contentTypeInfo.value()));
      this.contentTypes.removeAll(Arrays.asList(contentTypeInfo.unsupported()));
    }

    contentTypeInfo = getAnnotation(ContentType.class);
    if (contentTypeInfo != null) {
      this.contentTypes.addAll(Arrays.asList(contentTypeInfo.value()));
      this.contentTypes.removeAll(Arrays.asList(contentTypeInfo.unsupported()));
    }
  }

  /**
   * The REST endpoint that defines specifies this REST method.
   *
   * @return The REST endpoint that defines specifies this REST method.
   */
  public RESTEndpoint getRESTEndpoint() {
    return endpoint;
  }

  // Inherited.
  public String getServiceEndpointName() {
    return getRESTEndpoint().getName();
  }

  // Inherited.
  public TypeDeclaration getServiceEndpointInterface() {
    return getDeclaringType();
  }

  // Inherited.
  public TypeDeclaration getServiceEndpointDefaultImplementation() {
    return getRESTEndpoint();
  }

  /**
   * The noun for this method.
   *
   * @return The noun for this method.
   */
  public RESTNoun getNoun() {
    return noun;
  }

  /**
   * The verb for this method.
   *
   * @return The verb for this method.
   */
  public VerbType[] getVerbs() {
    VerbType[] verbs = { VerbType.read };

    Verb verbInfo = getAnnotation(Verb.class);
    if (verbInfo != null) {
      verbs = verbInfo.value();
    }

    for (int i = 0; i < verbs.length; i++) {
      VerbType verb = verbs[i];
      if (verb.getAlias() != null) {
        verbs[i] = verb.getAlias();
      }
    }

    return verbs;
  }

  /**
   * The proper noun for this method.
   *
   * @return The proper noun for this method.
   */
  public RESTParameter getProperNoun() {
    return this.properNoun;
  }

  /**
   * The noun value for this method.
   *
   * @return The noun value for this method.
   */
  public RESTParameter getNounValue() {
    return this.nounValue;
  }

  /**
   * The adjectives for this REST method.
   *
   * @return The adjectives for this REST method.
   */
  public Collection<RESTParameter> getAdjectives() {
    return adjectives;
  }

  /**
   * The context parameters for this REST method.
   *
   * @return The context parameters for this REST method.
   */
  public Collection<RESTParameter> getContextParameters() {
    return contextParameters;
  }

  /**
   * The content type paramters for this REST method.
   *
   * @return The content type paramters for this REST method.
   */
  public Collection<RESTParameter> getContentTypeParameters() {
    return contentTypeParameters;
  }

  /**
   * The errors possibly thrown by this REST method.
   *
   * @return The errors possibly thrown by this REST method.
   */
  public Collection<RESTError> getRESTErrors() {
    return RESTErrors;
  }

  /**
   * Whether the return type from this REST method is XML.
   *
   * @return Whether the return type from this REST method is XML.
   */
  public boolean isCustomType() {
    return ((DecoratedTypeMirror) getReturnType()).isInstanceOf(DataHandler.class.getName());
  }

  /**
   * The XML type of the return value.
   *
   * @return The XML type of the return value.
   */
  public XmlType getXMLReturnType() {
    try {
      return XmlTypeFactory.getXmlType(getReturnType());
    }
    catch (XmlTypeException e) {
      throw new ValidationException(getPosition(), e.getMessage());
    }
  }

  /**
   * The JSONP parameter name.
   *
   * @return The JSONP parameter name, or null if none.
   */
  public String getJSONPParameter() {
    return jsonpParameter;
  }

  /**
   * The data formats applied to this method.
   *
   * @return The data formats applied to this method.
   */
  public Set<String> getContentTypes() {
    return contentTypes;
  }
}
