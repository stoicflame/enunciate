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
package com.webcohesion.enunciate.modules.jaxrs.model;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.ParamDocComment;
import com.webcohesion.enunciate.javac.javadoc.ReturnDocComment;
import com.webcohesion.enunciate.javac.javadoc.StaticDocComment;
import com.webcohesion.enunciate.metadata.rs.*;
import com.webcohesion.enunciate.metadata.rs.ResponseHeader;
import com.webcohesion.enunciate.modules.jaxrs.EnunciateJaxrsContext;
import com.webcohesion.enunciate.modules.jaxrs.model.util.JaxrsUtil;
import com.webcohesion.enunciate.modules.jaxrs.model.util.RSParamDocComment;
import com.webcohesion.enunciate.modules.jaxrs.model.util.ReturnWrappedDocComment;
import com.webcohesion.enunciate.util.AnnotationUtils;
import com.webcohesion.enunciate.util.IgnoreUtils;
import com.webcohesion.enunciate.util.TypeHintUtils;
import io.swagger.annotations.*;

import javax.annotation.security.RolesAllowed;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.webcohesion.enunciate.modules.jaxrs.model.Resource.extractPathComponents;

/**
 * A JAX-RS resource method.
 *
 * @author Ryan Heaton
 */
public class ResourceMethod extends DecoratedExecutableElement implements HasFacets, PathContext {

  private static final Pattern CONTEXT_PARAM_PATTERN = Pattern.compile("\\{([^\\}]+)\\}");

  private final EnunciateJaxrsContext context;
  private final String subpath;
  private final String label;
  private final String customParameterName;
  private final Set<String> httpMethods;
  private final Set<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType> consumesMediaTypes;
  private final Set<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType> producesMediaTypes;
  private final Resource parent;
  private final Set<ResourceParameter> resourceParameters;
  private final ResourceEntityParameter entityParameter;
  private final Map<String, Object> metaData = new HashMap<String, Object>();
  private final List<? extends ResponseCode> statusCodes;
  private final List<? extends ResponseCode> warnings;
  private final Map<String, String> responseHeaders;
  private final ResourceRepresentationMetadata representationMetadata;
  private final Set<Facet> facets = new TreeSet<Facet>();
  private final List<PathSegment> pathComponents;

