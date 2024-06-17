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
package com.webcohesion.enunciate.modules.jaxb.model;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlTypeFactory;
import com.webcohesion.enunciate.util.BeanValidationUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import jakarta.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An accessor that is marshalled in xml to an xml element.
 *
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class Element extends Accessor {

  private final XmlElement xmlElement;
  private final Collection<Element> choices;
  private boolean isChoice = false;

  public Element(javax.lang.model.element.Element delegate, TypeDefinition typedef, EnunciateJaxbContext context) {
    super(delegate, typedef, context);

    XmlElement xmlElement = getAnnotation(XmlElement.class);
    XmlElements xmlElements = getAnnotation(XmlElements.class);
    if (xmlElements != null) {
      XmlElement[] elementChoices = xmlElements.value();
      if (elementChoices.length == 0) {
        xmlElements = null;
      }
      else if ((xmlElement == null) && (elementChoices.length == 1)) {
        xmlElement = elementChoices[0];
        xmlElements = null;
      }
    }

    this.xmlElement = xmlElement;
    this.choices = new ArrayList<Element>();
    if (xmlElements != null) {
      for (final XmlElement element : xmlElements.value()) {
        DecoratedTypeMirror typeMirror = Annotations.mirrorOf(element::type, this.env, XmlElement.DEFAULT.class);

        if ((typeMirror instanceof ArrayType && ((ArrayType)typeMirror).getComponentType().getKind() != TypeKind.BYTE) || (typeMirror.isCollection())) {
          throw new EnunciateException("Member " + getName() + " of " + typedef.getQualifiedName() + ": an element choice must not be a collection or an array.");
        }

        this.choices.add(new Element(getDelegate(), getTypeDefinition(), element, context));
      }
    }
    else {
      this.choices.add(this);
    }
  }

  /**
   * Construct an element accessor with a specific element annotation.
   *
   * @param delegate   The delegate.
   * @param typedef    The type definition.
   * @param xmlElement The specific element annotation.
   */
  protected Element(javax.lang.model.element.Element delegate, TypeDefinition typedef, XmlElement xmlElement, EnunciateJaxbContext context) {
    super(delegate, typedef, context);
    this.xmlElement = xmlElement;
    this.choices = new ArrayList<Element>();
    this.choices.add(this);
    this.isChoice = true;
  }

  // Inherited.
  public String getName() {
    String propertyName = getSimpleName().toString();

    if ((xmlElement != null) && (!"##default".equals(xmlElement.name()))) {
      propertyName = xmlElement.name();
    }

    return propertyName;
  }

  // Inherited.
  public String getNamespace() {
    String namespace = null;

    if (getForm() == XmlNsForm.QUALIFIED) {
      namespace = getTypeDefinition().getNamespace();
    }

    if ((xmlElement != null) && (!"##default".equals(xmlElement.namespace()))) {
      namespace = xmlElement.namespace();
    }

    return namespace;
  }

  /**
   * The form of this element.
   *
   * @return The form of this element.
   */
  public XmlNsForm getForm() {
    XmlNsForm elementForm = getTypeDefinition().getSchema().getElementFormDefault();

    if (elementForm == null || elementForm == XmlNsForm.UNSET) {
      elementForm = XmlNsForm.UNQUALIFIED;
    }

    return elementForm;
  }

  /**
   * The qname for the referenced element, if this element is a reference to a global element, or null if
   * this element is not a reference element.
   *
   * @return The qname for the referenced element, if exists.
   */
  public QName getRef() {
    QName ref = null;

    boolean qualified = getForm() == XmlNsForm.QUALIFIED;
    String typeNamespace = getTypeDefinition().getNamespace();
    typeNamespace = typeNamespace == null ? "" : typeNamespace;
    String elementNamespace = isWrapped() ? getWrapperNamespace() : getNamespace();
    elementNamespace = elementNamespace == null ? "" : elementNamespace;
    if ((!elementNamespace.equals(typeNamespace)) && (qualified || !"".equals(elementNamespace))) {
      //the namespace is different; must be a ref...
      return new QName(elementNamespace, isWrapped() ? getWrapperName() : getName());
    }
    else {
      //check to see if this is an implied ref as per the jaxb spec, section 8.9.1.2
      com.webcohesion.enunciate.modules.jaxb.model.types.XmlType baseType = getBaseType();
      if ((baseType.isAnonymous()) && (baseType instanceof XmlClassType)) {
        TypeDefinition baseTypeDef = ((XmlClassType) baseType).getTypeDefinition();
        if (baseTypeDef.getAnnotation(XmlRootElement.class) != null) {
          RootElementDeclaration rootElement = new RootElementDeclaration(baseTypeDef.getDelegate(), baseTypeDef, this.context);
          ref = new QName(rootElement.getNamespace(), rootElement.getName());
        }
      }
    }

    return ref;
  }

  /**
   * The type of an element accessor can be specified by an annotation.
   *
   * @return The accessor type.
   */
  @Override
  public DecoratedTypeMirror getAccessorType() {
    DecoratedTypeMirror specifiedType = null;

    if (xmlElement != null) {
      specifiedType = Annotations.mirrorOf(xmlElement::type, this.env, XmlElement.DEFAULT.class);
    }

    if (specifiedType != null) {
      if (!isChoice) {
        DecoratedTypeMirror accessorType = super.getAccessorType();
        if (accessorType.isCollection()) {
          TypeElement collectionElement = (TypeElement) (accessorType.isList() ? TypeMirrorUtils.listType(this.env).asElement() : TypeMirrorUtils.collectionType(this.env).asElement());
          specifiedType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(this.env.getTypeUtils().getDeclaredType(collectionElement, specifiedType), this.env);
        }
        else if (accessorType.isArray() && !(specifiedType.isArray())) {
          specifiedType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(this.env.getTypeUtils().getArrayType(specifiedType), this.env);
        }
      }

      return specifiedType;
    }

    return super.getAccessorType();
  }

  /**
   * The base type of an element accessor can be specified by an annotation.
   *
   * @return The base type.
   */
  @Override
  public com.webcohesion.enunciate.modules.jaxb.model.types.XmlType getBaseType() {
    if (xmlElement != null) {
      TypeMirror typeMirror = Annotations.mirrorOf(xmlElement::type, this.env, XmlElement.DEFAULT.class);

      if (typeMirror != null) {
        return XmlTypeFactory.getXmlType(typeMirror, this.context);
      }
    }

    return super.getBaseType();
  }

  /**
   * Whether this element is nillable.
   *
   * @return Whether this element is nillable.
   */
  public boolean isNillable() {
    boolean nillable = false;

    if (xmlElement != null) {
      nillable = xmlElement.nillable();
    }

    return nillable;
  }

  /**
   * Whether this element is required.
   *
   * @return Whether this element is required.
   */
  public boolean isRequired() {
    boolean required = getDefaultValue() == null && BeanValidationUtils.isNotNull(this, this.env);

    if (xmlElement != null && !required) {
      required = xmlElement.required();
    }

    return required;
  }

  /**
   * The min occurs of this element.
   *
   * @return The min occurs of this element.
   */
  public int getMinOccurs() {
    if (isRequired()) {
      return 1;
    }

    DecoratedTypeMirror accessorType = getAccessorType();
    boolean primitive = accessorType.isPrimitive();
    if ((!primitive) && (accessorType.isArray())) {
      //we have to check if the component type if its an array type, too.
      DecoratedTypeMirror componentType = TypeMirrorUtils.getComponentType(accessorType, this.env);
      primitive = componentType.isPrimitive() && componentType.getKind() != TypeKind.BYTE;
    }

    return primitive ? 1 : 0;
  }

  /**
   * The max occurs of this element.
   *
   * @return The max occurs of this element.
   */
  public String getMaxOccurs() {
    return isCollectionType() ? "unbounded" : "1";
  }

  /**
   * The default value, or null if none exists.
   *
   * @return The default value, or null if none exists.
   */
  public String getDefaultValue() {
    String defaultValue = null;

    if ((xmlElement != null) && (!"\u0000".equals(xmlElement.defaultValue()))) {
      defaultValue = xmlElement.defaultValue();
    }

    return defaultValue;
  }

  /**
   * The choices for this element.
   *
   * @return The choices for this element.
   */
  public Collection<? extends Element> getChoices() {
    return choices;
  }

  /**
   * Whether this xml element is wrapped.
   *
   * @return Whether this xml element is wrapped.
   */
  public boolean isWrapped() {
    return (isCollectionType() && (getAnnotation(XmlElementWrapper.class) != null));
  }

  /**
   * The name of the wrapper element.
   *
   * @return The name of the wrapper element.
   */
  public String getWrapperName() {
    String name = getSimpleName().toString();

    XmlElementWrapper xmlElementWrapper = getAnnotation(XmlElementWrapper.class);
    if ((xmlElementWrapper != null) && (!"##default".equals(xmlElementWrapper.name()))) {
      name = xmlElementWrapper.name();
    }

    return name;
  }

  /**
   * The namespace of the wrapper element.
   *
   * @return The namespace of the wrapper element.
   */
  public String getWrapperNamespace() {
    String namespace = null;

    if (getForm() == XmlNsForm.QUALIFIED) {
      namespace = getTypeDefinition().getNamespace();
    }

    XmlElementWrapper xmlElementWrapper = getAnnotation(XmlElementWrapper.class);
    if ((xmlElementWrapper != null) && (!"##default".equals(xmlElementWrapper.namespace()))) {
      namespace = xmlElementWrapper.namespace();
    }

    return namespace;
  }

  /**
   * Whether the wrapper is nillable.
   *
   * @return Whether the wrapper is nillable.
   */
  public boolean isWrapperNillable() {
    boolean nillable = false;

    XmlElementWrapper xmlElementWrapper = getAnnotation(XmlElementWrapper.class);
    if (xmlElementWrapper != null) {
      nillable = xmlElementWrapper.nillable();
    }

    return nillable;
  }

  /**
   * Whether the wrapper is required.
   *
   * @return Whether the wrapper is required.
   */
  public boolean isWrapperRequired() {
    boolean required = false;

    XmlElementWrapper xmlElementWrapper = getAnnotation(XmlElementWrapper.class);
    if (xmlElementWrapper != null) {
        required = xmlElementWrapper.required();
    }

    return required;
  }
  
  /**
   * Whether this is a choice of multiple element refs.
   *
   * @return Whether this is a choice of multiple element refs.
   */
  public boolean isElementRefs() {
    return false;
  }

}
