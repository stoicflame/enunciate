/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.resources.*;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.spring_web.model.*;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class MethodImpl implements Method {

  private final String httpMethod;
  private final RequestMapping requestMapping;
  private final ResourceGroup group;
  private final MethodExampleImpl example;
  private final ApiRegistrationContext registrationContext;

  public MethodImpl(String httpMethod, RequestMapping requestMapping, ResourceGroup group, ApiRegistrationContext registrationContext) {
    this.httpMethod = httpMethod;
    this.requestMapping = requestMapping;
    this.group = group;
    this.registrationContext = registrationContext;
    this.example = this.requestMapping.getContext().isDisableExamples() ? null : new MethodExampleImpl(this.httpMethod, this.requestMapping, this.registrationContext);
  }

  @Override
  public Resource getResource() {
    return new ResourceImpl(this.requestMapping, this.group, registrationContext);
  }

  @Override
  public String getLabel() {
    return this.requestMapping.getLabel() == null ? this.httpMethod : this.requestMapping.getLabel();
  }

  @Override
  public String getDeveloperLabel() {
    return this.requestMapping.getSimpleName().toString();
  }

  @Override
  public String getHttpMethod() {
    return this.httpMethod;
  }

  @Override
  public String getSlug() {
    return this.group.getSlug() + "_" + requestMapping.getSimpleName() + "_" + this.httpMethod;
  }

  @Override
  public String getSummary() {
    return null;
  }

  @Override
  public String getDescription() {
    return this.requestMapping.getJavaDoc(this.registrationContext.getTagHandler()).toString();
  }

  @Override
  public String getDeprecated() {
    String deprecated = ElementUtils.findDeprecationMessage(this.requestMapping, this.registrationContext.getTagHandler());
    if (deprecated == null) {
      deprecated = ElementUtils.findDeprecationMessage(this.requestMapping.getParent(), this.registrationContext.getTagHandler());
    }
    return deprecated;
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList tags = this.requestMapping.getJavaDoc(this.registrationContext.getTagHandler()).get("since");
    return tags == null ? null : tags.toString();
  }

  @Override
  public List<String> getSeeAlso() {
    return this.requestMapping.getJavaDoc(this.registrationContext.getTagHandler()).get("see");
  }

  @Override
  public String getVersion() {
    JavaDoc.JavaDocTagList tags = this.requestMapping.getJavaDoc(this.registrationContext.getTagHandler()).get("version");
    return tags == null ? null : tags.toString();
  }

  @Override
  public boolean isIncludeDefaultParameterValues() {
    for (RequestParameter parameter : this.requestMapping.getRequestParameters()) {
      if (parameter.getDefaultValue() != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isHasParameterConstraints() {
    for (Parameter parameter : getParameters()) {
      if (parameter.getConstraints() != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isHasParameterMultiplicity() {
    for (RequestParameter parameter : this.requestMapping.getRequestParameters()) {
      if (parameter.isMultivalued()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<? extends Parameter> getParameters() {
    Set<RequestParameter> resourceParams = this.requestMapping.getRequestParameters();
    ArrayList<Parameter> parameters = new ArrayList<Parameter>(resourceParams.size());
    for (RequestParameter param : resourceParams) {
      parameters.add(new ParameterImpl(param));
    }
    return parameters;
  }

  @Override
  public Entity getRequestEntity() {
    ResourceEntityParameter entityParameter = this.requestMapping.getEntityParameter();
    return entityParameter == null ? null : new RequestEntityImpl(this.requestMapping, entityParameter, registrationContext);
  }

  @Override
  public List<? extends StatusCode> getResponseCodes() {
    ArrayList<StatusCode> statusCodes = new ArrayList<StatusCode>();
    for (ResponseCode responseCode : this.requestMapping.getStatusCodes()) {
      statusCodes.add(new StatusCodeImpl(responseCode, this.registrationContext));
    }
    return statusCodes;
  }

  @Override
  public Entity getResponseEntity() {
    ResourceRepresentationMetadata responseMetadata = this.requestMapping.getRepresentationMetadata();
    return responseMetadata == null ? null : new ResponseEntityImpl(this.requestMapping, responseMetadata, registrationContext);
  }

  @Override
  public List<? extends StatusCode> getWarnings() {
    ArrayList<StatusCode> statusCodes = new ArrayList<StatusCode>();
    for (ResponseCode responseCode : this.requestMapping.getWarnings()) {
      statusCodes.add(new StatusCodeImpl(responseCode, this.registrationContext));
    }
    return statusCodes;
  }

  @Override
  public List<? extends Parameter> getResponseHeaders() {
    Map<String, String> responseHeaders = this.requestMapping.getResponseHeaders();
    ArrayList<Parameter> headerValues = new ArrayList<Parameter>();
    for (Map.Entry<String, String> responseHeader : responseHeaders.entrySet()) {
      headerValues.add(new ResponseHeaderParameterImpl(responseHeader.getKey(), responseHeader.getValue(), Collections.<String>emptySet()));
    }
    return headerValues;
  }

  @Override
  public Set<String> getSecurityRoles() {
    return this.requestMapping.getSecurityRoles();
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.requestMapping.getAnnotation(annotationType);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.requestMapping.getAnnotations();
  }

  @Override
  public Set<Facet> getFacets() {
    return this.requestMapping.getFacets();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.requestMapping.getJavaDoc(this.registrationContext.getTagHandler());
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.requestMapping, this.requestMapping.getContext().getContext().getConfiguration().getAnnotationStyles());
  }

  @Override
  public Example getExample() {
    return example;
  }

  public RequestMapping getRequestMapping() {
    return requestMapping;
  }
}
