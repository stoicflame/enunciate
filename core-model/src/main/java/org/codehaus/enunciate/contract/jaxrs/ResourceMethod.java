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
import net.sf.jelly.apt.decorations.JavaDoc;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedMethodDeclaration;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedClassType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.Facet;
import org.codehaus.enunciate.contract.HasFacets;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.jaxrs.*;
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
public class ResourceMethod extends DecoratedMethodDeclaration implements HasFacets {

  private static final Pattern CONTEXT_PARAM_PATTERN = Pattern.compile("\\{([^\\}]+)\\}");

  private final String subpath;
  private final String label;
  private final String showSampleRequest;
  private final String showSampleResponse;
  private final String sampleResponseCode;
  private final String customParameterName;
  private final Set<String> httpMethods;
  private final Set<String> consumesMime;
  private final Set<String> producesMime;
  private final Set<String> additionalHeaderLabels;
  private final Resource parent;
  private final List<ResourceParameter> resourceParameters;
  private final ResourceEntityParameter entityParameter;
  private final List<ResourceEntityParameter> declaredEntityParameters;
  private final Map<String, Object> metaData = new HashMap<String, Object>();
  private final List<? extends ResponseCode> statusCodes;
  private final List<? extends ResponseCode> warnings;
  private final Map<String, String> responseHeaders = new HashMap<String, String>();
  private final ResourceRepresentationMetadata representationMetadata;
  private final Set<Facet> facets = new TreeSet<Facet>();

