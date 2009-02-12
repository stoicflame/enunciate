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
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.util.Types;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.validation.ValidationException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An accessor that is marshalled in xml to an xml element.
 *
 * @author Ryan Heaton
 */
public class ElementRef extends Element {

  private final XmlElementRef xmlElementRef;
  private final Collection<ElementRef> choices;
  private final QName ref;

  public ElementRef(MemberDeclaration delegate, TypeDefinition typedef) {
    super(delegate, typedef, null);

    XmlElementRef xmlElementRef = getAnnotation(XmlElementRef.class);
    XmlElementRefs xmlElementRefs = getAnnotation(XmlElementRefs.class);

    if (xmlElementRefs != null) {
      XmlElementRef[] elementRefChoices = xmlElementRefs.value();
      if (elementRefChoices.length == 0) {
        xmlElementRefs = null;
      }
      else if ((xmlElementRef == null) && (elementRefChoices.length == 1)) {
        xmlElementRef = elementRefChoices[0];
        xmlElementRefs = null;
      }
    }

    this.xmlElementRef = xmlElementRef;
    this.choices = new ArrayList<ElementRef>();
    if (xmlElementRefs != null) {
      for (XmlElementRef elementRef : xmlElementRefs.value()) {
        this.choices.add(new ElementRef((MemberDeclaration) getDelegate(), getTypeDefinition(), elementRef));
      }

      this.ref = null;
    }
    else if (((DecoratedTypeMirror) getBareAccessorType()).isInstanceOf(JAXBElement.class.getName())) {
      //this is either a single-valued JAXBElement, or a parametric collection of them...
      //todo: throw an exception if this is referencing a non-global element for this namespace?
      this.choices.add(this);
      this.ref = new QName(xmlElementRef.namespace(), xmlElementRef.name());
    }
    else if (isCollectionType()) {
      //if it's a parametric collection type, we need to provide a choice between all subclasses of the base type.
      TypeMirror typeMirror = getBareAccessorType();
      if (typeMirror instanceof DeclaredType) {
        String fqn = ((DeclaredType) typeMirror).getDeclaration().getQualifiedName();
        EnunciateFreemarkerModel model = ((EnunciateFreemarkerModel) FreemarkerModel.get());
        for (RootElementDeclaration rootElement : model.getRootElementDeclarations()) {
          if (isInstanceOf(rootElement, fqn)) {
            this.choices.add(new ElementRef((MemberDeclaration) getDelegate(), getTypeDefinition(), rootElement));
          }
        }
      }

      if (this.choices.isEmpty()) {
        throw new ValidationException(getPosition(), String.format("No known root element subtypes of %s", typeMirror));
      }

      this.ref = null;
    }
    else {
      this.choices.add(this);
      this.ref = loadRef();
    }
  }

  /**
   * Determines whether the class declaration is an instance of the declared type of the given fully-qualified name.
   *
   * @param classDeclaration The class declaration.
   * @param fqn              The FQN.
   * @return Whether the class declaration is an instance of the declared type of the given fully-qualified name.
   */
  protected boolean isInstanceOf(ClassDeclaration classDeclaration, String fqn) {
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    Types utils = env.getTypeUtils();
    DeclaredType declaredType = utils.getDeclaredType(env.getTypeDeclaration(classDeclaration.getQualifiedName()));
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(declaredType);
    return decorated.isInstanceOf(fqn);
  }

  /**
   * Construct an element accessor with a specific element ref annotation.
   *
   * @param delegate      The delegate.
   * @param typedef       The type definition.
   * @param xmlElementRef The specific element ref annotation.
   */
  protected ElementRef(MemberDeclaration delegate, TypeDefinition typedef, XmlElementRef xmlElementRef) {
    super(delegate, typedef);
    this.xmlElementRef = xmlElementRef;
    this.choices = new ArrayList<ElementRef>();
    this.choices.add(this);
    this.ref = loadRef();
  }

  /**
   * Construct an element accessor with a specific base type.
   *
   * @param delegate The delegate.
   * @param typedef  The type definition.
   * @param ref      The referenced root element.
   */
  private ElementRef(MemberDeclaration delegate, TypeDefinition typedef, RootElementDeclaration ref) {
    super(delegate, typedef);
    this.xmlElementRef = null;
    this.choices = new ArrayList<ElementRef>();
    this.choices.add(this);
    this.ref = new QName(ref.getNamespace(), ref.getName());
  }