  public ResourceMethod(ExecutableElement delegate, Resource parent, TypeVariableContext variableContext, EnunciateJaxrsContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.context = context;

    Set<String> httpMethods = loadHttpMethods(delegate);

    if (httpMethods.isEmpty()) {
      throw new IllegalStateException("A resource method must specify an HTTP method by using a request method designator annotation.");
    }

    this.httpMethods = httpMethods;
    this.consumesMediaTypes = loadConsumes(delegate, parent);
    this.producesMediaTypes = loadProduces(delegate, parent);

    String label = null;
    ResourceLabel resourceLabel = delegate.getAnnotation(ResourceLabel.class);
    if (resourceLabel != null) {
      label = resourceLabel.value();
      if ("##default".equals(label)) {
        label = null;
      }
    }

    String subpath = null;
    Path pathInfo = delegate.getAnnotation(Path.class);
    if (pathInfo != null) {
      subpath = pathInfo.value();
    }

    List<PathSegment> pathComponents = extractPathComponents(subpath);

    String customParameterName = null;
    ResourceEntityParameter entityParameter;
    Set<ResourceParameter> resourceParameters;
    ResourceRepresentationMetadata outputPayload;
    ResourceMethodSignature signatureOverride = delegate.getAnnotation(ResourceMethodSignature.class);
    if (signatureOverride == null) {
      entityParameter = null;
      resourceParameters = new TreeSet<ResourceParameter>();
      //if we're not overriding the signature, assume we use the real method signature.
      for (VariableElement parameterDeclaration : getParameters()) {
        if (IgnoreUtils.isIgnored(parameterDeclaration)) {
          continue;
        }

        if (ResourceParameter.isResourceParameter(parameterDeclaration, context)) {
          resourceParameters.add(new ResourceParameter(parameterDeclaration, this));
        }
        else if (ResourceParameter.isBeanParameter(parameterDeclaration)) {
          resourceParameters.addAll(ResourceParameter.getFormBeanParameters(parameterDeclaration, this));
        }
        else if (!ResourceParameter.isSystemParameter(parameterDeclaration, context)) {
          entityParameter = new ResourceEntityParameter(this, parameterDeclaration, variableContext, context);
          customParameterName = parameterDeclaration.getSimpleName().toString();
        }
      }

      //now resolve any type variables.
      DecoratedTypeMirror returnType = loadReturnType();
      returnType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(variableContext.resolveTypeVariables(returnType, this.env), this.env);
      returnType.setDeferredDocComment(new ReturnDocComment(this));
      outputPayload = returnType.isVoid() ? null : new ResourceRepresentationMetadata(returnType);
    }
    else {
      entityParameter = loadEntityParameter(signatureOverride);
      resourceParameters = loadResourceParameters(signatureOverride);
      outputPayload = loadOutputPayload(signatureOverride);
    }

    if (outputPayload == null && getJavaDoc().get("responseExample") != null) {
      //if no response was found but a response example is supplied, create a dummy response output.
      DecoratedProcessingEnvironment env = context.getContext().getProcessingEnvironment();
      outputPayload = new ResourceRepresentationMetadata(TypeMirrorUtils.objectType(env), new StaticDocComment(""));
    }

    if (entityParameter == null && getJavaDoc().get("requestExample") != null) {
      //if no entity parameter was found, but a request example is supplied, create a dummy entity parameter.
      DecoratedProcessingEnvironment env = context.getContext().getProcessingEnvironment();
      entityParameter = new ResourceEntityParameter(this, TypeMirrorUtils.objectType(env), env);
    }

    resourceParameters.addAll(loadExtraParameters(parent, context));

    this.entityParameter = entityParameter;
    this.resourceParameters = resourceParameters;
    this.subpath = subpath;
    this.label = label;
    this.customParameterName = customParameterName;
    this.parent = parent;
    this.statusCodes = loadStatusCodes(parent);
    this.warnings = loadWarnings(parent);
    this.representationMetadata = outputPayload;
    this.facets.addAll(Facet.gatherFacets(delegate, context.getContext()));
    this.facets.addAll(parent.getFacets());
    this.pathComponents = pathComponents;
    this.responseHeaders = loadResponseHeaders(parent);
  }

  protected Set<String> loadHttpMethods(ExecutableElement delegate) {
    Set<String> httpMethods = new TreeSet<String>();
    List<? extends AnnotationMirror> mirrors = delegate.getAnnotationMirrors();
    for (AnnotationMirror mirror : mirrors) {
      Element annotationDeclaration = mirror.getAnnotationType().asElement();
      HttpMethod httpMethodInfo = annotationDeclaration.getAnnotation(HttpMethod.class);
      if (httpMethodInfo != null) {
        //request method designator found.
        httpMethods.add(httpMethodInfo.value());
      }
    }

    final ApiOperation apiOperation = delegate.getAnnotation(ApiOperation.class);
    if (apiOperation != null && !apiOperation.httpMethod().isEmpty()) {
      httpMethods.clear(); //swagger annotation overrides JAX-RS annotations by definition.
      httpMethods.add(apiOperation.httpMethod());
    }
    return httpMethods;
  }

  public Map<String, String> loadResponseHeaders(Resource parent) {
    Map<String, String> responseHeaders = new HashMap<String, String>();
    ResponseHeaders responseHeaderInfo = getAnnotation(ResponseHeaders.class);
    if (responseHeaderInfo != null) {
      for (ResponseHeader header : responseHeaderInfo.value()) {
        responseHeaders.put(header.name(), header.description());
      }
    }

    List<ResponseHeaders> inheritedResponseHeaders = AnnotationUtils.getAnnotations(ResponseHeaders.class, parent);
    for (ResponseHeaders inheritedResponseHeader : inheritedResponseHeaders) {
      for (ResponseHeader header : inheritedResponseHeader.value()) {
        responseHeaders.put(header.name(), header.description());
      }
    }

    JavaDoc.JavaDocTagList doclets = getJavaDoc().get("ResponseHeader"); //support jax-doclets. see http://jira.codehaus.org/browse/ENUNCIATE-690
    if (doclets != null) {
      for (String doclet : doclets) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String header = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        responseHeaders.put(header, doc);
      }
    }

