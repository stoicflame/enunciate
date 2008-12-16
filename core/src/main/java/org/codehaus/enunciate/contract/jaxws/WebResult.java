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

package org.codehaus.enunciate.contract.jaxws;

import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.TypeVisitor;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.jaxb.ImplicitChildElement;
import org.codehaus.enunciate.contract.jaxb.adapters.Adaptable;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterType;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterUtil;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.validation.ValidationException;

import javax.jws.soap.SOAPBinding;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * A decorated type mirror that is a web result.
 *
 * @author Ryan Heaton
 */
public class WebResult extends DecoratedTypeMirror implements Adaptable, WebMessage, WebMessagePart, ImplicitChildElement {

  private final boolean header;
  private final String name;
  private final String elementName;
  private final String targetNamespace;
  private final String partName;
  private final WebMethod method;
  private final AdapterType adapterType;

  protected WebResult(TypeMirror delegate, WebMethod method) {
    super(delegate);
    this.method = method;

    javax.jws.WebResult annotation = method.getAnnotation(javax.jws.WebResult.class);

    String targetNamespace = "";
    if ((annotation != null) && (annotation.targetNamespace() != null) && (!"".equals(annotation.targetNamespace()))) {
      targetNamespace = annotation.targetNamespace();
    }
    this.targetNamespace = targetNamespace;

    String partName = "return";
    if ((annotation != null) && (!"".equals(annotation.partName()))) {
      partName = annotation.partName();
    }
    this.partName = partName;
    this.header = ((annotation != null) && (annotation.header()));
    this.adapterType = AdapterUtil.findAdapterType(method);

    String name = "return";
    if ((annotation != null) && (annotation.name() != null) && (!"".equals(annotation.name()))) {
      name = annotation.name();
      this.elementName = name;
    }
    else if (!isHeader() && isImplicitSchemaElement()) {
      this.elementName = method.getSimpleName() + "Response";
    }
    else if (this.header) {
      this.elementName = "";
    }
    else {
      this.elementName = name;
    }
    this.name = name;
  }

  public void accept(TypeVisitor typeVisitor) {
    delegate.accept(typeVisitor);
  }

  /**
   * The name of the web result.
   *
   * @return The name of the web result.
   */
  public String getName() {
    return name;
  }

  /**
   * The namespace of the web result.
   *
   * @return The namespace of the web result.
   */
  public String getTargetNamespace() {
    return targetNamespace;
  }

  /**
   * The part name.
   *
   * @return The part name.
   */
  public String getPartName() {
    return partName;
  }

  /**
   * The web method.
   *
   * @return The web method.
   */
  public WebMethod getWebMethod() {
    return method;
  }

  /**
   * Whether this is a bare web result.
   *
   * @return Whether this is a bare web result.
   */
  private boolean isBare() {
    return method.getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE;
  }

  /**
   * The message name in the case of a document/bare service.
   *
   * @return The message name in the case of a document/bare service.
   */
  public String getMessageName() {
    String messageName = null;

    if (isBare()) {
      messageName = method.getDeclaringEndpointInterface().getSimpleName() + "." + method.getSimpleName() + "Response";
    }
    else if (isHeader()) {
      messageName = method.getDeclaringEndpointInterface().getSimpleName() + "." + method.getSimpleName() + "." + getName();
    }

    return messageName;
  }

  /**
   * There is only message documentation if this web result is BARE.
   *
   * @return The documentation if BARE, null otherwise.
   */
  public String getMessageDocs() {
    if (isBare()) {
      return getDocComment();
    }

    return null;
  }

  // Inherited.
  public boolean isInput() {
    return false;
  }

  // Inherited.
  public boolean isOutput() {
    return true;
  }

  // Inherited.
  public boolean isHeader() {
    return header;
  }

  // Inherited.
  public boolean isFault() {
    return false;
  }

  /**
   * If this web result is a part, the comments for the result.
   *
   * @return The part docs.
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
   * The qname of the particle for this web result.  If the {@link #getParticleType() particle type} is
   * TYPE then it's the qname of the xml type.  Otherwise, it's the qname of the implicit schema
   * element.
   *
   * @return The qname of the particle for this web result as a part.
   */
  public QName getParticleQName() {
    if (method.getSoapBindingStyle() == SOAPBinding.Style.RPC && !isHeader()) {
      return getTypeQName();
    }
    else {      
      return new QName(method.getDeclaringEndpointInterface().getTargetNamespace(), getElementName());
    }
  }

  /**
   * This web result defines an implicit schema element if it is of DOCUMENT binding style and it is
   * either BARE or a header.
   *
   * @return Whether this web result is an implicit schema element.
   */
  public boolean isImplicitSchemaElement() {
    return isHeader() || (method.getSoapBindingStyle() != SOAPBinding.Style.RPC && method.getSoapParameterStyle() == SOAPBinding.ParameterStyle.BARE);
  }

  // Inherited.
  public Collection<WebMessagePart> getParts() {
    if (!isBare() && !isHeader()) {
      throw new UnsupportedOperationException("Web result doesn't represent a complex method input/output.");
    }

    return new ArrayList<WebMessagePart>(Arrays.asList(this));
  }

  /**
   * The qname of the type of this result as an implicit schema element.
   *
   * @return The qname of the type of this result.
   * @throws ValidationException If the type is anonymous or otherwise problematic.
   */
  public QName getTypeQName() {
    return getXmlType().getQname();
  }

  /**
   * Gets the xml type of this result.
   *
   * @return The xml type of this result.
   */
  public XmlType getXmlType() {
    try {
      XmlType xmlType = XmlTypeFactory.findSpecifiedType(this);
      if (xmlType == null) {
        xmlType = XmlTypeFactory.getXmlType(getType());
      }
      return xmlType;
    }
    catch (XmlTypeException e) {
      throw new ValidationException(method.getPosition(), e.getMessage());
    }
  }

  /**
   * The min occurs of a web result is 0.
   *
   * @return 0
   */
  public int getMinOccurs() {
    return 0;
  }

  /**
   * The max occurs of the web result.
   *
   * @return The max occurs.
   */
  public String getMaxOccurs() {
    DecoratedTypeMirror typeMirror = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(this.delegate);
    return typeMirror.isArray() || typeMirror.isCollection() ? "unbounded" : "1";
  }

  /**
   * The element name.
   *
   * @return The element name.
   */
  public String getElementName() {
    return this.elementName;
  }

  /**
   * The element docs.
   *
   * @return The element docs.
   */
  public String getElementDocs() {
    return ((DecoratedTypeMirror) delegate).getDocComment();
  }

  /**
   * Used when treating this as a parameter.
   *
   * @return The delegate.
   */
  public TypeMirror getType() {
    return this.delegate;
  }

  // Inherited.
  public boolean isAdapted() {
    return this.adapterType != null;
  }

  // Inherited.
  public AdapterType getAdapterType() {
    return this.adapterType;
  }

  @Override
  public boolean isVoid() {
    return ((DecoratedTypeMirror) delegate).isVoid();
  }
}
