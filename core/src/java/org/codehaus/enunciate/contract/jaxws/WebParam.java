/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.contract.jaxws;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.decorations.declaration.DecoratedParameterDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.jaxb.RootElementDeclaration;
import org.codehaus.enunciate.contract.jaxb.adapters.Adaptable;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterType;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterUtil;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.validation.ValidationException;
import org.codehaus.enunciate.util.MapTypeUtil;
import org.codehaus.enunciate.util.MapType;

import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class WebParam extends DecoratedParameterDeclaration implements Adaptable, WebMessage, WebMessagePart, ImplicitChildElement {

  private final javax.jws.WebParam annotation;
  private final WebMethod method;
  private final AdapterType adapterType;

  protected WebParam(ParameterDeclaration delegate, WebMethod method) {
    super(delegate);

    this.method = method;
    if (this.method == null) {
      throw new IllegalArgumentException("A web method must be provided.");
    }

    annotation = delegate.getAnnotation(javax.jws.WebParam.class);
    this.adapterType = AdapterUtil.findAdapterType(this);
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
    String name = getSimpleName();

    if ((annotation != null) && (annotation.name() != null) && (!"".equals(annotation.name()))) {
      name = annotation.name();
    }

    return name;
  }

  /**
   * The doc comment associated with this web param.
   *
   * @return The doc comment associated with this web param.
   */
  public String getElementDocs() {
    return getDelegate().getDocComment();
  }

  /**
   * The part name of the message for this parameter.
   *
   * @return The part name of the message for this parameter.
   */
  public String getPartName() {
    String partName = getSimpleName();

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
      messageName = method.getDeclaringEndpointInterface().getSimpleName() + "." + method.getSimpleName() + "." + getSimpleName();
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
      return getDelegate().getDocComment();
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

    return getDelegate().getDocComment();
  }

  /**
   * If the web method style is RPC, the particle type is TYPE.  Otherwise, it's ELEMENT.
   *
   * @return The particle type.
   */
  public ParticleType getParticleType() {
    return this.method.getSoapBindingStyle() == SOAPBinding.Style.RPC ? ParticleType.TYPE : ParticleType.ELEMENT;
  }

  /**
   * The qname of the particle for this parameter.  If the {@link #getParticleType() particle type} is
   * TYPE then it's the qname of the xml type.  Otherwise, if the parameter type is an xml root element,
   * the qname of the root xml element is returned.  Otherwise, it's the qname of the implicit schema
   * element.
   *
   * @return The qname of the particle for this part.
   */
  public QName getParticleQName() {
    TypeMirror parameterType = getType();
    if (parameterType instanceof DeclaredType) {
      TypeDeclaration parameterTypeDeclaration = ((DeclaredType) parameterType).getDeclaration();
      if ((method.getSoapBindingStyle() == SOAPBinding.Style.DOCUMENT) && (parameterTypeDeclaration.getAnnotation(XmlRootElement.class) != null)) {
        RootElementDeclaration rootElement = new RootElementDeclaration((ClassDeclaration) parameterTypeDeclaration, null);
        return new QName(rootElement.getNamespace(), rootElement.getName());
      }
    }

    if (method.getSoapBindingStyle() == SOAPBinding.Style.RPC) {
      return getTypeQName();
    }

    return new QName(method.getDeclaringEndpointInterface().getTargetNamespace(), getElementName());
  }

  /**
   * This web parameter defines an implicit schema element if it is DOCUMENT binding style and it
   * is NOT of a class type that is an xml root element.
   *
   * @return Whether this web parameter is an implicit schema element.
   */
  public boolean isImplicitSchemaElement() {
    if (method.getSoapBindingStyle() != SOAPBinding.Style.RPC) {
      TypeMirror parameterType = getType();
      return !((parameterType instanceof DeclaredType) && (((DeclaredType) parameterType).getDeclaration().getAnnotation(XmlRootElement.class) != null));
    }

    return false;
  }

  /**
   * The qname of the xml type type of this parameter.
   *
   * @return The qname of the type of this parameter.
   * @throws ValidationException If the type is anonymous or otherwise problematic.
   */
  public QName getTypeQName() {
    return getXmlType().getQname();
  }

  /**
   * Gets the xml type for this web parameter.
   *
   * @return The xml type.
   */
  public XmlType getXmlType() {
    try {
      XmlType xmlType = XmlTypeFactory.findSpecifiedType(this);

      if (xmlType == null) {
        TypeMirror type = getType();
        if (isHolder()) {
          Collection<TypeMirror> typeArgs = ((DeclaredType) type).getActualTypeArguments();
          if ((typeArgs == null) || (typeArgs.size() == 0)) {
            throw new ValidationException(getPosition(), "Unable to get the type of the holder.");
          }

          type = typeArgs.iterator().next();
        }
        xmlType = XmlTypeFactory.getXmlType(type);
      }
      return xmlType;
    }
    catch (XmlTypeException e) {
      throw new ValidationException(getPosition(), e.getMessage());
    }
  }

  /**
   * The min occurs of this parameter as a child element.  Always 1.
   *
   * @return 1
   */
  public int getMinOccurs() {
    return 1;
  }

  /**
   * The max occurs of this parameter as a child element.
   *
   * @return The max occurs of this parameter as a child element.
   */
  public String getMaxOccurs() {
    DecoratedTypeMirror paramType = (DecoratedTypeMirror) getType();
    return paramType.isArray() || paramType.isCollection() ? "unbounded" : "1";
  }

  /**
   * The mode of this web param.
   *
   * @return The mode of this web param.
   */
  public javax.jws.WebParam.Mode getMode() {
    javax.jws.WebParam.Mode mode = javax.jws.WebParam.Mode.IN;

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
    return (getMode() == javax.jws.WebParam.Mode.IN) ||
      ((getMode() == javax.jws.WebParam.Mode.INOUT) && (isHolder()));
  }

  /**
   * Whether this is an output message depends on its mode.
   *
   * @return Whether this is an output message depends on its mode.
   */
  public boolean isOutput() {
    return (getMode() == javax.jws.WebParam.Mode.OUT) ||
      ((getMode() == javax.jws.WebParam.Mode.INOUT) && (isHolder()));
  }

  /**
   * Whether the parameter type is a holder.
   *
   * @return Whether the parameter type is a holder.
   */
  public boolean isHolder() {
    return ((DecoratedTypeMirror) getType()).isInstanceOf(Holder.class.getName());
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
}