    List<JavaDoc.JavaDocTagList> inheritedDoclets = AnnotationUtils.getJavaDocTags("ResponseHeader", parent);
    for (JavaDoc.JavaDocTagList inheritedDoclet : inheritedDoclets) {
      for (String doclet : inheritedDoclet) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String header = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        responseHeaders.put(header, doc);
      }
    }

    return responseHeaders;
  }

  public ArrayList<ResponseCode> loadWarnings(Resource parent) {
    ArrayList<ResponseCode> warnings = new ArrayList<ResponseCode>();
    Warnings warningInfo = getAnnotation(Warnings.class);
    if (warningInfo != null) {
      for (com.webcohesion.enunciate.metadata.rs.ResponseCode code : warningInfo.value()) {
        ResponseCode rc = new ResponseCode(this);
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        warnings.add(rc);
      }
    }

    List<Warnings> inheritedWarnings = AnnotationUtils.getAnnotations(Warnings.class, parent);
    for (Warnings inheritedWarning : inheritedWarnings) {
      for (com.webcohesion.enunciate.metadata.rs.ResponseCode code : inheritedWarning.value()) {
        ResponseCode rc = new ResponseCode(this);
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        warnings.add(rc);
      }
    }

    JavaDoc.JavaDocTagList doclets = getJavaDoc().get("HTTPWarning");
    if (doclets != null) {
      for (String doclet : doclets) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String code = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        try {
          ResponseCode rc = new ResponseCode(this);
          rc.setCode(Integer.parseInt(code));
          rc.setCondition(doc);
          warnings.add(rc);
        }
        catch (NumberFormatException e) {
          //fall through...
        }
      }
    }

    List<JavaDoc.JavaDocTagList> inheritedDoclets = AnnotationUtils.getJavaDocTags("HTTPWarning", parent);
    for (JavaDoc.JavaDocTagList inheritedDoclet : inheritedDoclets) {
      for (String doclet : inheritedDoclet) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String code = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        try {
          ResponseCode rc = new ResponseCode(this);
          rc.setCode(Integer.parseInt(code));
          rc.setCondition(doc);
          warnings.add(rc);
        }
        catch (NumberFormatException e) {
          //fall through...
        }
      }
    }
    return warnings;
  }

  protected Set<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType> loadConsumes(ExecutableElement delegate, Resource parent) {
    Set<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType> consumes;
    Consumes consumesInfo = delegate.getAnnotation(Consumes.class);
    if (consumesInfo != null) {
      consumes = new TreeSet<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType>(JaxrsUtil.value(consumesInfo));
    }
    else {
      consumes = new TreeSet<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType>(parent.getConsumesMediaTypes());
    }

    final ApiOperation apiOperation = delegate.getAnnotation(ApiOperation.class);
    if (apiOperation != null && !apiOperation.consumes().isEmpty()) {
      consumes = new TreeSet<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType>();
      for (String mediaType : apiOperation.consumes().split(",")) {
        mediaType = mediaType.trim();
        if (!mediaType.isEmpty()) {
          consumes.add(new com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType(mediaType, 1.0F));
        }
      }
    }
    return consumes;
  }

  protected Set<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType> loadProduces(ExecutableElement delegate, Resource parent) {
    Set<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType> produces;
    Produces producesInfo = delegate.getAnnotation(Produces.class);
    if (producesInfo != null) {
      produces = new TreeSet<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType>(JaxrsUtil.value(producesInfo));
    }
    else {
      produces = new TreeSet<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType>(parent.getProducesMediaTypes());
    }

    final ApiOperation apiOperation = delegate.getAnnotation(ApiOperation.class);
    if (apiOperation != null && !apiOperation.produces().isEmpty()) {
      produces = new TreeSet<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType>();
      for (String mediaType : apiOperation.produces().split(",")) {
        mediaType = mediaType.trim();
        if (!mediaType.isEmpty()) {
          produces.add(new com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType(mediaType, 1.0F));
        }
      }
    }
    return produces;
  }

  protected DecoratedTypeMirror loadReturnType() {
    DecoratedTypeMirror returnType;
    TypeHint hintInfo = getAnnotation(TypeHint.class);
    if (hintInfo != null) {
      returnType = (DecoratedTypeMirror) TypeHintUtils.getTypeHint(hintInfo, this.env, getReturnType());
      returnType.setDeferredDocComment(new ReturnDocComment(this));
    }
    else {
      returnType = (DecoratedTypeMirror) getReturnType();

      // in the case where the return type is com.sun.jersey.api.JResponse,
      // we can use the type argument to get the entity type
      if (returnType.isClass() && returnType.isInstanceOf("com.sun.jersey.api.JResponse")) {
        DecoratedDeclaredType jresponse = (DecoratedDeclaredType) returnType;
        if (!jresponse.getTypeArguments().isEmpty()) {
          DecoratedTypeMirror responseType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(jresponse.getTypeArguments().get(0), this.env);
          if (responseType.isDeclared()) {
            responseType.setDeferredDocComment(new ReturnDocComment(this));
            returnType = responseType;
          }
        }
      }
      else if (returnType.isInstanceOf(Response.class) || returnType.isInstanceOf(java.io.InputStream.class)) {
        //generic response that doesn't have a type hint; we'll just have to assume return type of "object"
        DecoratedDeclaredType objectType = (DecoratedDeclaredType) TypeMirrorDecorator.decorate(this.env.getElementUtils().getTypeElement(Object.class.getName()).asType(), this.env);
        objectType.setDeferredDocComment(new ReturnDocComment(this));
        returnType = objectType;
      }
    }

    final ApiOperation apiOperation = getAnnotation(ApiOperation.class);
    if (apiOperation != null) {
      DecoratedTypeMirror swaggerReturnType = Annotations.mirrorOf(new Callable<Class<?>>() {
        @Override
        public Class<?> call() throws Exception {
          return apiOperation.response();
        }
      }, this.env, Void.class);

      if (swaggerReturnType != null) {
        if (!apiOperation.responseContainer().isEmpty()) {
          swaggerReturnType = (DecoratedTypeMirror) this.env.getTypeUtils().getArrayType(swaggerReturnType);
          swaggerReturnType.setDeferredDocComment(new ReturnDocComment(this));
        }

        returnType = swaggerReturnType;
      }
    }

    JavaDoc localDoc = new JavaDoc(getDocComment(), null, null, this.env);
    if (localDoc.get("returnWrapped") != null) { //support jax-doclets. see http://jira.codehaus.org/browse/ENUNCIATE-690
      String returnWrapped = localDoc.get("returnWrapped").get(0);
      String fqn = returnWrapped.substring(0, JavaDoc.indexOfFirstWhitespace(returnWrapped)).trim();

      boolean array = false;
      if (fqn.endsWith("[]")) {
        array = true;
        fqn = fqn.substring(0, fqn.length() - 2);
      }

      TypeElement type = env.getElementUtils().getTypeElement(fqn);
      if (type != null) {
        if (!array && isNoContentType(fqn)) {
          returnType = (DecoratedTypeMirror) this.env.getTypeUtils().getNoType(TypeKind.VOID);
        } else {
          returnType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(env.getTypeUtils().getDeclaredType(type), this.env);

          if (array) {
            returnType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(env.getTypeUtils().getArrayType(returnType), this.env);
          }
        }

        returnType.setDeferredDocComment(new ReturnWrappedDocComment(this));
      }
      else {
        getContext().getContext().getLogger().info("Invalid @returnWrapped type: \"%s\" (doesn't resolve to a type).", fqn);
      }
    }

    return returnType;
  }
  
  //the following name denotes 'no content' semantics:
  //- com.webcohesion.enunciate.metadata.rs.TypeHint.NO_CONTENT
  private boolean isNoContentType(String fqn) {
    String noContentClassName = TypeHint.NO_CONTENT.class.getName();
    return fqn.equals(noContentClassName.replace('$', '.'));
  }
  
  public Set<ResourceParameter> loadExtraParameters(Resource parent, EnunciateJaxrsContext context) {
    Set<ResourceParameter> extraParameters = new TreeSet<ResourceParameter>();
    JavaDoc localDoc = new JavaDoc(getDocComment(), null, null, this.env);
    JavaDoc.JavaDocTagList doclets = localDoc.get("RequestHeader"); //support jax-doclets. see http://jira.codehaus.org/browse/ENUNCIATE-690
    if (doclets != null) {
      for (String doclet : doclets) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String header = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        extraParameters.add(new ExplicitResourceParameter(this, new StaticDocComment(doc), header, ResourceParameterType.HEADER, context));
      }
    }

    List<JavaDoc.JavaDocTagList> inheritedDoclets = AnnotationUtils.getJavaDocTags("RequestHeader", parent);
    for (JavaDoc.JavaDocTagList inheritedDoclet : inheritedDoclets) {
      for (String doclet : inheritedDoclet) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String header = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        extraParameters.add(new ExplicitResourceParameter(this, new StaticDocComment(doc), header, ResourceParameterType.HEADER, context));
      }
    }

    RequestHeaders requestHeaders = getAnnotation(RequestHeaders.class);
    if (requestHeaders != null) {
      for (RequestHeader header : requestHeaders.value()) {
        extraParameters.add(new ExplicitResourceParameter(this, new StaticDocComment(header.description()), header.name(), ResourceParameterType.HEADER, context));
      }
    }

    List<RequestHeaders> inheritedRequestHeaders = AnnotationUtils.getAnnotations(RequestHeaders.class, parent);
    for (RequestHeaders inheritedRequestHeader : inheritedRequestHeaders) {
      for (RequestHeader header : inheritedRequestHeader.value()) {
        extraParameters.add(new ExplicitResourceParameter(this, new StaticDocComment(header.description()), header.name(), ResourceParameterType.HEADER, context));
      }
    }

    ApiImplicitParams swaggerImplicitParams = getAnnotation(ApiImplicitParams.class);
    if (swaggerImplicitParams != null) {
      for (ApiImplicitParam swaggerImplicitParam : swaggerImplicitParams.value()) {
        ResourceParameterType parameterType;
        try {
          parameterType = ResourceParameterType.valueOf(swaggerImplicitParam.paramType().toUpperCase());
        }
        catch (IllegalArgumentException e) {
          continue;
        }

        extraParameters.add(new ExplicitResourceParameter(this, new StaticDocComment(swaggerImplicitParam.value()), swaggerImplicitParam.name(), parameterType, context));
      }
    }

    return extraParameters;
  }

  public ArrayList<ResponseCode> loadStatusCodes(Resource parent) {
    ArrayList<ResponseCode> statusCodes = new ArrayList<ResponseCode>();
    StatusCodes codes = getAnnotation(StatusCodes.class);
    if (codes != null) {
      for (com.webcohesion.enunciate.metadata.rs.ResponseCode code : codes.value()) {
        ResponseCode rc = new ResponseCode(this);
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        for (ResponseHeader header : code.additionalHeaders()) {
          rc.setAdditionalHeader(header.name(), header.description());
        }

        rc.setType((DecoratedTypeMirror) TypeHintUtils.getTypeHint(code.type(), this.env, null));

        statusCodes.add(rc);
      }
    }

    List<StatusCodes> inheritedStatusCodes = AnnotationUtils.getAnnotations(StatusCodes.class, parent);
    for (StatusCodes inheritedStatusCode : inheritedStatusCodes) {
      for (com.webcohesion.enunciate.metadata.rs.ResponseCode code : inheritedStatusCode.value()) {
        ResponseCode rc = new ResponseCode(this);
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        for (ResponseHeader header : code.additionalHeaders()) {
          rc.setAdditionalHeader(header.name(), header.description());
        }

        rc.setType((DecoratedTypeMirror) TypeHintUtils.getTypeHint(code.type(), this.env, null));

        statusCodes.add(rc);
      }
    }

    JavaDoc.JavaDocTagList doclets = getJavaDoc().get("HTTP"); //support jax-doclets. see http://jira.codehaus.org/browse/ENUNCIATE-690
    if (doclets != null) {
      for (String doclet : doclets) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String code = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        try {
          ResponseCode rc = new ResponseCode(this);
          rc.setCode(Integer.parseInt(code));
          rc.setCondition(doc);
          statusCodes.add(rc);
        }
        catch (NumberFormatException e) {
          //fall through...
        }
      }
    }

    List<JavaDoc.JavaDocTagList> inheritedDoclets = AnnotationUtils.getJavaDocTags("HTTP", parent);
    for (JavaDoc.JavaDocTagList inheritedDoclet : inheritedDoclets) {
      for (String doclet : inheritedDoclet) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String code = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        try {
          ResponseCode rc = new ResponseCode(this);
          rc.setCode(Integer.parseInt(code));
          rc.setCondition(doc);
          statusCodes.add(rc);
        }
        catch (NumberFormatException e) {
          //fall through...
        }
      }
    }

    ApiResponses swaggerResponses = getAnnotation(ApiResponses.class);
    if (swaggerResponses != null) {
      for (ApiResponse swaggerResponse : swaggerResponses.value()) {
        ResponseCode rc = new ResponseCode(this);
        rc.setCode(swaggerResponse.code());
        rc.setCondition(swaggerResponse.message());

        io.swagger.annotations.ResponseHeader[] headers = swaggerResponse.responseHeaders();
        for (io.swagger.annotations.ResponseHeader header : headers) {
          if (!header.name().isEmpty()) {
            rc.setAdditionalHeader(header.name(), header.description());
          }
        }

        statusCodes.add(rc);
      }
    }

    return statusCodes;
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
        TypeElement type = env.getElementUtils().getTypeElement(outputType.getName());
        return new ResourceRepresentationMetadata(env.getTypeUtils().getDeclaredType(type), returnType.getDeferredDocComment());
      }
    }
    catch (MirroredTypeException e) {
      DecoratedTypeMirror typeMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(e.getTypeMirror(), this.env);
      if (typeMirror.isInstanceOf(ResourceMethodSignature.class.getName() + ".NONE")) {
        return null;
      }
      return new ResourceRepresentationMetadata(typeMirror, returnType.getDeferredDocComment());
    }

    return null;
  }

  @Override
  protected ParamDocComment createParamDocComment(VariableElement param) {
    return new RSParamDocComment(this, param.getSimpleName().toString());
  }

  /**
   * Loads the overridden resource parameter values.
   *
   * @param signatureOverride The signature override.
   * @return The explicit resource parameters.
   */
  protected Set<ResourceParameter> loadResourceParameters(ResourceMethodSignature signatureOverride) {
    TreeSet<ResourceParameter> params = new TreeSet<ResourceParameter>();
    for (CookieParam cookieParam : signatureOverride.cookieParams()) {
      params.add(new ExplicitResourceParameter(this, new RSParamDocComment(this, cookieParam.value()), cookieParam.value(), ResourceParameterType.COOKIE, context));
    }
    for (MatrixParam matrixParam : signatureOverride.matrixParams()) {
      params.add(new ExplicitResourceParameter(this, new RSParamDocComment(this, matrixParam.value()), matrixParam.value(), ResourceParameterType.MATRIX, context));
    }
    for (QueryParam queryParam : signatureOverride.queryParams()) {
      params.add(new ExplicitResourceParameter(this, new RSParamDocComment(this, queryParam.value()), queryParam.value(), ResourceParameterType.QUERY, context));
    }
    for (PathParam pathParam : signatureOverride.pathParams()) {
      params.add(new ExplicitResourceParameter(this, new RSParamDocComment(this, pathParam.value()), pathParam.value(), ResourceParameterType.PATH, context));
    }
    for (HeaderParam headerParam : signatureOverride.headerParams()) {
      params.add(new ExplicitResourceParameter(this, new RSParamDocComment(this, headerParam.value()), headerParam.value(), ResourceParameterType.HEADER, context));
    }
    for (FormParam formParam : signatureOverride.formParams()) {
      params.add(new ExplicitResourceParameter(this, new RSParamDocComment(this, formParam.value()), formParam.value(), ResourceParameterType.FORM, context));
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
        TypeElement type = env.getElementUtils().getTypeElement(entityType.getName());
        return new ResourceEntityParameter(type, env.getTypeUtils().getDeclaredType(type), this.context.getContext().getProcessingEnvironment());
      }
    }
    catch (MirroredTypeException e) {
      DecoratedTypeMirror typeMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(e.getTypeMirror(), this.context.getContext().getProcessingEnvironment());
      if (typeMirror.isDeclared()) {
        if (typeMirror.isInstanceOf(ResourceMethodSignature.class.getName() + ".NONE")) {
          return null;
        }
        else {
          return new ResourceEntityParameter(((DeclaredType) typeMirror).asElement(), typeMirror, this.context.getContext().getProcessingEnvironment());
        }
      }
      else {
        throw new EnunciateException(toString() + ": Illegal input type (must be a declared type): " + typeMirror);
      }
    }

    return null;
  }

  public EnunciateJaxrsContext getContext() {
    return context;
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
   * Get the path components for this resource method.
   *
   * @return The path components.
   */
  public List<PathSegment> getPathComponents() {
    List<PathSegment> components = new ArrayList<PathSegment>();
    Resource parent = getParent();
    if (parent != null) {
      components.addAll(parent.getPathComponents());
    }
    components.addAll(this.pathComponents);
    return components;
  }

  /**
   * Builds the full URI path to this resource method.
   *
   * @return the full URI path to this resource method.
   */
  public String getFullpath() {
    StringBuilder builder = new StringBuilder();
    for (PathSegment component : getPathComponents()) {
      builder.append('/').append(component.getValue());
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
   * A slug for this method.
   *
   * @return A slug for this method.
   */
  public String getSlug() {
    String slug = "";
    Resource parent = this.parent;
    while (parent instanceof SubResource) {
      slug = parent.getSimpleName() + "_" + slug;
      parent = parent.getParent();
    }
    slug = slug + getSimpleName();
    return slug;
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
  public Set<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType> getConsumesMediaTypes() {
    return consumesMediaTypes;
  }

  /**
   * The MIME types that are produced by this method.
   *
   * @return The MIME types that are produced by this method.
   */
  public Set<com.webcohesion.enunciate.modules.jaxrs.model.util.MediaType> getProducesMediaTypes() {
    return producesMediaTypes;
  }

  /**
   * The list of resource parameters that this method requires to be invoked.
   *
   * @return The list of resource parameters that this method requires to be invoked.
   */
  public Set<ResourceParameter> getResourceParameters() {
    TreeSet<ResourceParameter> resourceParams = new TreeSet<ResourceParameter>(this.resourceParameters);
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

  /**
   * The security roles for this method.
   *
   * @return The security roles for this method.
   */
  public Set<String> getSecurityRoles() {
    TreeSet<String> roles = new TreeSet<String>();
    RolesAllowed rolesAllowed = getAnnotation(RolesAllowed.class);
    if (rolesAllowed != null) {
      Collections.addAll(roles, rolesAllowed.value());
    }

    Resource parent = getParent();
    if (parent != null) {
      roles.addAll(parent.getSecurityRoles());
    }
    return roles;
  }

}
