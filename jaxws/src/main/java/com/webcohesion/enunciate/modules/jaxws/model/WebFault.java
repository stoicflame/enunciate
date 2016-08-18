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
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.element.PropertyElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.metadata.Ignore;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.ElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.ImplicitChildElement;
import com.webcohesion.enunciate.modules.jaxb.model.ImplicitRootElement;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.Adaptable;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlTypeFactory;
import com.webcohesion.enunciate.modules.jaxb.model.util.JAXBUtil;
import com.webcohesion.enunciate.modules.jaxb.model.util.MapType;
import com.webcohesion.enunciate.modules.jaxws.EnunciateJaxwsContext;
import com.webcohesion.enunciate.util.HasClientConvertibleType;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.XmlAttachmentRef;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * A fault that is declared potentially thrown in some web service call.
 *
 * @author Ryan Heaton
 */
public class WebFault extends DecoratedTypeElement implements WebMessage, WebMessagePart, ImplicitRootElement, HasFacets {

  private final javax.xml.ws.WebFault annotation;
  private final DeclaredType explicitFaultBeanType;
  private final Set<Facet> facets = new TreeSet<Facet>();
  private final EnunciateJaxwsContext context;
  private final DecoratedTypeMirror reference;

  public WebFault(TypeElement delegate, DecoratedTypeMirror reference, EnunciateJaxwsContext context) {
    super(delegate, context.getContext().getProcessingEnvironment());
    this.context = context;

    this.annotation = getAnnotation(javax.xml.ws.WebFault.class);
    this.reference = reference;

    DeclaredType explicitFaultBeanType = null;
    List<PropertyElement> properties = getProperties();
    PropertyElement faultInfoProperty = null;
    for (PropertyElement propertyDeclaration : properties) {
      if ("faultInfo".equals(propertyDeclaration.getPropertyName())) {
        faultInfoProperty = propertyDeclaration;
        break;
      }
    }

    if ((faultInfoProperty != null) && (faultInfoProperty.getPropertyType() instanceof DeclaredType)) {
      DeclaredType faultInfoType = (DeclaredType) faultInfoProperty.getPropertyType();
      if (faultInfoType.asElement() == null) {
        throw new EnunciateException(getQualifiedName() + ": class not found: " + faultInfoType + ".");
      }

      boolean messageConstructorFound = false;
      boolean messageAndThrowableConstructorFound = false;
      List<ExecutableElement> constructors = getConstructors();
      for (ExecutableElement constructor : constructors) {
        if (constructor.getModifiers().contains(Modifier.PUBLIC)) {
          VariableElement[] parameters = constructor.getParameters().toArray(new VariableElement[constructor.getParameters().size()]);
          if (parameters.length >= 2) {
            DecoratedTypeMirror param0Type = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(parameters[0].asType(), this.env);
            DecoratedTypeMirror param1Type = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(parameters[1].asType(), this.env);
            if (parameters.length == 2) {
              messageConstructorFound |= param0Type.isInstanceOf(String.class.getName()) && param1Type.isInstanceOf(faultInfoType);
            }
            else if (parameters.length == 3) {
              DecoratedTypeMirror param2Type = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(parameters[2].asType(), this.env);
              messageAndThrowableConstructorFound |= param0Type.isInstanceOf(String.class.getName())
                && param1Type.isInstanceOf(faultInfoType)
                && param2Type.isInstanceOf(Throwable.class);
            }
          }
        }
      }

      if (messageConstructorFound && messageAndThrowableConstructorFound) {
        explicitFaultBeanType = faultInfoType;
      }
    }

    if (faultInfoProperty != null && explicitFaultBeanType == null) {
      throw new EnunciateException("The 'getFaultInfo' method is only allowed on a web fault if you're " +
        "declaring an explicit fault bean, and you don't have the right constructor signatures set up in order for '" +
        faultInfoProperty.getPropertyType() + "' to be an explicit fault bean.");
    }

    this.explicitFaultBeanType = explicitFaultBeanType;
    this.facets.addAll(Facet.gatherFacets(delegate));
  }