  /**
   * Load the qname of the referenced root element declaration.
   *
   * @return the qname of the referenced root element declaration.
   */
  protected QName loadRef() {
    TypeDeclaration declaration = null;
    String elementDeclaration;
    try {
      if ((xmlElementRef != null) && (xmlElementRef.type() != XmlElementRef.DEFAULT.class)) {
        Class typeClass = xmlElementRef.type();
        elementDeclaration = typeClass.getName();
        declaration = getEnv().getTypeDeclaration(typeClass.getName());
      }
      else {
        TypeMirror accessorType = getAccessorType();
        elementDeclaration = accessorType.toString();
        if (accessorType instanceof DeclaredType) {
          declaration = ((DeclaredType) accessorType).getDeclaration();
        }
      }
    }
    catch (MirroredTypeException e) {
      //This exception implies the ref is within the source base.
      TypeMirror typeMirror = e.getTypeMirror();
      elementDeclaration = typeMirror.toString();
      if (typeMirror instanceof DeclaredType) {
        declaration = ((DeclaredType) typeMirror).getDeclaration();
      }
    }

    QName refQName = null;
    if (declaration instanceof ClassDeclaration) {
      if (declaration.getQualifiedName().equals(JAXBElement.class.getName())) {
        String localName = xmlElementRef != null && !"##default".equals(xmlElementRef.name()) ? xmlElementRef.name() : null;
        String namespace = xmlElementRef != null ? xmlElementRef.namespace() : "";
        if (localName == null) {
          throw new ValidationException(getPosition(), "@XmlElementRef annotates a type JAXBElement without specifying the name of the JAXB element.");
        }
        refQName = new QName(namespace, localName);
      }
      else if (declaration.getAnnotation(XmlRootElement.class) != null) {
        ClassDeclaration classDeclaration = (ClassDeclaration) declaration;
        RootElementDeclaration refElement = new RootElementDeclaration(classDeclaration, ((EnunciateFreemarkerModel) FreemarkerModel.get()).findTypeDefinition(classDeclaration));
        refQName = new QName(refElement.getNamespace(), refElement.getName());
      }
    }

    if (refQName == null) {
      throw new ValidationException(getPosition(), elementDeclaration + " is neither JAXBElement nor a root element declaration.");
    }

    return refQName;
  }

  /**
   * Whether this is a choice of multiple element refs.
   *
   * @return Whether this is a choice of multiple element refs.
   */
  public boolean isElementRefs() {
    return (this.ref == null);
  }

  /**
   * The name of an element ref is the name of the element it references, unless the type is a JAXBElement, in which
   * case the name is specified.
   *
   * @return The name of the element ref.
   */
  @Override
  public String getName() {
    if (isElementRefs()) {
      throw new UnsupportedOperationException("No single reference for this element: multiple choices.");
    }

    return this.ref.getLocalPart();
  }

  /**
   * The namespace of an element ref is the namespace of the element it references, unless the type is a JAXBElement, in which
   * case the namespace is specified.
   *
   * @return The namespace of the element ref.
   */
  @Override
  public String getNamespace() {
    if (isElementRefs()) {
      throw new UnsupportedOperationException("No single reference for this element: multiple choices.");
    }

    //it's kind of weird to return null when the namespace is the default namesapce, but that's what the rest of the classes do...
    return XMLConstants.NULL_NS_URI.equals(this.ref.getNamespaceURI()) ? null : this.ref.getNamespaceURI();
  }

  /**
   * The ref for this element ref.
   *
   * @return The ref.
   */
  @Override
  public QName getRef() {
    if (isElementRefs()) {
      throw new UnsupportedOperationException("No single reference for this element: multiple choices.");
    }

    return this.ref;
  }

  /**
   * There is no base type for an element ref.
   *
   * @throws UnsupportedOperationException Because there is no such things as a base type for an element ref.
   */
  @Override
  public XmlType getBaseType() {
    throw new UnsupportedOperationException("There is no base type for an element ref.");
  }

  /**
   * The type of an element accessor can be specified by an annotation.
   *
   * @return The accessor type.
   */
  @Override
  public TypeMirror getAccessorType() {
    try {
      if ((xmlElementRef != null) && (xmlElementRef.type() != XmlElementRef.DEFAULT.class)) {
        Class clazz = xmlElementRef.type();
        return getAccessorType(clazz);
      }
    }
    catch (MirroredTypeException e) {
      // The mirrored type exception implies that the specified type is within the source base.
      return TypeMirrorDecorator.decorate(e.getTypeMirror());
    }

    return super.getAccessorType();
  }

  /**
   * An element ref is not binary data.  It may refer to an element that is binary data, though...
   *
   * @return false
   */
  @Override
  public boolean isBinaryData() {
    return false;
  }

  /**
   * An element ref is not a swa ref.  In may refer to an element that is a swa ref, though...
   *
   * @return false
   */
  @Override
  public boolean isSwaRef() {
    return false;
  }

  /**
   * An element ref is not an MTOM attachment.  It may refer to an element that is an MTOM attachment, though...
   *
   * @return false
   */
  @Override
  public boolean isMTOMAttachment() {
    return false;
  }

  /**
   * An element ref is not nillable.
   *
   * @return false
   */
  @Override
  public boolean isNillable() {
    return false;
  }

  /**
   * An element ref is not required.
   *
   * @return false.
   */
  @Override
  public boolean isRequired() {
    return false;
  }

  /**
   * The min occurs of an element ref is 0
   *
   * @return 0
   */
  @Override
  public int getMinOccurs() {
    return isRequired() ? 1 : 0;
  }

  /**
   * The choices for this element.
   *
   * @return The choices for this element.
   */
  @Override
  public Collection<ElementRef> getChoices() {
    return choices;
  }

  /**
   * The current environment.
   *
   * @return The current environment.
   */
  protected AnnotationProcessorEnvironment getEnv() {
    return Context.getCurrentEnvironment();
  }

  @Override
  public boolean isElementRef() {
    return true;
  }
}
