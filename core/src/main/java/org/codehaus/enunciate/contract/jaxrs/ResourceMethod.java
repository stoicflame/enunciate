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

package org.codehaus.enunciate.contract.jaxrs;

import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.codehaus.enunciate.contract.common.rest.*;
import org.codehaus.enunciate.rest.MimeType;

/**
 * A JAX-RS resource method.
 *
 * @author Ryan Heaton
 */
public class ResourceMethod extends DecoratedMethodDeclaration implements RESTResource {

  private static final Pattern CONTEXT_PARAM_PATTERN = Pattern.compile("\\{([^\\}]+)\\}");

  private final String subpath;
  private final Set<String> httpMethods;
  private final Set<String> consumesMime;
  private final Set<String> producesMime;
  private final Resource parent;
  private final List<ResourceParameter> resourceParameters;
  private final ResourceEntityParameter entityParameter;
  private final Map<String, Object> metaData = new HashMap<String, Object>();
  private final List<? extends RESTResourceError> errors;

  public ResourceMethod(MethodDeclaration delegate, Resource parent) {
    super(delegate);

    Set<String> httpMethods = new TreeSet<String>();
    Collection<AnnotationMirror> mirrors = delegate.getAnnotationMirrors();
    for (AnnotationMirror mirror : mirrors) {
      AnnotationTypeDeclaration annotationDeclaration = mirror.getAnnotationType().getDeclaration();
      HttpMethod httpMethodInfo = annotationDeclaration.getAnnotation(HttpMethod.class);
      if (httpMethodInfo != null) {
        //request method designator found.
        httpMethods.add(httpMethodInfo.value());
        break;
      }
    }

    if (httpMethods.isEmpty()) {
      throw new IllegalStateException("A resource method must specify an HTTP method by using a request method designator annotation.");
    }

    this.httpMethods = httpMethods;

    Set<String> consumes;
    Consumes consumesInfo = delegate.getAnnotation(Consumes.class);
    if (consumesInfo != null) {
      consumes = new TreeSet<String>(Arrays.asList(consumesInfo.value()));
    }
    else {
      consumes = new TreeSet<String>(parent.getConsumesMime());
    }
    this.consumesMime = consumes;

    Set<String> produces;
    Produces producesInfo = delegate.getAnnotation(Produces.class);
    if (producesInfo != null) {
      produces = new TreeSet<String>(Arrays.asList(producesInfo.value()));
    }
    else {
      produces = new TreeSet<String>(parent.getProducesMime());
    }
    this.producesMime = produces;

    String subpath = null;
    Path pathInfo = delegate.getAnnotation(Path.class);
    if (pathInfo != null) {
      subpath = pathInfo.value();
    }

    ParameterDeclaration entityParameter = null;
    List<ResourceParameter> resourceParameters = new ArrayList<ResourceParameter>();
    for (ParameterDeclaration parameterDeclaration : getParameters()) {
      if (ResourceParameter.isResourceParameter(parameterDeclaration)) {
        resourceParameters.add(new ResourceParameter(parameterDeclaration));
      }
      else {
        entityParameter = parameterDeclaration;
      }
    }

    this.entityParameter = entityParameter == null ? null : new ResourceEntityParameter(entityParameter);
    this.resourceParameters = resourceParameters;
    this.subpath = subpath;
    this.parent = parent;
    this.errors = new ArrayList<RESTResourceError>();
  }

  /**
   * The HTTP methods for invoking the method.
   *
   * @return The HTTP methods for invoking the method.
   */
  public Set<String> getHttpMethods() {
    return httpMethods;
  }

  /**
   * Builds the full URI path to this resource method.
   *
   * @return the full URI path to this resource method.
   */
  public String getFullpath() {
    List<String> subpaths = new ArrayList<String>();
    if (getSubpath() != null) {
      subpaths.add(0, getSubpath());
    }

    Resource parent = getParent();
    while (parent != null) {
      subpaths.add(0, parent.getPath());
      parent = parent.getParent();
    }

    StringBuilder builder = new StringBuilder();
    for (String subpath : subpaths) {
      subpath = subpath.trim();
      if (!subpath.startsWith("/")) {
        subpath = '/' + subpath;
      }
      while (subpath.endsWith("/")) {
        subpath = subpath.substring(0, subpath.length() - 1);
      }
      subpath = scrubParamNames(subpath);

      builder.append(subpath);
    }

    return builder.toString();
  }