  public ResourceMethod(MethodDeclaration delegate, Resource parent) {
    super(delegate);
    this.paramsComments.putAll(this.parseAllParamComments(getJavaDoc()));

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

    String label = null;
    ResourceLabel resourceLabel = delegate.getAnnotation(ResourceLabel.class);
    if (resourceLabel != null) {
      label = resourceLabel.value();
    }

    String subpath = null;
    Path pathInfo = delegate.getAnnotation(Path.class);
    if (pathInfo != null) {
      subpath = pathInfo.value();
    }

    String showSampleRequest = null;
    SampleRequest sampleRequest = delegate.getAnnotation(SampleRequest.class);
    if (sampleRequest != null) {
        showSampleRequest = sampleRequest.sampleType();
    }

    String showSampleResponse = null;
    SampleResponse sampleResponse = delegate.getAnnotation(SampleResponse.class);
    if (sampleResponse != null) {
        showSampleResponse = sampleResponse.sampleType();
    }

    String sampleResponseCode = "";
    if (sampleResponse != null) {
        ResponseCode code = new ResponseCode();
        code.setCode(sampleResponse.responseCode());
        sampleResponseCode = code.getCodeString();
    }

    String customParameterName = null;
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
        else if (ResourceParameter.isFormBeanParameter(parameterDeclaration)) {
          resourceParameters.addAll(ResourceParameter.getFormBeanParameters(parameterDeclaration));
        }
        else if (!ResourceParameter.isSystemParameter(parameterDeclaration)) {
          entityParameter = new ResourceEntityParameter(this, parameterDeclaration);
          declaredEntityParameters.add(entityParameter);
          customParameterName = parameterDeclaration.getSimpleName();
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

        if (getJavaDoc().get("returnWrapped") != null) { //support jax-doclets. see http://jira.codehaus.org/browse/ENUNCIATE-690
          String fqn = getJavaDoc().get("returnWrapped").get(0);
          AnnotationProcessorEnvironment env = net.sf.jelly.apt.Context.getCurrentEnvironment();
          TypeDeclaration type = env.getTypeDeclaration(fqn);
          if (type != null) {
            returnTypeMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(env.getTypeUtils().getDeclaredType(type));
          }
        }

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

    JavaDoc.JavaDocTagList doclets = getJavaDoc().get("RequestHeader"); //support jax-doclets. see http://jira.codehaus.org/browse/ENUNCIATE-690
    if (doclets != null) {
      for (String doclet : doclets) {
        int firstspace = doclet.indexOf(' ');
        String header = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        resourceParameters.add(new ExplicitResourceParameter(this, doc, header, ResourceParameterType.HEADER));
      }
    }

    ArrayList<ResponseCode> statusCodes = new ArrayList<ResponseCode>();
    ArrayList<ResponseCode> warnings = new ArrayList<ResponseCode>();
    Set<String> additionalHeaderLabels = new TreeSet<String>();
    StatusCodes codes = getAnnotation(StatusCodes.class);
    if (codes != null) {
      for (org.codehaus.enunciate.jaxrs.ResponseCode code : codes.value()) {
        ResponseCode rc = new ResponseCode();
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        for (ResponseHeader header : code.additionalHeaders()){
            rc.setAdditionalHeader(header.name(), header.description());
            additionalHeaderLabels.add(header.name());
        }
        statusCodes.add(rc);
      }
    }

    doclets = getJavaDoc().get("HTTP"); //support jax-doclets. see http://jira.codehaus.org/browse/ENUNCIATE-690
    if (doclets != null) {
      for (String doclet : doclets) {
        int firstspace = doclet.indexOf(' ');
        String code = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        try {
          ResponseCode rc = new ResponseCode();
          rc.setCode(Integer.parseInt(code));
          rc.setCondition(doc);
          statusCodes.add(rc);
        }
        catch (NumberFormatException e) {
          //fall through...
        }
      }
    }

    Warnings warningInfo = getAnnotation(Warnings.class);
    if (warningInfo != null) {
      for (org.codehaus.enunciate.jaxrs.ResponseCode code : warningInfo.value()) {
        ResponseCode rc = new ResponseCode();
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        warnings.add(rc);
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

    warningInfo = parent.getAnnotation(Warnings.class);
    if (warningInfo != null) {
      for (org.codehaus.enunciate.jaxrs.ResponseCode code : warningInfo.value()) {
        ResponseCode rc = new ResponseCode();
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        warnings.add(rc);
      }
    }

    ResponseHeaders responseHeaders = parent.getAnnotation(ResponseHeaders.class);
    if (responseHeaders != null) {
      for (ResponseHeader header : responseHeaders.value()) {
        this.responseHeaders.put(header.name(), header.description());
      }
    }

    responseHeaders = getAnnotation(ResponseHeaders.class);
    if (responseHeaders != null) {
      for (ResponseHeader header : responseHeaders.value()) {
        this.responseHeaders.put(header.name(), header.description());
      }
    }

    doclets = getJavaDoc().get("ResponseHeader"); //support jax-doclets. see http://jira.codehaus.org/browse/ENUNCIATE-690
    if (doclets != null) {
      for (String doclet : doclets) {
        int firstspace = doclet.indexOf(' ');
        String header = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        this.responseHeaders.put(header, doc);
      }
    }

    this.additionalHeaderLabels = additionalHeaderLabels;
    this.entityParameter = entityParameter;
    this.resourceParameters = resourceParameters;
    this.subpath = subpath;
    this.label = label;
    this.showSampleRequest = showSampleRequest;
    this.showSampleResponse = showSampleResponse;
    this.sampleResponseCode = sampleResponseCode;
    this.customParameterName = customParameterName;
    this.parent = parent;
    this.statusCodes = statusCodes;
    this.warnings = warnings;
    this.representationMetadata = outputPayload;
    this.declaredEntityParameters = declaredEntityParameters;
    this.facets.addAll(Facet.gatherFacets(delegate));
    this.facets.add(new Facet("org.codehaus.enunciate.contract.jaxrs.Resource", parent.getSimpleName(), parent.getJavaDoc().toString())); //resource methods have an implicit facet for their declaring resource.
    this.facets.addAll(parent.getFacets());
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

  protected static HashMap<String, String> parseParamComments(String tagName, JavaDoc jd) {
    HashMap<String, String> paramComments = new HashMap<String, String>();
    if (jd.get(tagName) != null) {
      for (String paramDoc : jd.get(tagName)) {
        paramDoc = paramDoc.trim().replaceFirst("\\s+", " ");
        int spaceIndex = paramDoc.indexOf(' ');
        if (spaceIndex == -1) {
          spaceIndex = paramDoc.length();
        }

        String param = paramDoc.substring(0, spaceIndex);
        String paramComment = "";
        if ((spaceIndex + 1) < paramDoc.length()) {
          paramComment = paramDoc.substring(spaceIndex + 1);
        }

        paramComments.put(param, paramComment);
      }
    }
    return paramComments;
  }

  protected HashMap<String, String> parseAllParamComments(JavaDoc jd) {
    HashMap<String, String> paramRESTComments = parseParamComments("RSParam", jd);
    HashMap<String, String> paramComments = parseParamComments("param", jd);
    paramComments.putAll(paramRESTComments);
    return paramComments;
  }


  /**
   * Loads the overridden resource parameter values.
   *
   * @param signatureOverride The signature override.
   * @return The explicit resource parameters.
   */
  protected List<ResourceParameter> loadResourceParameters(ResourceMethodSignature signatureOverride) {
    HashMap<String, String> paramComments = parseAllParamComments(getJavaDoc());
//    HashMap<String, String> paramComments = parseParamComments(getJavaDoc());

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
          return new ResourceEntityParameter(((DeclaredType) typeMirror).getDeclaration(), typeMirror);
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
  protected static String scrubParamNames(String subpath) {
    StringBuilder builder = new StringBuilder(subpath.length());
    int charIndex = 0;
    int inBrace = 0;
    boolean definingRegexp = false;
    while (charIndex < subpath.length()) {
      char ch = subpath.charAt(charIndex++);
      if (ch == '{') {
        inBrace++;
      }
      else if (ch == '}') {
        inBrace--;
        if (inBrace == 0) {
          definingRegexp = false;
        }
      }
      else if (inBrace == 1 && ch == ':') {
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
   * The label for this resource method, if it exists.
   *
   * @return The subpath for this resource method, if it exists.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Controls if a sample Request is being generated and of which type the sample is (JSON or XML or plain text).
   *
   * @return the type of the sample request
   */
   public String getShowSampleRequest() {
      return showSampleRequest;
   }

   /**
    * Controls if a sample Response is being generated and of which type the sample is (JSON or XML or plain text).
    *
    * @return the type of the sample response
    */
   public String getShowSampleResponse() {
      return showSampleResponse;
   }

   /**
    * The Status Code that is shown in the sample request.
    *
    * @return The Status Code that is shown in the sample request.
    */
   public String getSampleResponseCode() {
      return sampleResponseCode;
   }

  /**
   * The name of a custom request parameter (e.g String password -> "password").
   *
   * @return the name of the custom parameter
   */
   public String getCustomParameterName() {
      return customParameterName;
   }

  /**
   * Set of labels for additional ResponseHeaders
   *
   * @return
   */
  public Set<String> getAdditionalHeaderLabels() {
    return additionalHeaderLabels;
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
   * The potential warnings.
   *
   * @return The potential warnings.
   */
  public List<? extends ResponseCode> getWarnings() {
    return this.warnings;
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

  /**
   * The response headers that are expected on this resource method.
   *
   * @return The response headers that are expected on this resource method.
   */
  public Map<String, String> getResponseHeaders() {
    return responseHeaders;
  }

  /**
   * The facets here applicable.
   *
   * @return The facets here applicable.
   */
  public Set<Facet> getFacets() {
    return facets;
  }


}
