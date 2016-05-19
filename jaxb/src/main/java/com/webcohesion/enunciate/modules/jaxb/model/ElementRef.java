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

package com.webcohesion.enunciate.modules.jaxb.model;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.javac.decorations.Annotations;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlTypeFactory;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * An accessor that is marshalled in xml to an xml element.
 *
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class ElementRef extends Element {

  private final XmlElementRef xmlElementRef;
  private final Collection<ElementRef> choices;
  private final ReferencedElement referencedElement;
  private boolean isChoice = false;

  public ElementRef(javax.lang.model.element.Element delegate, TypeDefinition typedef, EnunciateJaxbContext context) {
    super(delegate, typedef, context);

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
    Collection<ElementRef> choices;
    if (xmlElementRefs != null) {
      choices = new ArrayList<ElementRef>();
      for (XmlElementRef elementRef : xmlElementRefs.value()) {
        choices.add(new ElementRef(getDelegate(), getTypeDefinition(), elementRef, context));
      }

      this.referencedElement = null;
    }
    else {
      DecoratedTypeMirror accessorType = getBareAccessorType();
      if (accessorType.isInstanceOf(JAXBElement.class)) {
        //this is either a single-valued JAXBElement, or a parametric collection of them...
        //todo: throw an exception if this is referencing a non-global element for this namespace?
        choices = new ArrayList<ElementRef>();
        choices.add(this);
        QName refQname = new QName(xmlElementRef.namespace(), xmlElementRef.name());
        List<? extends TypeMirror> elementTypeArguments = ((DecoratedDeclaredType) accessorType).getTypeArguments();
        DecoratedTypeMirror elementOf = elementTypeArguments == null || elementTypeArguments.size() != 1 ? TypeMirrorUtils.objectType(context.getContext().getProcessingEnvironment()) : (DecoratedTypeMirror) elementTypeArguments.get(0);
        this.referencedElement = new TypeReferencedElement(refQname, elementOf);
      }
      else if (isCollectionType()) {
        choices = new CollectionOfElementRefChoices();
        this.referencedElement = null;
      }
      else {
        choices = new ArrayList<ElementRef>();
        choices.add(this);
        this.referencedElement = loadRef();
      }
    }
    this.choices = choices;
  }

  /**
   * Construct an element accessor with a specific element ref annotation.
   *
   * @param delegate      The delegate.
   * @param typedef       The type definition.
   * @param xmlElementRef The specific element ref annotation.
   */
  protected ElementRef(javax.lang.model.element.Element delegate, TypeDefinition typedef, XmlElementRef xmlElementRef, EnunciateJaxbContext context) {
    super(delegate, typedef, context);
    this.xmlElementRef = xmlElementRef;
    this.choices = new ArrayList<ElementRef>();
    this.choices.add(this);
    this.referencedElement = loadRef();
    this.isChoice = true;
  }

  /**
   * Construct an element accessor with a specific base type.
   *
   * @param delegate The delegate.
   * @param typedef  The type definition.
   * @param ref      The referenced root element.
   */
  private ElementRef(javax.lang.model.element.Element delegate, TypeDefinition typedef, ElementDeclaration ref, EnunciateJaxbContext context) {
    super(delegate, typedef, context);
    this.xmlElementRef = null;
    this.choices = new ArrayList<ElementRef>();
    this.choices.add(this);
    this.referencedElement = new ElementReferencedElement(ref);
    this.isChoice = true;
  }

  /**
   * Load the qname of the referenced root element declaration.
   *
   * @return the qname of the referenced root element declaration.
   */
  protected ReferencedElement loadRef() {
    DecoratedTypeMirror refType = null;

    if (xmlElementRef != null) {
      refType = Annotations.mirrorOf(new Callable<Class<?>>() {
        @Override
        public Class<?> call() throws Exception {
          return xmlElementRef.type();
        }
      }, this.env, XmlElementRef.DEFAULT.class);
    }

    if (refType == null) {
      refType = getBareAccessorType();
    }

    TypeElement declaration = null;
    if (refType.isDeclared()) {
      declaration = (TypeElement) ((DeclaredType)refType).asElement();
    }

    ReferencedElement referencedElement = null;
    if (refType.isInstanceOf(JAXBElement.class)) {
      String localName = xmlElementRef != null && !"##default".equals(xmlElementRef.name()) ? xmlElementRef.name() : null;
      String namespace = xmlElementRef != null ? xmlElementRef.namespace() : "";
      if (localName == null) {
        throw new EnunciateException("Member " + getName() + " of " + getTypeDefinition().getQualifiedName() + ": @XmlElementRef annotates a type JAXBElement without specifying the name of the JAXB element.");
      }

      QName refQname = new QName(namespace, localName);
      List<? extends TypeMirror> elementTypeArguments = ((DecoratedDeclaredType) refType).getTypeArguments();
      DecoratedTypeMirror elementOf = elementTypeArguments == null || elementTypeArguments.size() != 1 ? TypeMirrorUtils.objectType(context.getContext().getProcessingEnvironment()) : (DecoratedTypeMirror) elementTypeArguments.get(0);
      referencedElement = new TypeReferencedElement(refQname, elementOf);
    }
    else if (declaration != null && declaration.getAnnotation(XmlRootElement.class) != null) {
      RootElementDeclaration refElement = new RootElementDeclaration(declaration, null, this.context);
      referencedElement = new DeclarationReferencedElement(new QName(refElement.getNamespace(), refElement.getName()), declaration);
    }

    if (referencedElement == null) {
      throw new EnunciateException("Member " + getSimpleName() + " of " + getTypeDefinition().getQualifiedName() + ": " + refType + " is neither JAXBElement nor a root element declaration.");
    }

    return referencedElement;
  }

  /**
   * Whether this is a choice of multiple element refs.
   *
   * @return Whether this is a choice of multiple element refs.
   */
  public boolean isElementRefs() {
    return (this.referencedElement == null);
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

    return this.referencedElement.getQname().getLocalPart();
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
    return XMLConstants.NULL_NS_URI.equals(this.referencedElement.getQname().getNamespaceURI()) ? null : this.referencedElement.getQname().getNamespaceURI();
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

    return this.referencedElement.getQname();
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

  @Override
  public XmlType getXmlType() {
    if (isElementRefs()) {
      throw new UnsupportedOperationException("No single xml type for this element: multiple choices.");
    }

    return this.referencedElement.getXmlType();
  }

  @Override
  public boolean isQNameType() {
    return false;
  }

  /**
   * The type of an element ref accessor can be specified by an annotation.
   *
   * @return The accessor type.
   */
  @Override
  public DecoratedTypeMirror getAccessorType() {
    DecoratedTypeMirror specifiedType = null;

    if (xmlElementRef != null) {
      specifiedType = Annotations.mirrorOf(new Callable<Class<?>>() {
        @Override
        public Class<?> call() throws Exception {
          return xmlElementRef.type();
        }
      }, this.env, XmlElementRef.DEFAULT.class);
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
    return this.xmlElementRef != null && this.xmlElementRef.required();
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

  @Override
  public boolean isElementRef() {
    return true;
  }

  /**
   * Lazy-loaded collection of element ref choices.
   *
   * @author Ryan Heaton
   */
  @SuppressWarnings ( "NullableProblems" )
  private class CollectionOfElementRefChoices extends AbstractCollection<ElementRef> {

    @Override
    public Iterator<ElementRef> iterator() {
      return lookupRefs().iterator();
    }

    @Override
    public int size() {
      return lookupRefs().size();
    }

    private Collection<ElementRef> lookupRefs() {
      Collection<ElementRef> choices = new ArrayList<ElementRef>();
      Collection<QName> qnamesAdded = new HashSet<QName>();

      //if it's a parametric collection type, we need to provide a choice between all subclasses of the base type.
      DecoratedTypeMirror typeMirror = getBareAccessorType();
      javax.lang.model.element.Element element = env.getTypeUtils().asElement(typeMirror);
      if (element != null) {
        ElementDeclaration xmlElement = context.findElementDeclaration(element);
        if (xmlElement != null) {
          if (qnamesAdded.add(xmlElement.getQname())) {
            choices.add(new ElementRef(getDelegate(), getTypeDefinition(), xmlElement, context));
          }
        }
      }

      return choices;
    }
  }

  private interface ReferencedElement {

    QName getQname();

    XmlType getXmlType();
  }

  private class TypeReferencedElement implements ReferencedElement {
    private final QName qname;
    private final DecoratedTypeMirror mirror;

    public TypeReferencedElement(QName qname, DecoratedTypeMirror mirror) {
      this.qname = qname;
      this.mirror = mirror;
    }

    @Override
    public QName getQname() {
      return qname;
    }

    @Override
    public XmlType getXmlType() {
      return XmlTypeFactory.getXmlType(this.mirror, context);
    }
  }

  private class DeclarationReferencedElement implements ReferencedElement {
    private final QName qname;
    private final TypeElement declaration;

    public DeclarationReferencedElement(QName qname, TypeElement declaration) {
      this.qname = qname;
      this.declaration = declaration;
    }

    @Override
    public QName getQname() {
      return qname;
    }

    @Override
    public XmlType getXmlType() {
      return new XmlClassType(context.findTypeDefinition(this.declaration));
    }
  }

  private class ElementReferencedElement implements ReferencedElement {

    private final ElementDeclaration element;

    public ElementReferencedElement(ElementDeclaration element) {
      this.element = element;
    }

    @Override
    public QName getQname() {
      return this.element.getQname();
    }

    @Override
    public XmlType getXmlType() {
      return this.element instanceof LocalElementDeclaration ? ((LocalElementDeclaration) this.element).getElementXmlType() : new XmlClassType(((RootElementDeclaration)this.element).getTypeDefinition());
    }
  }
}
