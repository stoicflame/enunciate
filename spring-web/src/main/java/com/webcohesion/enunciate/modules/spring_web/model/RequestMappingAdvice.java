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
package com.webcohesion.enunciate.modules.spring_web.model;

import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.javac.javadoc.ParamDocComment;
import com.webcohesion.enunciate.javac.javadoc.ReturnDocComment;
import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.*;
import com.webcohesion.enunciate.modules.spring_web.EnunciateSpringWebContext;
import com.webcohesion.enunciate.modules.spring_web.model.util.RSParamDocComment;
import com.webcohesion.enunciate.modules.spring_web.model.util.ReturnWrappedDocComment;
import com.webcohesion.enunciate.util.AnnotationUtils;
import com.webcohesion.enunciate.util.TypeHintUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.IncompleteAnnotationException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * A JAX-RS resource method.
 *
 * @author Ryan Heaton
 */
public class RequestMappingAdvice extends DecoratedExecutableElement {

  private static final Pattern CONTEXT_PARAM_PATTERN = Pattern.compile("\\{([^\\}]+)\\}");

  private final EnunciateSpringWebContext context;
  private final SpringControllerAdvice parent;
  private final Set<RequestParameter> requestParameters;
  private final ResourceEntityParameter entityParameter;
  private final List<? extends ResponseCode> statusCodes;
  private final List<? extends ResponseCode> warnings;
  private final Map<String, String> responseHeaders = new HashMap<String, String>();
  private final ResourceRepresentationMetadata representationMetadata;

  public RequestMappingAdvice(RequestMapping requestMapping, ModelAttribute modelAttribute, ExecutableElement delegate, SpringControllerAdvice parent, TypeVariableContext variableContext, EnunciateSpringWebContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.context = context;

    ResourceEntityParameter entityParameter = null;
    ResourceRepresentationMetadata outputPayload;
    Set<RequestParameter> requestParameters = new TreeSet<RequestParameter>();

    for (VariableElement parameterDeclaration : getParameters()) {
      if (parameterDeclaration.getAnnotation(RequestBody.class) != null) {
        entityParameter = new ResourceEntityParameter(parameterDeclaration, variableContext, context);
      }
      else {
        requestParameters.addAll(RequestParameterFactory.getRequestParameters(this, parameterDeclaration, requestMapping));
      }
    }

    DecoratedTypeMirror<?> returnType;
    TypeHint hintInfo = getAnnotation(TypeHint.class);
    JavaDoc localDoc = new JavaDoc(getDocComment(), null, null, this.env);
    if (hintInfo != null) {
      returnType = (DecoratedTypeMirror) TypeHintUtils.getTypeHint(hintInfo, this.env, null);
      if (returnType != null) {
        returnType.setDeferredDocComment(new ReturnDocComment(this));
      }
    }
    else {
      returnType = (DecoratedTypeMirror) getReturnType();

      if (returnType instanceof DecoratedDeclaredType && (returnType.isInstanceOf(Callable.class) || returnType.isInstanceOf("org.springframework.web.context.request.async.DeferredResult") || returnType.isInstanceOf("org.springframework.util.concurrent.ListenableFuture"))) {
        //attempt unwrap callable and deferred results.
        List<? extends TypeMirror> typeArgs = ((DecoratedDeclaredType) returnType).getTypeArguments();
        returnType = (typeArgs != null && typeArgs.size() == 1) ? (DecoratedTypeMirror<?>) TypeMirrorDecorator.decorate(typeArgs.get(0), this.env) : TypeMirrorUtils.objectType(this.env);
      }

      boolean returnsResponseBody = getAnnotation(ResponseBody.class) != null
        || AnnotationUtils.getMetaAnnotation(ResponseBody.class, parent.getDelegate()) != null;

      if (returnType instanceof DecoratedDeclaredType && returnType.isInstanceOf("org.springframework.http.HttpEntity")) {
        DecoratedDeclaredType entity = (DecoratedDeclaredType) returnType;
        List<? extends TypeMirror> typeArgs = ((DecoratedDeclaredType) entity).getTypeArguments();
        returnType = (typeArgs != null && typeArgs.size() == 1) ? (DecoratedTypeMirror<?>) TypeMirrorDecorator.decorate(typeArgs.get(0), this.env) : TypeMirrorUtils.objectType(this.env);
      }
      else if (!returnsResponseBody) {
        //doesn't return response body; no way to tell what's being returned.
        returnType = TypeMirrorUtils.objectType(this.env);
      }

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

      //now resolve any type variables.
      returnType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(variableContext.resolveTypeVariables(returnType, this.env), this.env);
      returnType.setDeferredDocComment(new ReturnDocComment(this));
    }

    outputPayload = returnType == null || returnType.isVoid() ? null : new ResourceRepresentationMetadata(returnType);


    JavaDoc.JavaDocTagList doclets = localDoc.get("RequestHeader"); //support jax-doclets. see http://jira.codehaus.org/browse/ENUNCIATE-690
    if (doclets != null) {
      for (String doclet : doclets) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String header = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        requestParameters.add(new ExplicitRequestParameter(this, doc, header, ResourceParameterType.HEADER, context));
      }
    }