  /**
   * The message name of this fault.
   *
   * @return The message name of this fault.
   */
  public String getMessageName() {
    return getSimpleName().toString();
  }

  /**
   * The message documentation for a fault is the documentation for its type.
   *
   * @return The documentation for its type.
   */
  public String getMessageDocs() {
    return getElementDocs();
  }

  @Override
  public Set<Facet> getFacets() {
    return this.facets;
  }

  /**
   * The element name of the implicit web fault bean, or null if this isn't an implicit web fault.
   *
   * @return The element name of the implicit web fault, or null.
   */
  public String getElementName() {
    String name = null;

    if (isImplicitSchemaElement()) {
      name = getParticleName();
    }

    return name;
  }

  private String getParticleName() {
    String name;
    name = getSimpleName().toString();

    if ((annotation != null) && (annotation.name() != null) && (!"".equals(annotation.name()))) {
      name = annotation.name();
    }
    return name;
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
   * The comments on the fault itself.
   *
   * @return The comments on the fault itself.
   */
  public String getElementDocs() {
    String docs = getJavaDoc().toString();
    if (docs.trim().length() == 0) {
      docs = null;
    }
    return docs;
  }

  /**
   * The part name of this web fault as it would appear in wsdl.
   *
   * @return The part name of this web fault as it would appear in wsdl.
   */
  public String getPartName() {
    return getSimpleName().toString();
  }

  /**
   * @return Description of the conditions under which this fault will be thrown.
   */
  public String getPartDocs() {
    return getConditions();
  }

  /**
   * @return Description of the conditions under which this fault will be thrown.
   */
  public String getConditions() {
    return this.reference.getDocValue();
  }

  /**
   * The qualified name of the implicit fault bean of this web fault, or null if this web fault
   * does not define an implicit faul bean.
   *
   * @return The qualified name of the implicit fault bean of this web fault.
   */
  public String getImplicitFaultBeanQualifiedName() {
    String faultBean = null;

    if (isImplicitSchemaElement()) {
      faultBean = getPackage().getQualifiedName() + ".jaxws." + getSimpleName() + "Bean";

      if ((annotation != null) && (annotation.faultBean() != null) && (!"".equals(annotation.faultBean()))) {
        faultBean = annotation.faultBean();
      }
    }

    return faultBean;
  }

  /**
   * A web fault has an explicit fault bean if all three of the following are present:
   * <p/>
   * <ol>
   * <li>A getFaultInfo method that returns the bean instance of a class type.
   * <li>A constructor taking a message and bean instance.
   * <li>A constructor taking a message, a bean instance, and a cause.
   * </ol>
   *
   * @return The type of the explicit fault bean, if exists, or null otherwise.
   */
  public DeclaredType getExplicitFaultBeanType() {
    return explicitFaultBeanType;
  }

  /**
   * A web fault has an explicit fault bean if all three of the following are present:
   * <p/>
   * <ol>
   * <li>A getFaultInfo method that returns the bean instance of a class type.
   * <li>A constructor taking a message and bean instance.
   * <li>A constructor taking a message, a bean instance, and a cause.
   * </ol>
   *
   * @return The explicit fault bean of this web fault, if exists, or null otherwise.
   */
  public ElementDeclaration findExplicitFaultBean() {
    if (this.explicitFaultBeanType == null || this.explicitFaultBeanType.asElement() == null) {
      return null;
    }

    return this.context.getJaxbContext().findElementDeclaration(this.explicitFaultBeanType.asElement());
  }

  /**
   * @return {@link ParticleType#ELEMENT}
   */
  public ParticleType getParticleType() {
    return ParticleType.ELEMENT;
  }

  /**
   * The qname reference to the fault info.
   *
   * @return The qname reference to the fault info.
   */
  public QName getParticleQName() {
    ElementDeclaration faultBean = findExplicitFaultBean();
    if (faultBean != null) {
      return new QName(faultBean.getNamespace(), faultBean.getName());
    }
    else {
      return new QName(getTargetNamespace(), getParticleName());
    }
  }

  /**
   * Gets the target namespace of the implicit fault bean, or null if this web fault defines
   * an explicit fault info bean.
   *
   * @return the target namespace of the implicit fault bean, or null.
   */
  public String getTargetNamespace() {
    String targetNamespace = null;

    if (isImplicitSchemaElement()) {
      if (annotation != null) {
        targetNamespace = annotation.targetNamespace();
      }

      if ((targetNamespace == null) || ("".equals(targetNamespace))) {
        targetNamespace = calculateNamespaceURI();
      }
    }

    return targetNamespace;
  }


  /**
   * Calculates a namespace URI for a given package.  Default implementation uses the algorithm defined in
   * section 3.2 of the jax-ws spec.
   *
   * @return The calculated namespace uri.
   */
  protected String calculateNamespaceURI() {
    PackageElement pkg = getPackage();
    if ((pkg == null) || ("".equals(pkg.getQualifiedName().toString()))) {
      throw new EnunciateException(getQualifiedName() + ": a web fault in no package must specify a target namespace.");
    }

    String[] tokens = pkg.getQualifiedName().toString().split("\\.");
    String uri = "http://";
    for (int i = tokens.length - 1; i >= 0; i--) {
      uri += tokens[i];
      if (i != 0) {
        uri += ".";
      }
    }
    uri += "/";
    return uri;
  }

  /**
   * If there is an explicit fault bean, it will be a root schema element referencing its own type. Otherwise,
   * the type is anonymous.
   *
   * @return null.
   */
  public QName getTypeQName() {
    return null;
  }

  /**
   * This web fault defines an implicit schema element if it does not have an explicit fault bean.
   *
   * @return Whether this web fault defines an implicit schema element.
   */
  public boolean isImplicitSchemaElement() {
    return (this.explicitFaultBeanType == null);
  }

  /**
   * If this is an implicit fault bean, return the child elements.
   *
   * @return The child elements of the bean, or null if none.
   */
  public Collection<ImplicitChildElement> getChildElements() {
    if (!isImplicitSchemaElement()) {
      return Collections.emptyList();
    }

    Set<ImplicitChildElement> childElements = new TreeSet<ImplicitChildElement>(new Comparator<ImplicitChildElement>() {
      public int compare(ImplicitChildElement o1, ImplicitChildElement o2) {
        return o1.getElementName().compareTo(o2.getElementName());
      }
    });

    for (PropertyElement property : getAllFaultProperties(this)) {
      String propertyName = property.getPropertyName();
      if (("cause".equals(propertyName)) || ("localizedMessage".equals(propertyName)) || ("stackTrace".equals(propertyName)) || "suppressed".equals(propertyName)) {
        continue;
      }

      childElements.add(new FaultBeanChildElement(property, this, context.getJaxbContext()));
    }

    return childElements;
  }

  /**
   * Gets all properties, including properties from the superclass.
   *
   * @param declaration The declaration from which to get all properties.
   * @return All properties.
   */
  protected Collection<PropertyElement> getAllFaultProperties(DecoratedTypeElement declaration) {
    ArrayList<PropertyElement> properties = new ArrayList<PropertyElement>();

    Set<String> excludedProperties = new TreeSet<String>();
    while ((declaration != null) && (!Object.class.getName().equals(declaration.getQualifiedName().toString()))) {
      for (PropertyElement property : declaration.getProperties()) {
        if (property.getGetter() != null &&
          property.getAnnotation(XmlTransient.class) == null &&
          property.getAnnotation(Ignore.class) == null &&
          !excludedProperties.contains(property.getPropertyName())) {
          //only the readable properties that are not marked with @XmlTransient
          properties.add(property);
        }
        else {
          excludedProperties.add(property.getPropertyName());
        }
      }

      declaration = (DecoratedTypeElement) ((DeclaredType)declaration.getSuperclass()).asElement();
    }

    return properties;
  }

  /**
   * There's only one part to a web fault.
   *
   * @return this.
   */
  public Collection<WebMessagePart> getParts() {
    return new ArrayList<WebMessagePart>(Arrays.asList(this));
  }

  /**
   * @return false
   */
  public boolean isInput() {
    return false;
  }

  /**
   * @return true
   */
  public boolean isOutput() {
    return false;
  }

  /**
   * @return false
   */
  public boolean isHeader() {
    return false;
  }

  /**
   * @return true
   */
  public boolean isFault() {
    return true;
  }

  public WebMethod getWebMethod() {
    throw new UnsupportedOperationException("Web faults aren't associated with a specific web method.");
  }

  public EnunciateJaxwsContext getContext() {
    return context;
  }

  public static class FaultBeanChildElement implements Adaptable, ImplicitChildElement, HasClientConvertibleType {

    private final EnunciateJaxbContext context;
    private final PropertyElement property;
    private final int minOccurs;
    private final String maxOccurs;
    private final AdapterType adaperType;
    private final WebFault webFault;

    private FaultBeanChildElement(PropertyElement property, WebFault webFault, EnunciateJaxbContext context) {
      this.context = context;
      DecoratedTypeMirror propertyType = (DecoratedTypeMirror) property.getPropertyType();
      this.adaperType = JAXBUtil.findAdapterType(property.getGetter(), context);
      int minOccurs = propertyType.isPrimitive() ? 1 : 0;
      boolean unbounded = propertyType.isCollection() || propertyType.isArray();
      if (propertyType.isArray()) {
        TypeMirror componentType = ((ArrayType) propertyType).getComponentType();
        //special case for byte[]
        if (componentType.getKind() == TypeKind.BYTE) {
          unbounded = false;
        }
      }
      String maxOccurs = unbounded ? "unbounded" : "1";

      this.property = property;
      this.minOccurs = minOccurs;
      this.maxOccurs = maxOccurs;
      this.webFault = webFault;
    }

    public PropertyElement getProperty() {
      return property;
    }

    public String getElementName() {
      return property.getPropertyName();
    }

    public String getTargetNamespace() {
      return webFault.getTargetNamespace();
    }

    public String getElementDocs() {
      String docs = property.getJavaDoc().toString();
      if (docs.trim().length() == 0) {
        docs = null;
      }
      return docs;
    }

    public XmlType getXmlType() {
      XmlType xmlType = XmlTypeFactory.findSpecifiedType(this, context);
      if (xmlType == null) {
        xmlType = XmlTypeFactory.getXmlType(getType(), context);
      }
      return xmlType;
    }

    public String getMimeType() {
      XmlMimeType mimeType = property.getAnnotation(XmlMimeType.class);
      return mimeType == null ? null : mimeType.value();
    }

    public boolean isSwaRef() {
      return property.getAnnotation(XmlAttachmentRef.class) != null;
    }

    public QName getTypeQName() {
      return getXmlType().getQname();
    }

    public int getMinOccurs() {
      return minOccurs;
    }

    public String getMaxOccurs() {
      return maxOccurs;
    }

    public TypeMirror getType() {
      TypeMirror propertyType = property.getPropertyType();
      MapType mapType = MapType.findMapType(propertyType, context);
      if (mapType != null) {
        propertyType = mapType;
      }
      return propertyType;
    }

    public boolean isAdapted() {
      return this.adaperType != null;
    }

    public AdapterType getAdapterType() {
      return this.adaperType;
    }

    @Override
    public TypeMirror getClientConvertibleType() {
      return getType();
    }
  }

}
