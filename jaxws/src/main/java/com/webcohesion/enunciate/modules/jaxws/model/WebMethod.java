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
package com.webcohesion.enunciate.modules.jaxws.model;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.modules.jaxb.model.util.MapType;
import com.webcohesion.enunciate.modules.jaxws.EnunciateJaxwsContext;
import com.webcohesion.enunciate.util.AnnotationUtils;

import javax.jws.Oneway;
import javax.jws.soap.SOAPBinding;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.*;

/**
 * A method invoked on a web service.
 *
 * @author Ryan Heaton
 */
public class WebMethod extends DecoratedExecutableElement implements Comparable<WebMethod>, HasFacets {

  private final javax.jws.WebMethod annotation;
  private final boolean oneWay;
  private final EndpointInterface endpointInterface;
  private final DecoratedTypeMirror returnType;
  private final WebResult webResult;
  private final Collection<WebParam> webParams;
  private final Collection<WebFault> webFaults;
  private final Collection<WebMessage> messages;
  private final RequestWrapper requestWrapper;
  private final ResponseWrapper responseWrapper;
  private final Set<Facet> facets = new TreeSet<Facet>();
  private final EnunciateJaxwsContext context;

  public WebMethod(ExecutableElement delegate, EndpointInterface endpointInterface, EnunciateJaxwsContext context, TypeVariableContext variableContext) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.context = context;

    TypeMirror type = variableContext.resolveTypeVariables(super.getReturnType(), this.env);
    MapType mapType = MapType.findMapType(type, this.context.getJaxbContext());
    if (mapType != null) {
      type = mapType;
    }
    this.returnType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(type, this.env);

    this.annotation = getAnnotation(javax.jws.WebMethod.class);
    this.oneWay = getAnnotation(Oneway.class) != null;
    this.endpointInterface = endpointInterface;
    this.webResult = new WebResult(getReturnType(), this, context);

    List<? extends VariableElement> parameters = getParameters();
    Collection<WebParam> webParameters = new ArrayList<WebParam>(parameters.size());
    int parameterIndex = 0;
    for (VariableElement parameter : parameters) {
      webParameters.add(new WebParam(parameter, this, parameterIndex++, context, variableContext));
    }
    this.webParams = webParameters;

    Collection<WebFault> webFaults = new ArrayList<WebFault>();
    for (TypeMirror referenceType : getThrownTypes()) {
      if (!(referenceType instanceof DeclaredType)) {
        throw new EnunciateException("Method " + getSimpleName() + " of " + endpointInterface.getQualifiedName() + ": Thrown type must be a declared type.");
      }

      TypeElement declaration = (TypeElement) ((DeclaredType) referenceType).asElement();

      if (declaration == null) {
        throw new EnunciateException("Method " + getSimpleName() + " of " + endpointInterface.getQualifiedName() + ": unknown declaration for " + referenceType);
      }

      if (declaration.getQualifiedName().toString().startsWith("java.")) {
        continue; //skip generic java exceptions.
      }

      webFaults.add(new WebFault(declaration, (DecoratedTypeMirror) referenceType, context));
    }
    this.webFaults = webFaults;

    Collection<WebMessage> messages = new ArrayList<WebMessage>();
    SOAPBinding.Style bindingStyle = getSoapBindingStyle();

    //first add all the headers.
    for (WebParam webParam : webParameters) {
      if (webParam.isHeader()) {
        messages.add(webParam);
      }
    }

    if (webResult.isHeader()) {
      messages.add(webResult);
    }

    RequestWrapper requestWrapper = null;
    ResponseWrapper responseWrapper = null;
    if (bindingStyle == SOAPBinding.Style.DOCUMENT) {
      SOAPBinding.ParameterStyle parameterStyle = getSoapParameterStyle();
      if (parameterStyle == SOAPBinding.ParameterStyle.WRAPPED) {
        requestWrapper = new RequestWrapper(this);
        messages.add(requestWrapper);
        if (!isOneWay()) {
          responseWrapper = new ResponseWrapper(this);
          messages.add(responseWrapper);
        }
        messages.addAll(webFaults);
      }
      else {
        for (WebParam webParam : webParameters) {
          if (!webParam.isHeader()) {
            messages.add(webParam);
          }
        }
        if (!isOneWay() && getReturnType().getKind() != TypeKind.VOID && !webResult.isHeader()) {
          messages.add(webResult);
        }

        messages.addAll(webFaults);
      }
    }
    else {
      messages.add(new RPCInputMessage(this));
      messages.add(new RPCOutputMessage(this));
      messages.addAll(webFaults);
    }

