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

package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.*;
import com.sun.mirror.util.Types;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import org.codehaus.enunciate.contract.jaxb.types.XmlClassType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.validation.ValidationException;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An accessor that is marshalled in xml to an xml element.
 *
 * @author Ryan Heaton
 */
public class Element extends Accessor {

  private final XmlElement xmlElement;
  private final Collection<Element> choices;
  private boolean isChoice = false;

  public Element(MemberDeclaration delegate, TypeDefinition typedef) {
    super(delegate, typedef);

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
      for (XmlElement element : xmlElements.value()) {
        try {
          Class clazz = element.type();
          if ((clazz == null) || (clazz == XmlElement.DEFAULT.class)) {
            throw new ValidationException(getPosition(), "Member " + getName() + " of " + typedef.getQualifiedName() + ": an element choice must have its type specified.");
          }
          else if ((clazz.isArray()) || (Collection.class.isAssignableFrom(clazz))) {
            throw new ValidationException(getPosition(), "Member " + getName() + " of " + typedef.getQualifiedName() + ": an element choice must not be a collection or an array.");
          }
        }
        catch (MirroredTypeException e) {
          // Fall through.
          // If the mirrored type exception is thrown, we couldn't load the class.  This probably
          // implies that the type is valid and it's in the source base.
        }

        this.choices.add(new Element((MemberDeclaration) getDelegate(), getTypeDefinition(), element));
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
  protected Element(MemberDeclaration delegate, TypeDefinition typedef, XmlElement xmlElement) {
    super(delegate, typedef);
    this.xmlElement = xmlElement;
    this.choices = new ArrayList<Element>();
    this.choices.add(this);
    this.isChoice = true;
  }

  // Inherited.
  public String getName() {
    String propertyName = getSimpleName();

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
      XmlType baseType = getBaseType();
      if ((baseType.isAnonymous()) && (baseType instanceof XmlClassType)) {
        TypeDefinition baseTypeDef = ((XmlClassType) baseType).getTypeDefinition();
        if (baseTypeDef.getAnnotation(XmlRootElement.class) != null) {
          RootElementDeclaration rootElement = new RootElementDeclaration((ClassDeclaration) baseTypeDef.getDelegate(), baseTypeDef);
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
  public TypeMirror getAccessorType() {
    TypeMirror specifiedType = null;
    try {
      if ((xmlElement != null) && (xmlElement.type() != XmlElement.DEFAULT.class)) {
        Class clazz = xmlElement.type();
        specifiedType = getAccessorType(clazz);
      }
    }
    catch (MirroredTypeException e) {
      // The mirrored type exception implies that the specified type is within the source base.
      specifiedType = TypeMirrorDecorator.decorate(e.getTypeMirror());
    }

    if (specifiedType != null) {
      if (!isChoice) {
        DecoratedTypeMirror accessorType = (DecoratedTypeMirror) super.getAccessorType();

        if (accessorType.isCollection()) {
          AnnotationProcessorEnvironment ape = Context.getCurrentEnvironment();
          Types types = ape.getTypeUtils();
          if (specifiedType instanceof PrimitiveType) {
            specifiedType = types.getPrimitiveType(((PrimitiveType) specifiedType).getKind());
          }
          else {
            specifiedType = types.getDeclaredType(ape.getTypeDeclaration(((DeclaredType) specifiedType).getDeclaration().getQualifiedName()));
          }
          specifiedType = TypeMirrorDecorator.decorate(types.getDeclaredType(ape.getTypeDeclaration(((DeclaredType) accessorType).getDeclaration().getQualifiedName()), specifiedType));
        }
        else if (accessorType.isArray()) {
          Types types = Context.getCurrentEnvironment().getTypeUtils();
          if (specifiedType instanceof PrimitiveType) {
            specifiedType = types.getPrimitiveType(((PrimitiveType) specifiedType).getKind());
          }
          else {
            specifiedType = types.getDeclaredType(((DeclaredType) specifiedType).getDeclaration());
          }
          specifiedType = TypeMirrorDecorator.decorate(types.getArrayType(specifiedType));
        }
      }

      return specifiedType;
    }

    return super.getAccessorType();
  }

  /**
   * Get the accessor type for the specified class.
   *
   * @param clazz The class.
   * @return The accessor type.
   */
  protected TypeMirror getAccessorType(Class clazz) {
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    TypeMirror undecorated;
    if (clazz.isPrimitive()) {
      if (Boolean.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.BOOLEAN);
      }
      else if (Byte.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.BYTE);
      }
      else if (Character.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.CHAR);
      }
      else if (Double.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.DOUBLE);
      }
      else if (Float.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.FLOAT);
      }
      else if (Integer.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.INT);
      }
      else if (Long.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.LONG);
      }
      else if (Short.TYPE == clazz) {
        undecorated = env.getTypeUtils().getPrimitiveType(PrimitiveType.Kind.SHORT);
      }
      else {
        throw new IllegalArgumentException("Unknown primitive type: " + clazz.getName());
      }
    }
    else if (clazz.isArray()) {
      undecorated = env.getTypeUtils().getArrayType(getAccessorType(clazz.getComponentType()));
    }
    else {
      TypeDeclaration typeDeclaration = env.getTypeDeclaration(clazz.getName());
      //todo: worry about the formal type parameters?
      undecorated = env.getTypeUtils().getDeclaredType(typeDeclaration);
    }

    return TypeMirrorDecorator.decorate(undecorated);
  }

  /**
   * The base type of an element accessor can be specified by an annotation.
   *
   * @return The base type.
   */
  @Override
  public XmlType getBaseType() {
    if (xmlElement != null) {
      Class typeClass = null;
      TypeMirror typeMirror = null;
      try {
        typeClass = xmlElement.type();
      }
      catch (MirroredTypeException e) {
        typeMirror = e.getTypeMirror();
      }

      try {
        if (typeClass == null) {
          return XmlTypeFactory.getXmlType(typeMirror);
        }
        else if (typeClass != XmlElement.DEFAULT.class) {
          return XmlTypeFactory.getXmlType(typeClass);
        }
      }
      catch (XmlTypeException e) {
        throw new ValidationException(getPosition(), "Member " + getName() + " of " + getTypeDefinition().getQualifiedName() + e.getMessage());
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
    boolean required = false;

    if (xmlElement != null) {
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

    TypeMirror accessorType = getAccessorType();
    boolean primitive = (accessorType instanceof PrimitiveType);
    if ((!primitive) && (accessorType instanceof ArrayType)) {
      //we have to check if the component type if its an array type, too.
      TypeMirror componentType = ((ArrayType) accessorType).getComponentType();
      primitive = (componentType instanceof PrimitiveType) && (((PrimitiveType)componentType).getKind() != PrimitiveType.Kind.BYTE);
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
    String name = getSimpleName();

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

}
