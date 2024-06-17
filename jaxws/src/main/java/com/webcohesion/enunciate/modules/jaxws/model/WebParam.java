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
import com.webcohesion.enunciate.javac.decorations.element.DecoratedVariableElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jaxb.model.ImplicitChildElement;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.Adaptable;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlTypeFactory;
import com.webcohesion.enunciate.modules.jaxb.model.util.MapType;
import com.webcohesion.enunciate.modules.jaxws.EnunciateJaxwsContext;
import com.webcohesion.enunciate.modules.jaxws.model.util.JAXWSUtil;
import com.webcohesion.enunciate.util.HasClientConvertibleType;
import com.webcohesion.enunciate.util.BeanValidationUtils;

import jakarta.jws.soap.SOAPBinding;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import jakarta.xml.bind.annotation.XmlAttachmentRef;
import jakarta.xml.bind.annotation.XmlMimeType;
import jakarta.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import jakarta.xml.ws.Holder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class WebParam extends DecoratedVariableElement implements Adaptable, WebMessage, WebMessagePart, ImplicitChildElement, HasClientConvertibleType {

  private final jakarta.jws.WebParam annotation;
  private final WebMethod method;
  private final AdapterType adapterType;
  private final boolean useSourceParameterNames;
  private final int parameterIndex;
  private final EnunciateJaxwsContext context;
  private final TypeMirror webParamType;

  protected WebParam(VariableElement delegate, WebMethod method, int parameterIndex, EnunciateJaxwsContext context, TypeVariableContext variableContext) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.context = context;

    this.method = method;
    this.parameterIndex = parameterIndex;
    if (this.method == null) {
      throw new IllegalArgumentException("A web method must be provided.");
    }

    this.annotation = delegate.getAnnotation(jakarta.jws.WebParam.class);
    this.adapterType = JAXWSUtil.findAdapterType(this, context.getJaxbContext());
    this.useSourceParameterNames = context.isUseSourceParameterNames();

    TypeMirror type = variableContext.resolveTypeVariables(super.asType(), this.env);
    MapType mapType = MapType.findMapType(type, this.context.getJaxbContext());
    if (mapType != null) {
      type = mapType;
    }
    this.webParamType = type;
  }

  /**
   * The base param name.
   *
   * @return The base param name.
   */
  public String getBaseParamName() {
    return this.useSourceParameterNames ? getSimpleName().toString() : "arg" + this.parameterIndex;
  }

  /**
   * The web method for this web param.
   *
   * @return The web method for this web param.
   */
  public WebMethod getWebMethod() {
    return method;
  }

  /**
   * The element name of this web param.
   *
   * @return The element name of this web param.
   */
  public String getElementName() {
    String name = isHeader() ? "" : getBaseParamName();

    if ((annotation != null) && (annotation.name() != null) && (!"".equals(annotation.name()))) {
      name = annotation.name();
    }
    else if (!isHeader() && isImplicitSchemaElement()) {
      name = this.method.getSimpleName().toString();
    }

    return name;
  }

  /**
   * The target namespace of this web param.
   *
   * @return The target namespace of this web param.
   */
  public String getTargetNamespace() {
    String namespace = isImplicitSchemaElement() ? method.getDeclaringEndpointInterface().getTargetNamespace() : "";

    if ((annotation != null) && (annotation.targetNamespace() != null) && (!"".equals(annotation.targetNamespace()))) {
      namespace = annotation.targetNamespace();
    }

    return namespace;
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

  /**
   * The doc comment associated with this web param.
   *
   * @return The doc comment associated with this web param.
   */
  public String getElementDocs() {
    return getDocComment();
  }

  /**
   * The part name of the message for this parameter.
   *
   * @return The part name of the message for this parameter.
   */
  public String getPartName() {
    String partName = getBaseParamName();

    if ((annotation != null) && (annotation.partName() != null) && (!"".equals(annotation.partName()))) {
      partName = annotation.partName();
    }

    return partName;
  }

  /**
   * The message name of the message for this parameter, if this is a BARE web param.
   *
   * @return The message name of the message for this parameter, or null if this is not a BARE web param.
   */
  public String getMessageName() {
    String messageName = null;

    if (isBare()) {
      messageName = method.getDeclaringEndpointInterface().getSimpleName() + "." + method.getSimpleName();
    }
    else if (isHeader()) {
      messageName = method.getDeclaringEndpointInterface().getSimpleName() + "." + method.getSimpleName() + "." + getBaseParamName();
    }

    return messageName;
  }

  /**
   * There is only message documentation if this web parameter is BARE.
   *
   * @return The documentation if BARE, null otherwise.
   */
  public String getMessageDocs() {
    if (isBare()) {
      return getDocComment();
    }

    return null;
  }

  /**
   * There is only part documentation if this web parameter is not BARE.
   *
   * @return null if BARE, the documantation otherwise.
   */
  public String getPartDocs() {
    if (isBare()) {
      return null;
    }

    return getDocComment();
  }

  /**
   * If the web method style is RPC, the particle type is TYPE.  Otherwise, it's ELEMENT.
   *
   * @return The particle type.
   */
  public ParticleType getParticleType() {
    return this.method.getSoapBindingStyle() == SOAPBinding.Style.RPC ? isHeader() ? ParticleType.ELEMENT : ParticleType.TYPE : ParticleType.ELEMENT;
  }

  /**
   * The qname of the particle for this parameter.  If the {@link #getParticleType() particle type} is
   * TYPE then it's the qname of the xml type.  Otherwise, it's the qname of the implicit schema
   * element.
   *
   * @return The qname of the particle for this part.
   */
  public QName getParticleQName() {
    if (method.getSoapBindingStyle() == SOAPBinding.Style.RPC && !isHeader()) {
      return getTypeQName();
    }
    else {
      return new QName(getTargetNamespace(), getElementName());
    }
  }

  /**
   * This web parameter defines an implicit schema element if it is DOCUMENT binding style and either BARE or a header.
   *
   * @return Whether this web parameter is an implicit schema element.
   */
  public boolean isImplicitSchemaElement() {
    return isHeader() || (method.getSoapBindingStyle() != SOAPBinding.Style.RPC && method.getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE);
  }

  /**
   * The qname of the xml type type of this parameter.
   *
   * @return The qname of the type of this parameter.
   * @throws IllegalStateException If the type is anonymous or otherwise problematic.
   */
  public QName getTypeQName() {
    return getXmlType().getQname();
  }

  @Override
  public TypeMirror getType() {
    return this.webParamType;
  }

  /**
   * Gets the xml type for this web parameter.
   *
   * @return The xml type.
   */
  public XmlType getXmlType() {
    XmlType xmlType = XmlTypeFactory.findSpecifiedType(this, this.context.getJaxbContext());

    if (xmlType == null) {
      TypeMirror type = getType();
      if (isHolder()) {
        List<? extends TypeMirror> typeArgs = ((DeclaredType) type).getTypeArguments();
        if ((typeArgs == null) || (typeArgs.size() == 0)) {
          throw new EnunciateException("Parameter " + getSimpleName() + ": unable to get the type of the holder.");
        }

        type = typeArgs.iterator().next();
      }
      xmlType = XmlTypeFactory.getXmlType(type, this.context.getJaxbContext());
    }
    return xmlType;
  }

  public String getMimeType() {
    XmlMimeType mimeType = getAnnotation(XmlMimeType.class);
    return mimeType == null ? null : mimeType.value();
  }

  public boolean isSwaRef() {
    return getAnnotation(XmlAttachmentRef.class) != null;
  }

  /**
   * The min occurs of this parameter as a child element.
   *
   * @return 1 if primitive.  0 otherwise.
   */
  public int getMinOccurs() {
    DecoratedTypeMirror paramType = (DecoratedTypeMirror) getType();
    XmlElement xmlElement = delegate.getAnnotation(XmlElement.class);
    boolean required = xmlElement!=null ? xmlElement.required() : false;
    return (paramType.isPrimitive() ||
            required ||
            BeanValidationUtils.isNotNull(this, this.env)
           ) ? 1 : 0;
  }

  /**
   * The max occurs of this parameter as a child element.
   *
   * @return The max occurs of this parameter as a child element.
   */
  public String getMaxOccurs() {
    DecoratedTypeMirror paramType = (DecoratedTypeMirror) getType();
    boolean unbounded = paramType.isCollection() || paramType.isArray();
    if (paramType.isArray()) {
      TypeMirror componentType = ((ArrayType) paramType).getComponentType();
      //special case for byte[]
      if (componentType.getKind() == TypeKind.BYTE) {
        unbounded = false;
      }
    }
    return unbounded ? "unbounded" : "1";
  }

  /**
   * The mode of this web param.
   *
   * @return The mode of this web param.
   */
  public jakarta.jws.WebParam.Mode getMode() {
    jakarta.jws.WebParam.Mode mode = jakarta.jws.WebParam.Mode.IN;

    if ((annotation != null) && (annotation.mode() != null)) {
      mode = annotation.mode();
    }

    return mode;
  }

  /**
   * Whether this is a header param.
   *
   * @return Whether this is a header param.
   */
  public boolean isHeader() {
    boolean header = false;

    if (annotation != null) {
      header = annotation.header();
    }

    return header;
  }

  /**
   * Whether this is a bare web param.
   *
   * @return Whether this is a bare web param.
   */
  private boolean isBare() {
    return method.getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE;
  }

  /**
   * Whether this is an input message depends on its mode.
   *
   * @return Whether this is an input message depends on its mode.
   */
  public boolean isInput() {
    return (getMode() == jakarta.jws.WebParam.Mode.IN) ||
      ((getMode() == jakarta.jws.WebParam.Mode.INOUT) && (isHolder()));
  }

  /**
   * Whether this is an output message depends on its mode.
   *
   * @return Whether this is an output message depends on its mode.
   */
  public boolean isOutput() {
    return (getMode() == jakarta.jws.WebParam.Mode.OUT) ||
      ((getMode() == jakarta.jws.WebParam.Mode.INOUT) && (isHolder()));
  }

  /**
   * Whether the parameter type is a holder.
   *
   * @return Whether the parameter type is a holder.
   */
  public boolean isHolder() {
    TypeMirror type = getType();
    if (type instanceof DeclaredType) {
      Element element = ((DeclaredType) type).asElement();
      if (element instanceof TypeElement) {
        return ((TypeElement) element).getQualifiedName().toString().equals(Holder.class.getName());
      }
    }
    return false;
  }

  /**
   * @return false
   */
  public boolean isFault() {
    return false;
  }

  /**
   * If this web param is complex, it will only have one part: itself.
   *
   * @return this.
   * @throws UnsupportedOperationException if this web param isn't complex.
   */
  public Collection<WebMessagePart> getParts() {
    if (!isBare() && !isHeader()) {
      throw new UnsupportedOperationException("Web param doesn't represent a complex method input/output.");
    }

    return new ArrayList<WebMessagePart>(Arrays.asList(this));
  }

  // Inherited.
  public boolean isAdapted() {
    return this.adapterType != null;
  }

  // Inherited.
  public AdapterType getAdapterType() {
    return adapterType;
  }

  @Override
  public TypeMirror getClientConvertibleType() {
    return getType();
  }

  public EnunciateJaxwsContext getContext() {
    return context;
  }
}