    this.messages = messages;
    this.requestWrapper = requestWrapper;
    this.responseWrapper = responseWrapper;
    this.facets.addAll(Facet.gatherFacets(delegate, context.getContext()));
    this.facets.addAll(endpointInterface.getFacets());
  }

  public EnunciateJaxwsContext getContext() {
    return context;
  }

  /**
   * The simple name for client-side code generation.
   *
   * @return The simple name for client-side code generation.
   */
  public String getClientSimpleName() {
    String clientSimpleName = getSimpleName().toString();
    ClientName clientName = getAnnotation(ClientName.class);
    if (clientName != null) {
      clientSimpleName = clientName.value();
    }
    return clientSimpleName;
  }


  @Override
  public DecoratedTypeMirror getReturnType() {
    return this.returnType;
  }

  /**
   * The web result of this web method.
   *
   * @return The web result of this web method.
   */
  public WebResult getWebResult() {
    return this.webResult;
  }

  /**
   * The list of web parameters for this method.
   *
   * @return The list of web parameters for this method.
   */
  public Collection<WebParam> getWebParameters() {
    return this.webParams;
  }

  /**
   * The list of web faults thrown by this method.
   *
   * @return The list of web faults thrown by this method.
   */
  public Collection<WebFault> getWebFaults() {
    return this.webFaults;
  }

  /**
   * Get any documented HTTP request headers for this web method.
   *
   * @return Any documented HTTP request headers for this web method.
   */
  public Collection<HttpHeader> getHttpRequestHeaders() {
    List<HttpHeader> extraParameters = new ArrayList<HttpHeader>();
    JavaDoc localDoc = new JavaDoc(getDocComment(), null, null, this.env);
    JavaDoc.JavaDocTagList doclets = localDoc.get("RequestHeader"); //support jax-doclets. see http://jira.codehaus.org/browse/ENUNCIATE-690
    if (doclets != null) {
      for (String doclet : doclets) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String header = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        extraParameters.add(new HttpHeader(header, doc));
      }
    }

    List<JavaDoc.JavaDocTagList> inheritedDoclets = AnnotationUtils.getJavaDocTags("RequestHeader", getDeclaringEndpointInterface());
    for (JavaDoc.JavaDocTagList inheritedDoclet : inheritedDoclets) {
      for (String doclet : inheritedDoclet) {
        int firstspace = JavaDoc.indexOfFirstWhitespace(doclet);
        String header = firstspace > 0 ? doclet.substring(0, firstspace) : doclet;
        String doc = ((firstspace > 0) && (firstspace + 1 < doclet.length())) ? doclet.substring(firstspace + 1) : "";
        extraParameters.add(new HttpHeader(header, doc));
      }
    }

    RequestHeaders requestHeaders = getAnnotation(RequestHeaders.class);
    if (requestHeaders != null) {
      for (RequestHeader header : requestHeaders.value()) {
        extraParameters.add(new HttpHeader(header.name(), header.description()));
      }
    }

    List<RequestHeaders> inheritedRequestHeaders = AnnotationUtils.getAnnotations(RequestHeaders.class, getDeclaringEndpointInterface());
    for (RequestHeaders inheritedRequestHeader : inheritedRequestHeaders) {
      for (RequestHeader header : inheritedRequestHeader.value()) {
        extraParameters.add(new HttpHeader(header.name(), header.description()));
      }
    }

    return extraParameters;
  }

  /**
   * The messages of this web method.
   *
   * @return The messages of this web method.
   */
  public Collection<WebMessage> getMessages() {
    return this.messages;
  }

  public Collection<WebMessage> getInputMessages() {
    ArrayList<WebMessage> inputMessages = new ArrayList<WebMessage>();
    for (WebMessage message : getMessages()) {
      if (message.isInput()) {
        inputMessages.add(message);
      }
    }
    return inputMessages;
  }

  public Collection<WebMessage> getOutputMessages() {
    ArrayList<WebMessage> outputMessages = new ArrayList<WebMessage>();
    for (WebMessage message : getMessages()) {
      if (message.isOutput()) {
        outputMessages.add(message);
      }
    }
    return outputMessages;
  }

  /**
   * The request wrapper.
   *
   * @return The request wrapper, or null if none.
   */
  public RequestWrapper getRequestWrapper() {
    return requestWrapper;
  }

  /**
   * The response wrapper, or null if none.
   *
   * @return The response wrapper, or null if none.
   */
  public ResponseWrapper getResponseWrapper() {
    return responseWrapper;
  }

  /**
   * A set of the reference namespace for this method.
   *
   * @return A set of the reference namespace for this method.
   */
  public Set<String> getReferencedNamespaces() {
    HashSet<String> namespaces = new HashSet<String>();

    Collection<WebMessage> messages = getMessages();
    for (WebMessage message : messages) {
      for (WebMessagePart part : message.getParts()) {
        if (part.isImplicitSchemaElement()) {
          namespaces.add(part.getParticleQName().getNamespaceURI());
        }
      }
    }
    
    return namespaces;
  }

  /**
   * The operation name of this web method.
   *
   * @return The operation name of this web method.
   */
  public String getOperationName() {
    String operationName = getSimpleName().toString();

    if ((annotation != null) && (!"".equals(annotation.operationName()))) {
      return annotation.operationName();
    }

    return operationName;
  }

  /**
   * The action of this web method.
   *
   * @return The action of this web method.
   */
  public String getAction() {
    String action = "";

    if (annotation != null) {
      action = annotation.action();
    }

    return action;
  }

  /**
   * Whether this web method is one-way.
   *
   * @return Whether this web method is one-way.
   */
  public boolean isOneWay() {
    return oneWay;
  }

  /**
   * The SOAP binding style of this web method.
   *
   * @return The SOAP binding style of this web method.
   */
  public SOAPBinding.Style getSoapBindingStyle() {
    SOAPBinding.Style style = getDeclaringEndpointInterface().getSoapBindingStyle();
    SOAPBinding bindingInfo = getAnnotation(SOAPBinding.class);

    if (bindingInfo != null) {
      style = bindingInfo.style();
    }

    return style;
  }

  /**
   * The declaring web service for this web method.
   *
   * @return The declaring web service for this web method.
   */
  public EndpointInterface getDeclaringEndpointInterface() {
    return endpointInterface;
  }

  /**
   * The SOAP binding use of this web method.
   *
   * @return The SOAP binding use of this web method.
   */
  public SOAPBinding.Use getSoapUse() {
    SOAPBinding.Use use = getDeclaringEndpointInterface().getSoapUse();
    SOAPBinding bindingInfo = getAnnotation(SOAPBinding.class);

    if (bindingInfo != null) {
      use = bindingInfo.use();
    }

    return use;
  }

  /**
   * The SOAP parameter style of this web method.
   *
   * @return The SOAP parameter style of this web method.
   */
  public SOAPBinding.ParameterStyle getSoapParameterStyle() {
    SOAPBinding.ParameterStyle style = getDeclaringEndpointInterface().getSoapParameterStyle();
    SOAPBinding bindingInfo = getAnnotation(SOAPBinding.class);

    if (bindingInfo != null) {
      style = bindingInfo.parameterStyle();
    }

    return style;
  }

  /**
   * Determine whether this web method is doc/lit wrapped.
   *
   * @return Whether this web method is doc/lit wrapped.
   */
  public boolean isDocLitWrapped() {
    return getSoapBindingStyle() == SOAPBinding.Style.DOCUMENT &&
      getSoapUse() == SOAPBinding.Use.LITERAL &&
      getSoapParameterStyle() == SOAPBinding.ParameterStyle.WRAPPED;
  }

  /**
   * Web methods must be unique by name.  (JSR 181: 3.1.1)
   *
   * @param webMethod The web method to compare this to.
   * @return The comparison.
   */
  public int compareTo(WebMethod webMethod) {
    return getOperationName().compareTo(webMethod.getOperationName());
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
