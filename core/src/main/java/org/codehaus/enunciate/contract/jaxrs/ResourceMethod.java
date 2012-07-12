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

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.MirroredTypeException;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.declaration.DecoratedMethodDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedClassType;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.TypeHint;
import org.codehaus.enunciate.rest.MimeType;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A JAX-RS resource method.
 *
 * @author Ryan Heaton
 */
public class ResourceMethod extends DecoratedMethodDeclaration {

  private static final Pattern CONTEXT_PARAM_PATTERN = Pattern.compile("\\{([^\\}]+)\\}");

  private final String subpath;
  private final Set<String> httpMethods;
  private final Set<String> consumesMime;
  private final Set<String> producesMime;
  private final Resource parent;
  private final List<ResourceParameter> resourceParameters;
  private final ResourceEntityParameter entityParameter;
  private final List<ResourceEntityParameter> declaredEntityParameters;
  private final Map<String, Object> metaData = new HashMap<String, Object>();
  private final List<? extends ResponseCode> statusCodes;
  private final ResourceRepresentationMetadata representationMetadata;

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
      }
    }

    if (httpMethods.isEmpty()) {
      throw new IllegalStateException("A resource method must specify an HTTP method by using a request method designator annotation.");
    }

    this.httpMethods = httpMethods;

    Set<String> consumes;
    Consumes consumesInfo = delegate.getAnnotation(Consumes.class);
    if (consumesInfo != null) {
      consumes = new TreeSet<String>(Arrays.asList(JAXRSUtils.value(consumesInfo)));
    }
    else {
      consumes = new TreeSet<String>(parent.getConsumesMime());
    }
    this.consumesMime = consumes;

    Set<String> produces;
    Produces producesInfo = delegate.getAnnotation(Produces.class);
    if (producesInfo != null) {
      produces = new TreeSet<String>(Arrays.asList(JAXRSUtils.value(producesInfo)));
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

    ResourceEntityParameter entityParameter;
    List<ResourceEntityParameter> declaredEntityParameters = new ArrayList<ResourceEntityParameter>();
    List<ResourceParameter> resourceParameters;
    ResourceRepresentationMetadata outputPayload;
    ResourceMethodSignature signatureOverride = delegate.getAnnotation(ResourceMethodSignature.class);
    if (signatureOverride == null) {
      entityParameter = null;
      resourceParameters = new ArrayList<ResourceParameter>();
      //if we're not overriding the signature, assume we use the real method signature.
      for (ParameterDeclaration parameterDeclaration : getParameters()) {
        if (ResourceParameter.isResourceParameter(parameterDeclaration)) {
          resourceParameters.add(new ResourceParameter(parameterDeclaration));
        }
        else if (parameterDeclaration.getAnnotation(Context.class) == null) {
          entityParameter = new ResourceEntityParameter(parameterDeclaration);
          declaredEntityParameters.add(entityParameter);
        }
      }

      DecoratedTypeMirror returnTypeMirror;
      TypeHint hintInfo = getAnnotation(TypeHint.class);
      if (hintInfo != null) {
        try {
          Class hint = hintInfo.value();
          AnnotationProcessorEnvironment env = net.sf.jelly.apt.Context.getCurrentEnvironment();
          if (TypeHint.NO_CONTENT.class.equals(hint)) {
            returnTypeMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(env.getTypeUtils().getVoidType());
          }
          else {
            String hintName = hint.getName();

            if (TypeHint.NONE.class.equals(hint)) {
              hintName = hintInfo.qualifiedName();
            }

            if (!"##NONE".equals(hintName)) {
              TypeDeclaration type = env.getTypeDeclaration(hintName);
              returnTypeMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(env.getTypeUtils().getDeclaredType(type));
            }
            else {
              returnTypeMirror = (DecoratedTypeMirror) getReturnType();
            }
          }
        }
        catch (MirroredTypeException e) {
          returnTypeMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(e.getTypeMirror());
        }
        returnTypeMirror.setDocComment(((DecoratedTypeMirror) getReturnType()).getDocComment());
      }
      else {
        returnTypeMirror = (DecoratedTypeMirror) getReturnType();
        
        // in the case where the return type is com.sun.jersey.api.JResponse, 
        // we can use the type argument to get the entity type
        if (returnTypeMirror.isClass() && returnTypeMirror.isInstanceOf("com.sun.jersey.api.JResponse")) {
        	DecoratedClassType jresponse = (DecoratedClassType) returnTypeMirror;
        	if (!jresponse.getActualTypeArguments().isEmpty()) {
        		DecoratedTypeMirror responseType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(jresponse.getActualTypeArguments().iterator().next());
        		if (responseType.isDeclared()) {
        			responseType.setDocComment(returnTypeMirror.getDocComment());
        			returnTypeMirror = responseType;
        		}
        	}
        }
      }

      outputPayload = returnTypeMirror.isVoid() ? null : new ResourceRepresentationMetadata(returnTypeMirror);
    }
    else {
      entityParameter = loadEntityParameter(signatureOverride);
      declaredEntityParameters.add(entityParameter);
      resourceParameters = loadResourceParameters(signatureOverride);
      outputPayload = loadOutputPayload(signatureOverride);
    }

    ArrayList<ResponseCode> statusCodes = new ArrayList<ResponseCode>();
    StatusCodes codes = getAnnotation(StatusCodes.class);
    if (codes != null) {
      for (org.codehaus.enunciate.jaxrs.ResponseCode code : codes.value()) {
        ResponseCode rc = new ResponseCode();
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        statusCodes.add(rc);
      }
    }

    codes = parent.getAnnotation(StatusCodes.class);
    if (codes != null) {
      for (org.codehaus.enunciate.jaxrs.ResponseCode code : codes.value()) {
        ResponseCode rc = new ResponseCode();
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        statusCodes.add(rc);
      }
    }

    this.entityParameter = entityParameter;
    this.resourceParameters = resourceParameters;
    this.subpath = subpath;
    this.parent = parent;
    this.statusCodes = statusCodes;
    this.representationMetadata = outputPayload;
    this.declaredEntityParameters = declaredEntityParameters;
  }

  /**
   * Loads the explicit output payload.
   *
   * @param signatureOverride The method signature override.
   * @return The output payload (explicit in the signature override.
   */
  protected ResourceRepresentationMetadata loadOutputPayload(ResourceMethodSignature signatureOverride) {
    DecoratedTypeMirror returnType = (DecoratedTypeMirror) getReturnType();

    try {
      Class<?> outputType = signatureOverride.output();
      if (outputType != ResourceMethodSignature.NONE.class) {
        AnnotationProcessorEnvironment env = net.sf.jelly.apt.Context.getCurrentEnvironment();
        TypeDeclaration type = env.getTypeDeclaration(outputType.getName());
        return new ResourceRepresentationMetadata(env.getTypeUtils().getDeclaredType(type), returnType.getDocValue());
      }
    }
    catch (MirroredTypeException e) {
      DecoratedTypeMirror typeMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(e.getTypeMirror());
      if (typeMirror.isDeclared()) {
        if (typeMirror.isInstanceOf(ResourceMethodSignature.class.getName() + ".NONE")) {
          return null;
        }
        return new ResourceRepresentationMetadata(typeMirror, returnType.getDocValue());
      }
      else {
        throw new ValidationException(getPosition(), "Illegal output type (must be a declared type): " + typeMirror);
      }
    }

    return null;
  }

  /**
   * Loads the overridden resource parameter values.
   *
   * @param signatureOverride The signature override.
   * @return The explicit resource parameters.
   */
  protected List<ResourceParameter> loadResourceParameters(ResourceMethodSignature signatureOverride) {
    HashMap<String, String> paramComments = parseParamComments(getJavaDoc());
    
    ArrayList<ResourceParameter> params = new ArrayList<ResourceParameter>();
    for (CookieParam cookieParam : signatureOverride.cookieParams()) {
      params.add(new ExplicitResourceParameter(this, paramComments.get(cookieParam.value()), cookieParam.value(), ResourceParameterType.COOKIE));
    }
    for (MatrixParam matrixParam : signatureOverride.matrixParams()) {
      params.add(new ExplicitResourceParameter(this, paramComments.get(matrixParam.value()), matrixParam.value(), ResourceParameterType.MATRIX));
    }
    for (QueryParam queryParam : signatureOverride.queryParams()) {
      params.add(new ExplicitResourceParameter(this, paramComments.get(queryParam.value()), queryParam.value(), ResourceParameterType.QUERY));
    }
    for (PathParam pathParam : signatureOverride.pathParams()) {
      params.add(new ExplicitResourceParameter(this, paramComments.get(pathParam.value()), pathParam.value(), ResourceParameterType.PATH));
    }
    for (HeaderParam headerParam : signatureOverride.headerParams()) {
      params.add(new ExplicitResourceParameter(this, paramComments.get(headerParam.value()), headerParam.value(), ResourceParameterType.HEADER));
    }
    for (FormParam formParam : signatureOverride.formParams()) {
      params.add(new ExplicitResourceParameter(this, paramComments.get(formParam.value()), formParam.value(), ResourceParameterType.FORM));
    }
    
    return params;
  }

  /**
   * Loads the specified entity parameter according to the method signature override.
   *
   * @param signatureOverride The signature override.
   * @return The resource entity parameter.
   */
  protected ResourceEntityParameter loadEntityParameter(ResourceMethodSignature signatureOverride) {
    try {
      Class<?> entityType = signatureOverride.input();
      if (entityType != ResourceMethodSignature.NONE.class) {
        AnnotationProcessorEnvironment env = net.sf.jelly.apt.Context.getCurrentEnvironment();
        TypeDeclaration type = env.getTypeDeclaration(entityType.getName());
        return new ResourceEntityParameter(type, env.getTypeUtils().getDeclaredType(type));
      }
    }
    catch (MirroredTypeException e) {
      DecoratedTypeMirror typeMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(e.getTypeMirror());
      if (typeMirror.isDeclared()) {
        if (typeMirror.isInstanceOf(ResourceMethodSignature.class.getName() + ".NONE")) {
          return null;
        }
        else {
          return new ResourceEntityParameter(((DeclaredType)typeMirror).getDeclaration(), typeMirror);
        }
      }
      else {
        throw new ValidationException(getPosition(), "Illegal input type (must be a declared type): " + typeMirror);
      }
    }

    return null;
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

  /**
   * The entity parameters that were declared. According to JAX-RS, there should be only one for now.
   *
   * @return The entity parameters that were declared.
   */
  public List<ResourceEntityParameter> getDeclaredEntityParameters() {
    return declaredEntityParameters;
  }

  /**
   * The applicable media types for this resource.
   *
   * @return The applicable media types for this resource.
   */
  public List<ResourceMethodMediaType> getApplicableMediaTypes() {
    HashMap<String, ResourceMethodMediaType> applicableTypes = new HashMap<String, ResourceMethodMediaType>();
    for (String consumesMime : getConsumesMime()) {
      String type;
      try {
        type = MimeType.parse(consumesMime).toString();
      }
      catch (Exception e) {
        type = consumesMime;
      }

      ResourceMethodMediaType supportedType = applicableTypes.get(type);
      if (supportedType == null) {
        supportedType = new ResourceMethodMediaType();
        supportedType.setType(type);
        applicableTypes.put(type, supportedType);
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

      ResourceMethodMediaType supportedType = applicableTypes.get(type);
      if (supportedType == null) {
        supportedType = new ResourceMethodMediaType();
        supportedType.setType(type);
        applicableTypes.put(type, supportedType);
      }
      supportedType.setProduceable(true);
    }

    return new ArrayList<ResourceMethodMediaType>(applicableTypes.values());
  }

  /**
   * The output payload for this resource.
   *
   * @return The output payload for this resource.
   */
  public ResourceRepresentationMetadata getRepresentationMetadata() {
    return this.representationMetadata;
  }

  /**
   * The potential status codes.
   *
   * @return The potential status codes.
   */
  public List<? extends ResponseCode> getStatusCodes() {
    return this.statusCodes;
  }

  /**
     * The metadata associated with this resource.
     *
     * @return The metadata associated with this resource.
     */
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