  /**
   * The servlet pattern that can be applied to access this resource method.
   *
   * @return The servlet pattern that can be applied to access this resource method.
   */
  public String getServletPattern() {
    StringBuilder builder = new StringBuilder();
    String fullPath = getFullpath();
    Matcher pathParamMatcher = CONTEXT_PARAM_PATTERN.matcher(fullPath);
    if (pathParamMatcher.find()) {
      builder.append(fullPath, 0, pathParamMatcher.start()).append("*");
    }
    else {
      builder.append(fullPath);
    }
    return builder.toString();
  }

  /**
   * Scrubs the path parameters names from the specified subpath.
   *
   * @param subpath The subpath.
   * @return The scrubbed path.
   */
  protected String scrubParamNames(String subpath) {
    StringBuilder builder = new StringBuilder(subpath.length());
    int charIndex = 0;
    boolean inBrace = false;
    boolean definingRegexp = false;
    while (charIndex < subpath.length()) {
      char ch = subpath.charAt(charIndex++);
      if (ch == '{') {
        inBrace = true;
      }
      else if (ch == '}') {
        inBrace = false;
        definingRegexp = false;
      }
      else if (inBrace && ch == ':') {
        definingRegexp = true;
      }

      if (!definingRegexp) {
        builder.append(ch);
      }
    }
    return builder.toString();
  }

  /**
   * The subpath for this resource method, if it exists.
   *
   * @return The subpath for this resource method, if it exists.
   */
  public String getSubpath() {
    return subpath;
  }

  /**
   * The resource that holds this resource method.
   *
   * @return The resource that holds this resource method.
   */
  public Resource getParent() {
    return parent;
  }

  /**
   * The MIME types that are consumed by this method.
   *
   * @return The MIME types that are consumed by this method.
   */
  public Set<String> getConsumesMime() {
    return consumesMime;
  }

  /**
   * The MIME types that are produced by this method.
   *
   * @return The MIME types that are produced by this method.
   */
  public Set<String> getProducesMime() {
    return producesMime;
  }

  /**
   * The list of resource parameters that this method requires to be invoked.
   *
   * @return The list of resource parameters that this method requires to be invoked.
   */
  public List<ResourceParameter> getResourceParameters() {
    ArrayList<ResourceParameter> resourceParams = new ArrayList<ResourceParameter>(this.resourceParameters);
    resourceParams.addAll(getParent().getResourceParameters());
    return resourceParams;
  }

  /**
   * The entity parameter.
   *
   * @return The entity parameter, or null if none.
   */
  public ResourceEntityParameter getEntityParameter() {
    return entityParameter;
  }

  // Inherited.
  public String getPath() {
    return getFullpath();
  }

  // Inherited.
  public Set<String> getSupportedOperations() {
    return getHttpMethods();
  }

  // Inherited.
  public List<SupportedContentType> getSupportedContentTypes() {
    HashMap<String, SupportedContentType> supportedTypes = new HashMap<String, SupportedContentType>();
    for (String consumesMime : getConsumesMime()) {
      String type;
      try {
        type = MimeType.parse(consumesMime).toString();
      }
      catch (Exception e) {
        type = consumesMime;
      }

      SupportedContentType supportedType = supportedTypes.get(type);
      if (supportedType == null) {
        supportedType = new SupportedContentType();
        supportedType.setType(type);
        supportedTypes.put(type, supportedType);
      }
      supportedType.setConsumable(true);
    }
    for (String producesMime : getProducesMime()) {
      String type;
      try {
        type = MimeType.parse(producesMime).toString();
      }
      catch (Exception e) {
        type = producesMime;
      }

      SupportedContentType supportedType = supportedTypes.get(type);
      if (supportedType == null) {
        supportedType = new SupportedContentType();
        supportedType.setType(type);
        supportedTypes.put(type, supportedType);
      }
      supportedType.setProduceable(true);
    }

    return new ArrayList<SupportedContentType>(supportedTypes.values());
  }

  // Inherited.
  public RESTResourcePayload getInputPayload() {
    return getEntityParameter();
  }

  // Inherited.
  public RESTResourcePayload getOutputPayload() {
    DecoratedTypeMirror returnType = (DecoratedTypeMirror) getReturnType();
    return returnType.isVoid() ? null : new ResourcePayloadTypeAdapter(returnType);
  }

  // Inherited.
  public List<? extends RESTResourceError> getResourceErrors() {
    return this.errors;
  }

  // Inherited.1
  public Map<String, Object> getMetaData() {
    return Collections.unmodifiableMap(this.metaData);
  }

  /**
   * Put metadata associated with this resource.
   *
   * @param name The name of the metadata.
   * @param data The data.
   */
  public void putMetaData(String name, Object data) {
    this.metaData.put(name, data);
  }

}