    List<JavaDoc.JavaDocTagList> inheritedDoclets = AnnotationUtils.getJavaDocTags("RequestHeader", parent);
    for (JavaDoc.JavaDocTagList inheritedDoclet : inheritedDoclets) {
      for (String doclet : inheritedDoclet) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String header = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        requestParameters.add(new ExplicitRequestParameter(this, doc, header, ResourceParameterType.HEADER, context));
      }
    }

    RequestHeaders requestHeaders = getAnnotation(RequestHeaders.class);
    if (requestHeaders != null) {
      for (RequestHeader header : requestHeaders.value()) {
        requestParameters.add(new ExplicitRequestParameter(this, header.description(), header.name(), ResourceParameterType.HEADER, context));
      }
    }

    List<RequestHeaders> inheritedRequestHeaders = AnnotationUtils.getAnnotations(RequestHeaders.class, parent);
    for (RequestHeaders inheritedRequestHeader : inheritedRequestHeaders) {
      for (RequestHeader header : inheritedRequestHeader.value()) {
        requestParameters.add(new ExplicitRequestParameter(this, header.description(), header.name(), ResourceParameterType.HEADER, context));
      }
    }

    ArrayList<ResponseCode> statusCodes = new ArrayList<ResponseCode>();
    ArrayList<ResponseCode> warnings = new ArrayList<ResponseCode>();
    StatusCodes codes = getAnnotation(StatusCodes.class);
    if (codes != null) {
      for (com.webcohesion.enunciate.metadata.rs.ResponseCode code : codes.value()) {
        ResponseCode rc = new ResponseCode(requestMapping);
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        for (ResponseHeader header : code.additionalHeaders()) {
          rc.setAdditionalHeader(header.name(), header.description());
        }
        rc.setType((DecoratedTypeMirror) TypeHintUtils.getTypeHint(code.type(), this.env, null));
        statusCodes.add(rc);
      }
    }

    ResponseStatus responseStatus = getAnnotation(ResponseStatus.class);
    if (responseStatus != null) {
      HttpStatus code = responseStatus.value();
      if (code == HttpStatus.INTERNAL_SERVER_ERROR) {
        try {
          code = responseStatus.code();
        }
        catch (IncompleteAnnotationException e) {
          //fall through; 'responseStatus.code' was added in 4.2.
        }
      }
      ResponseCode rc = new ResponseCode(requestMapping);
      rc.setCode(code.value());
      String reason = responseStatus.reason();
      if (!reason.isEmpty()) {
        rc.setCondition(reason);
      }
      statusCodes.add(rc);
    }

    List<StatusCodes> inheritedStatusCodes = AnnotationUtils.getAnnotations(StatusCodes.class, parent);
    for (StatusCodes inheritedStatusCode : inheritedStatusCodes) {
      for (com.webcohesion.enunciate.metadata.rs.ResponseCode code : inheritedStatusCode.value()) {
        ResponseCode rc = new ResponseCode(requestMapping);
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        for (ResponseHeader header : code.additionalHeaders()) {
          rc.setAdditionalHeader(header.name(), header.description());
        }
        rc.setType((DecoratedTypeMirror) TypeHintUtils.getTypeHint(code.type(), this.env, null));
        statusCodes.add(rc);
      }
    }

    doclets = localDoc.get("HTTP");
    if (doclets != null) {
      for (String doclet : doclets) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String code = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        try {
          ResponseCode rc = new ResponseCode(requestMapping);
          rc.setCode(Integer.parseInt(code));
          rc.setCondition(doc);
          statusCodes.add(rc);
        }
        catch (NumberFormatException e) {
          //fall through...
        }
      }
    }

    inheritedDoclets = AnnotationUtils.getJavaDocTags("HTTP", parent);
    for (JavaDoc.JavaDocTagList inheritedDoclet : inheritedDoclets) {
      for (String doclet : inheritedDoclet) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String code = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        try {
          ResponseCode rc = new ResponseCode(requestMapping);
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
      for (com.webcohesion.enunciate.metadata.rs.ResponseCode code : warningInfo.value()) {
        ResponseCode rc = new ResponseCode(requestMapping);
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        warnings.add(rc);
      }
    }

    List<Warnings> inheritedWarnings = AnnotationUtils.getAnnotations(Warnings.class, parent);
    for (Warnings inheritedWarning : inheritedWarnings) {
      for (com.webcohesion.enunciate.metadata.rs.ResponseCode code : inheritedWarning.value()) {
        ResponseCode rc = new ResponseCode(requestMapping);
        rc.setCode(code.code());
        rc.setCondition(code.condition());
        warnings.add(rc);
      }
    }

    doclets = localDoc.get("HTTPWarning");
    if (doclets != null) {
      for (String doclet : doclets) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String code = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        try {
          ResponseCode rc = new ResponseCode(requestMapping);
          rc.setCode(Integer.parseInt(code));
          rc.setCondition(doc);
          warnings.add(rc);
        }
        catch (NumberFormatException e) {
          //fall through...
        }
      }
    }

    inheritedDoclets = AnnotationUtils.getJavaDocTags("HTTPWarning", parent);
    for (JavaDoc.JavaDocTagList inheritedDoclet : inheritedDoclets) {
      for (String doclet : inheritedDoclet) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String code = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        try {
          ResponseCode rc = new ResponseCode(requestMapping);
          rc.setCode(Integer.parseInt(code));
          rc.setCondition(doc);
          warnings.add(rc);
        }
        catch (NumberFormatException e) {
          //fall through...
        }
      }
    }

    ResponseHeaders responseHeaders = getAnnotation(ResponseHeaders.class);
    if (responseHeaders != null) {
      for (ResponseHeader header : responseHeaders.value()) {
        this.responseHeaders.put(header.name(), header.description());
      }
    }

    List<ResponseHeaders> inheritedResponseHeaders = AnnotationUtils.getAnnotations(ResponseHeaders.class, parent);
    for (ResponseHeaders inheritedResponseHeader : inheritedResponseHeaders) {
      for (ResponseHeader header : inheritedResponseHeader.value()) {
        this.responseHeaders.put(header.name(), header.description());
      }
    }

    doclets = localDoc.get("ResponseHeader");
    if (doclets != null) {
      for (String doclet : doclets) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String header = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        this.responseHeaders.put(header, doc);
      }
    }

    inheritedDoclets = AnnotationUtils.getJavaDocTags("ResponseHeader", parent);
    for (JavaDoc.JavaDocTagList inheritedDoclet : inheritedDoclets) {
      for (String doclet : inheritedDoclet) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String header = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        this.responseHeaders.put(header, doc);
      }
    }

    this.entityParameter = entityParameter;
    this.requestParameters = requestParameters;
    this.parent = parent;
    this.statusCodes = statusCodes;
    this.warnings = warnings;
    this.representationMetadata = outputPayload;
  }
  
  // the following name denotes 'no content' semantics:
  // - com.webcohesion.enunciate.metadata.rs.TypeHint.NO_CONTENT
  private boolean isNoContentType(String fqn) {
    String noContentClassName = TypeHint.NO_CONTENT.class.getName();
    return fqn.equals(noContentClassName.replace('$', '.'));
  }

  @Override
  protected ParamDocComment createParamDocComment(VariableElement param) {
    return new RSParamDocComment(this, param.getSimpleName().toString());
  }

  public EnunciateSpringWebContext getContext() {
    return context;
  }

  /**
   * The list of resource parameters that this method requires to be invoked.
   *
   * @return The list of resource parameters that this method requires to be invoked.
   */
  public Set<RequestParameter> getRequestParameters() {
    return this.requestParameters;
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
   * The response headers that are expected on this resource method.
   *
   * @return The response headers that are expected on this resource method.
   */
  public Map<String, String> getResponseHeaders() {
    return responseHeaders;
  }

}
